package util;

public class ExDetails {
	private String id;
	private String firstName;
	private String lastName;
	private String eMail;

	private static boolean isValidId(String id) {
		id = id.trim();
		if (id.length() != 8 && id.length() != 9)
			return false;
		for (int i=0; i < id.length(); i++)
			if (!Character.isDigit(id.charAt(i)))
				return false;
		int mult = 1;
		long result = 0;
		for (int i=id.length() - 1; i >= 0; i--) {
			int digit = (id.charAt(i) - '0') * mult;
			result += digit % 10;
			result += digit / 10;
			mult = mult == 2 ? 1 : 2;
		}
		return result % 10 == 0;
	}

	public ExDetails(String id, String firstName, String lastName, String eMail) {
		if (!isValidId(id)) 
			throw new RuntimeException(id + " is not a valid ID");
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.eMail = eMail;
	}

	public String getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String geteMail() {
		return eMail;
	}

	public String toString() {
		return String.format("%s : %s %s, Email:%s", id, firstName, lastName,
				eMail);
	}
	
	@Override 
	public boolean equals(Object o) {
		return id.equals(((ExDetails)o).id);
	}

	// Put your details here (in English!)
	public static ExDetails firstStudent() {
		return new ExDetails("203334743", "Daniel", "Gabbay", "d9777834@gmail.com");
	}

	// Put the second student details here, or return null if there is only one student.
	public static ExDetails secondStudent() {
		return new ExDetails("203334743", "Daniel", "Gabbay", "d9777834@gmail.com");
	}
}
