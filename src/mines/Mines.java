package mines;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class Mines {

	private int height;
	private int width;
	private int numMines;
	private boolean showAll;

	private Point gameBoard[][];
	private Random rnd = new Random();

	public Mines(int height, int width, int numMines) {
		this.height = height;
		this.width = width;
		this.numMines = numMines;
		this.showAll = false;
		gameBoard = new Point[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				gameBoard[i][j] = new Point(i, j);
			}
		}
		int count = 0;
		while (count < this.numMines) {
			int x = rnd.nextInt(height);
			int y = rnd.nextInt(width);
			if (!gameBoard[x][y].isMine) {
				gameBoard[x][y].isMine = true;
				count++;
			}
		}

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				gameBoard[i][j].setNeighbours();
			}
		}

	}

	public boolean addMine(int i, int j) {
		if (gameBoard[i][j].isMine) {
			return false;
		}
		gameBoard[i][j].isMine = true;
		return true;
	}

	public boolean open(int i, int j) {

		// if its not mine return true.
		if (gameBoard[i][j].isMine) {
			return false;
		}
		gameBoard[i][j].isOpen = true;

		if (gameBoard[i][j].getCountOfMinesArround() == 0) {
			Set<Point> neighbours = gameBoard[i][j].neighbours;
			for (Point p : neighbours) {
				if (!p.isOpen) {
					if (!open(p.x, p.y)) {
						return true;
					}
				}
			}
		}
		return true;
	}

	public void toggleFlag(int x, int y) {
		gameBoard[x][y].isFlag = !gameBoard[x][y].isFlag;
	}

	public boolean isDone() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (!gameBoard[i][j].isMine && !gameBoard[i][j].isOpen) {
					return false;
				}
			}
		}
		return true;
	}

	public String get(int i, int j) {
		return gameBoard[i][j].toString();
	}

	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}

	public String toString() {
		StringBuilder mine = new StringBuilder();
		boolean flag = false;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				String str = gameBoard[i][j].toString();
				if (!str.equals("")) {
					mine.append(str);
					flag = true;
				}
			}
			if (flag) {
				mine.append("\n");
			}
			flag = false;
		}
		return mine.toString();

	}

	// ************** inner class point **************//

	public Point[][] getBoard() {
		return gameBoard;
	}

	public class Point {

		private boolean isOpen;
		private boolean isMine;
		private boolean isFlag;
		private int x, y;
		private Set<Point> neighbours;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
			isOpen = false;
			isMine = false;
			isFlag = false;
			neighbours = new HashSet<>();
		}

		public boolean isOpen() {
			return isOpen;
		}

		public boolean isMine() {
			return isMine;
		}

		public boolean isFlag() {
			return isFlag;
		}

		public int getCountOfMinesArround() {
			Iterator<Point> it = neighbours.iterator();
			int count = 0;
			while (it.hasNext()) {
				if (it.next().isMine) {
					count++;
				}
			}
			return count;
		}

		public void setNeighbours() {
			// add all neighbours to set.
			for (int i = x - 1; i <= x + 1; i++) {
				for (int j = y - 1; j <= y + 1; j++) {
					if (i == x && j == y) {
						continue;
					}
					if (isInside(i, j)) {
						neighbours.add(gameBoard[i][j]);
					}
				}
			}
		}

		public boolean isInside(int i, int j) {
			// check if the point is inside the board.
			if (i < 0 || i >= height || j < 0 || j >= width) {
				return false;
			}
			return true;
		}

		public String toString() {
			if (isOpen || showAll) {
				if (isMine) {
					return "X";
				} else {
					if (getCountOfMinesArround() == 0) {
						return " ";
					}
					return "" + getCountOfMinesArround();
				}
			} else {
				if (isFlag) {
					return "F";
				} else {
					return ".";
				}
			}
		}
	}
}
