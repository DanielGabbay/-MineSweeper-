// 3264696
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Tester {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Optional {
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface LowerBound {
	}

	
	@SuppressWarnings("serial")
	public static class TesterException extends RuntimeException {
		public TesterException(String msg) {
			super(msg);
		}
	}

	private String testDescr;
	private boolean errorExists;
	private boolean errorEver = false;
	private StringBuilder output = null;
	private String extraDescr = "";
	public boolean testInitialized;

	// -----------------------------------------------------------------------------
	// Error reporting and checking
	// -----------------------------------------------------------------------------

	public void setOutput(StringBuilder output) {
		this.output = output;
	}

	public boolean errorExists() {
		return errorExists;
	}

	public boolean errorEver() {
		return errorEver;
	}

	public void setErrorExists(boolean val) {
		errorExists = val;
		if (val)
			errorEver = true;
	}

	public void initTest(String descr) {
		this.testDescr = descr;
		setErrorExists(false);
		testInitialized = true;
	}

	public void setExtraDescr(String extraDescr) {
		this.extraDescr = extraDescr;
	}

	private void initPublishedTest0(String msg) {
		String name = new Throwable().getStackTrace()[2].getMethodName();
		initTest("From published test file, " + msg + ": " + name + "()");
	}

	public void initPublishedTest(Class<?> c, String msg) {
		initPublishedTest0(c.getSimpleName() + ", " + msg);
	}

	public void initPublishedTest(Class<?> c) {
		initPublishedTest0(c.getSimpleName());
	}

	public void initPublishedTest(String msg) {
		initPublishedTest0(msg);
	}

	public void initRealTest(Class<?> c, String descr) {
		initTest(c.getSimpleName() + ": " + descr);
	}

	public void initRealTest(String descr) {
		initTest(descr);
	}

	public void done() {
		print("\n------------------------------------------------\n");
		if (errorEver) {
			print(this.getClass().getSimpleName()
					+ " has failed tests! see above.\n");
		} else
			print(this.getClass().getSimpleName() + " all tests passed.\n");
	}

	private void print(String text) {
		if (output == null)
			System.err.print(text);
		else
			output.append(text);
	}

	public void error(String msg) {
		if (!errorExists) {
			print(testDescr + " " + extraDescr + ":\n");
			setErrorExists(true);
		}
		print("    " + msg + ".\n");
	}

	public boolean check(boolean cond, String msg) {
		if (!cond) {
			error(msg);
			return false;
		}
		return true;
	}

	public boolean check(boolean cond) {
		return check(cond, "");
	}

	private static boolean eqObjects(Object o1, Object o2) {
		return o1 == o2 || (o1 != null && o1.equals(o2));
	}

	private String explicit(Object o) {
		if (o == null)
			return "null";
		String s = o.toString();
		StringBuilder b = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) != '\n')
				b.append(s.charAt(i));
			else
				b.append("\\n");
		return b.toString();
	}

	public boolean checkSame(Object got, Object expect, String msg) {
		return check(got == expect,
				String.format(
						"%sExpected '%s`<address=%d> but got '%s`<address=%d>",
						msg == null ? "" : msg + ": ", explicit(expect),
						System.identityHashCode(expect), explicit(got),
						System.identityHashCode(got)));
	}

	public boolean checkSame(Object got, Object expect) {
		return checkSame(got, expect, null);
	}

	public boolean checkEq(Object got, Object expect, String msg) {
		return check(eqObjects(got, expect),
				String.format("%sExpected '%s' but got '%s'",
						msg == null ? "" : msg + ": ", explicit(expect),
						explicit(got)));
	}

	public boolean checkEq(Object got, Object expect) {
		return checkEq(got, expect, null);
	}

	public boolean checkEqStr(Object got, Object expect, String msg) {
		return checkEq(got == null ? null : got.toString(),
				expect == null ? null : expect.toString(), msg);
	}

	public boolean checkEqStr(Object got, Object expect) {
		return checkEqStr(got.toString(), expect.toString(), null);
	}

	public boolean checkEqDouble(double got, double expect, String msg) {
		return check(Math.abs(got - expect) < 0.0001,
				String.format("%sExpected '%.4f' but got '%.4f'",
						msg == null ? "" : msg + ": ", expect, got));
	}

	public boolean checkEqDouble(double got, double expect) {
		return checkEqDouble(got, expect, null);
	}

	public interface Thrower {
		public void run() throws Exception;
	}

	public boolean checkThrows(Thrower thrower, Class<? extends Exception> ec,
			String msg) {
		try {
			thrower.run();
		} catch (Exception c) {
			if (ec.isAssignableFrom(c.getClass()))
				return true;
			error(String.format(
					"%s: Should throw %s, but instead is throwing %s", msg,
					ec.getName(), c.getClass().getName()));
			return false;
		}
		error(msg + ", should throw exception " + ec.getName());
		return false;
	}

	// -----------------------------------------------------------------------------
	// Overriding stdin
	// -----------------------------------------------------------------------------

	private byte[] stdinBuf;
	private int stdinIndex;

	public void setInput(String str) {
		stdinBuf = (str + "\n").getBytes(Charset.forName("UTF-8"));
		stdinIndex = 0;
		InputStream inputStream = new InputStream() {
			@Override
			public int read() {
				if (stdinIndex >= stdinBuf.length)
					return -1;

				return stdinBuf[stdinIndex++];
			}
		};
		System.setIn(inputStream);
	}

	// -----------------------------------------------------------------------------
	// Java Reflection stuff
	// -----------------------------------------------------------------------------

	private static boolean isPrivate(Method m) {
		return Modifier.isPrivate(m.getModifiers());
	}

	private static boolean isPrivate(Constructor<?> m) {
		return Modifier.isPrivate(m.getModifiers());
	}

	private static boolean isPrivate(Field f) {
		return Modifier.isPrivate(f.getModifiers());
	}

	private static boolean isOptional(Method m) {
		return m.getAnnotation(Optional.class) != null;
	}

	private static boolean isLowerBound(Class<?> c) {
		return c.getAnnotation(LowerBound.class) != null;
	}

	private static boolean isOptional(Constructor<?> c) {
		return c.getAnnotation(Optional.class) != null;
	}

	private static String descr(int modifier) {
		String res = Modifier.toString(modifier);
		return res.length() > 0 ? res + " " : res;
	}

	private static String descr(Class<?>[] ps) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < ps.length; i++) {
			b.append(ps[i].getSimpleName());
			if (i != ps.length - 1)
				b.append(',');
		}
		return b.toString();
	}

	private static String descr(Method m) {
		return descr(m.getModifiers()) + m.getReturnType().getSimpleName() + " "
				+ m.getName() + "(" + descr(m.getParameterTypes()) + ")"
				+ (m.getExceptionTypes().length == 0
						? ""
						: " throws " + descr(m.getExceptionTypes()));
	}

	private static String descr(Constructor<?> c, Class<?> realClass) {
		return descr(c.getModifiers()) + realClass.getSimpleName() + "("
				+ descr(c.getParameterTypes()) + ")"
				+ (c.getExceptionTypes().length == 0
						? ""
						: " throws " + descr(c.getExceptionTypes()));

	}

	private static String descr(Constructor<?> c) {
		return descr(c, c.getDeclaringClass());
	}

	private static String descr(Field f) {
		return descr(f.getModifiers()) + f.getName();
	}

	private static boolean equalMethods(Method m1, Method m2) {
		if (!m1.getName().equals(m2.getName()))
			return false;
		if (!Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes()))
			return false;
		if (!m1.getReturnType().equals(m2.getReturnType()))
			return false;
		// if (!Arrays.equals(m1.getExceptionTypes(), m2.getExceptionTypes()))
		// return false;
		if (m1.getModifiers() != m2.getModifiers())
			return false;
		return true;
	}

	private static boolean equalConstructors(Constructor<?> m1,
			Constructor<?> m2) {
		if (!Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes()))
			return false;
		// if (!Arrays.equals(m1.getExceptionTypes(), m2.getExceptionTypes()))
		// return false;
		if (m1.getModifiers() != m2.getModifiers())
			return false;
		return true;
	}

	private static boolean equalFields(Field f1, Field f2) {
		if (f1.getName().equals(f2.getName()))
			return false;
		if (f1.getModifiers() != f2.getModifiers())
			return false;
		return true;
	}

	private static boolean contains(Constructor<?>[] ms, Constructor<?> m) {
		if (isPrivate(m))
			return true;
		for (Constructor<?> m2 : ms)
			if (equalConstructors(m, m2))
				return true;
		return false;
	}

	private static boolean contains(Method[] ms, Method m) {
		if (isPrivate(m))
			return true;
		for (Method m2 : ms)
			if (equalMethods(m, m2))
				return true;
		return false;
	}

	private static boolean contains(Field[] fs, Field f) {
		if (isPrivate(f))
			return true;
		for (Field f2 : fs)
			if (equalFields(f, f2))
				return true;
		return false;
	}

	public void testEqualClasses(Class<?> user, Class<?> ref) {
		initPublishedTest(user, "checking the structure of the class");
		if (!Modifier.isPublic(user.getModifiers())) {
			error("Class " + user.getSimpleName() + " should be public");
			return;
		}

		Constructor<?>[] cU = user.getDeclaredConstructors();
		Constructor<?>[] cR = ref.getDeclaredConstructors();
		if (!isLowerBound(ref))
			for (Constructor<?> c : cU)
				if (!contains(cR, c))
					error("Constructor " + descr(c)
							+ " should be private, or should not exist, or just has a wrong signature");

		for (Constructor<?> c : cR)
			if (!isOptional(c) && !contains(cU, c))
				error("Constructor " + descr(c, user)
						+ " is missing (or has wrong signature)");

		Method[] mU = user.getDeclaredMethods();
		Method[] mR = ref.getDeclaredMethods();
		if (!isLowerBound(ref))
			for (Method m : mU) {
				if (m.getName().startsWith("access$"))
					continue;
				if (!contains(mR, m))
					error("Method " + descr(m)
							+ " should be private, or should not exist, or just has a wrong signature");
			}
		for (Method m : mR)
			if (!isOptional(m) && !contains(mU, m))
				error("Method " + descr(m)
						+ " is missing (or has wrong signature)");

		Field[] fU = user.getFields();
		Field[] fR = ref.getFields();
		if (!isLowerBound(ref))
			for (Field f : fU)
				if (!contains(fR, f))
					error("Field " + descr(f)
							+ " should be private, or should not exist, or just has a wrong name");
		for (Field f : fR)
			if (!contains(fU, f))
				error("Field " + descr(f)
						+ " is missing (or has wrong signature)");

		if (user.getSuperclass() != ref.getSuperclass())
			error("should be a direct subclass of "
					+ ref.getSuperclass().getSimpleName());

		if (!new HashSet<>(Arrays.asList(user.getInterfaces()))
				.equals(new HashSet<>(Arrays.asList(ref.getInterfaces()))))
			error("something wrong with implemented interfaces.");
	}

	// ---------------------------------------------------------------------------------
	// Zip stuff
	// ---------------------------------------------------------------------------------

	public static void zipIt(String zipFileName) throws IOException {
		String sourceFile = "src";
		FileOutputStream fos = new FileOutputStream(zipFileName);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		File fileToZip = new File(sourceFile);

		zipFile(fileToZip, fileToZip.getName(), zipOut);
		zipOut.close();
		fos.close();
	}

	private static void zipFile(File fileToZip, String fileName,
			ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			} else {
				zipOut.putNextEntry(new ZipEntry(fileName + "/"));
				zipOut.closeEntry();
			}
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(),
						zipOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}

	public static boolean isOneStudent() {
		ExDetails s1 = ExDetails.firstStudent();
		ExDetails s2 = ExDetails.secondStudent();
		return s2 == null || s1.equals(s2);
	}

	public static void generateZip() {
		ExDetails s1 = ExDetails.firstStudent();
		ExDetails s2 = ExDetails.secondStudent();
		String zipName;

		System.out.println("Check your personal details are correct here:");
		System.out.println();
		System.out.println(s1);
		if (isOneStudent()) {
			zipName = String.format("%s.zip", s1.getId());
		} else {
			System.out.println(s2);
			zipName = String.format("%s_%s.zip", s1.getId(), s2.getId());
		}
		System.out.println("--------------------------------------");

		try {
			zipIt(zipName);
			System.out.println(
					"Your zip file was created automatically!, you can find it in the project directory.");
			System.out.println("Its name is " + zipName);
			System.out.println(
					"Submit this file only! don't create your own zip!");
		} catch (IOException e) {
			System.err.println("Problem creating zip file! " + e);
		}
	}

	// ---------------------------------------------------------------------------------
	// Signature stuff
	// ---------------------------------------------------------------------------------

	private List<String> readFile() {
		String fileName = "src/" + this.getClass().getPackage().getName() + "/"
				+ this.getClass().getSimpleName() + ".java";
		List<String> lines = new ArrayList<>();

		try (Stream<String> stream = Files.lines(Paths.get(fileName),
				Charset.forName("UTF-8"))) {
			lines = stream.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return lines;
	}

	private static final int MOD = Integer.MAX_VALUE / 100;

	private int myHash(String s) {
		int hash = 0;
		for (int i = 0; i < s.length(); i++)
			if (!Character.isWhitespace(Character.codePointAt(s, i)))
				hash = (hash * 31 + Character.codePointAt(s, i)) % MOD;
		return hash;
	}

	private int signature(List<String> lines) {
		int sum = 0;
		for (String line : lines)
			sum = (sum * 7 + myHash(line)) % MOD;
		return sum;
	}

	public boolean checkSignature() {
		List<String> lines = readFile();
		int hardcoded = 0;
		try {
			hardcoded = Integer.valueOf(lines.get(0).substring(3));
		} catch (Exception e) {
			System.err.format(
					"You have probably changed %s.java."
							+ " Please download it again.%n",
					this.getClass().getSimpleName());
			return false;
		}
		lines.remove(0);
		int signature = signature(lines);
		if (hardcoded != signature) {
			System.err.format(
					"You have probably changed %s.java (%d)."
							+ " Please download it again.%n",
					this.getClass().getSimpleName(), signature);
			return false;
		}
		return true;
	}

	public boolean checkPackage(String packageName) {
		String real = this.getClass().getPackage().getName();
		if (!real.equals(packageName)) {
			System.err.format(
					"File %s.java should be in package %s and not %s%n",
					this.getClass().getSimpleName(), packageName, real);
			return false;
		}
		return true;
	}

	// ---------------------------------------------------------------------------------
	// used by specific Testers
	// ---------------------------------------------------------------------------------

	public void myTests() throws Exception {
	}

	public void myMain(String packageName) {

		if (!checkPackage(packageName))
			return;
		try {
			myTests();
			done();
			checkSignature();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ---------------------------------------------------------------------------------
	// main - for generating zip file.
	// ---------------------------------------------------------------------------------

	public static void main(String[] args) {
		Tester t = new Tester();
		if (t.checkPackage("util") && t.checkSignature())
			generateZip();
		else
			t.print("Zip not generated.");
	}

}
