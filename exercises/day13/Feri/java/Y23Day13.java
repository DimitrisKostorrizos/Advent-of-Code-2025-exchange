import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2023/day/13
 */
public class Y23Day13 {

	/*
	 * Example:
	 * 
	 * #.##..##.
	 * ..#.##.#.
	 * ##......#
	 * ##......#
	 * ..#.##.#.
	 * ..##..##.
	 * #.#.##.#.
	 * 
	 * #...##..#
	 * #....#..#
	 * ..##..###
	 * #####.##.
	 * #####.##.
	 * ..##..###
	 * #....#..#
	 * 
	 */

	private static final String INPUT_RX = "^([.#]*)$";
	
	public static record InputData(String row) {
		@Override public String toString() {
			return row;
		}
	}
	
	public static class InputProcessor implements Iterable<InputData>, Iterator<InputData> {
		private Scanner scanner;
		public InputProcessor(String inputFile) {
			try {
				scanner = new Scanner(new File(inputFile));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		@Override public Iterator<InputData> iterator() { return this; }
		@Override public boolean hasNext() { return scanner.hasNext(); }
		@Override public InputData next() {
			String line = scanner.nextLine().trim();
//			while (line.length() == 0) {
//				line = scanner.nextLine();
//			}
			if (line.matches(INPUT_RX)) {
				String row = line.replaceFirst(INPUT_RX, "$1");
				return new InputData(row);
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}

	
	public static class World {
		List<String> field;
		int maxX;
		int maxY;
		int toggleX;
		int toggleY;
		public World() {
			field = new ArrayList<>();
			toggleX = -1;
			toggleY = -1;

		}
		public boolean addRow(String row) {
			if (row.isEmpty()) {
				maxY = field.size();
				maxX = field.get(0).length();
				return true;
			}
			field.add(row);
			return false;
		}
		public String toString() {
			StringBuilder result = new StringBuilder();
			for (String row:field) {
				result.append(row).append("\n");
			}
			return result.toString();
		}
		public String toTString() {
			StringBuilder result = new StringBuilder();
			for (int x=0; x<maxX; x++) {
				result.append(getColumn(x)).append("\n");
			}
			return result.toString();
		}
		char get(int x, int y) {
			char c = field.get(y).charAt(x);
			if ((x == toggleX) && (y == toggleY)) {
				c = (c=='#') ? '.' : '#';
			}
			return c; 
		}

		String getRow(int row) {
			StringBuilder result = new StringBuilder();
			for (int x=0; x<maxX; x++) {
				result.append(get(x,row));
			}
			return result.toString();
		}
		
		String getColumn(int col) {
			StringBuilder result = new StringBuilder();
			for (int y=0; y<maxY; y++) {
				result.append(get(col,y));
			}
			return result.toString();
		}

		private boolean columnsEqual(int col1, int col2) {
			return getColumn(col1).equals(getColumn(col2));
		}
		private boolean rowsEqual(int row1, int row2) {
			return getRow(row1).equals(getRow(row2));
		}

		private int findMirrorVLine() {
			return findMirrorVLine(-1);
		}
		private int findMirrorVLine(int ignore) {
			for (int mirrorCol=1; mirrorCol<maxX; mirrorCol++) {
				boolean ok = mirrorCol != ignore;
				for (int checkDist=0; checkDist<maxX-mirrorCol; checkDist++) {
					if (mirrorCol-1-checkDist<0) {
						break;
					}
					if (!columnsEqual(mirrorCol+checkDist, mirrorCol-1-checkDist)) {
						ok = false;
						break;
					}
				}
				if (ok) {
					System.out.println("MIRROR between COLUMNS "+(mirrorCol)+" and "+(mirrorCol+1));
					return mirrorCol;
				}
			}
			return -1;
		}
		
		private int findMirrorHLine() {
			return findMirrorHLine(-1);
		}
		private int findMirrorHLine(int ignore) {
			for (int mirrorRow=1; mirrorRow<maxY; mirrorRow++) {
				boolean ok = mirrorRow != ignore;
				for (int checkDist=0; checkDist<maxY-mirrorRow; checkDist++) {
					if (mirrorRow-1-checkDist<0) {
						break;
					}
					if (!rowsEqual(mirrorRow+checkDist, mirrorRow-1-checkDist)) {
						ok = false;
						break;
					}
				}
				if (ok) {
					System.out.println("MIRROR between ROWS "+(mirrorRow)+" and "+(mirrorRow+1));
					return mirrorRow;
				}
			}
			return -1;
		}
		
		public int findMirrorLine() {
			int resultV = findMirrorVLine();
			int resultH = findMirrorHLine();
			if ((resultH!=-1) && (resultV!=-1)) {
				System.out.println("-- T --");
				System.out.println(toTString());
				throw new RuntimeException("two matches!");
			}
			if ((resultH==-1) && (resultV==-1)) {
				System.out.println("-- T --");
				System.out.println(toTString());
				throw new RuntimeException("no matches!");
			}
			if (resultH != -1) {
				return 100*resultH;
			}
			return resultV;
		}

		public int findUnsmudgedMirrorLine() {
			toggleX = -1;
			toggleY = -1;
			int skipResultV = findMirrorVLine();
			int skipResultH = findMirrorHLine();
			for (int y=0; y<maxY; y++) {
				toggleY = y;
				for (int x=0; x<maxX; x++) {
					toggleX = x;
					int resultV = findMirrorVLine(skipResultV);
					int resultH = findMirrorHLine(skipResultH);
					if ((resultH!=-1) && (resultV!=-1)) {
						System.out.println("-- T --");
						System.out.println(toTString());
						throw new RuntimeException("two matches!");
					}
					if (resultH!=-1) {
						return 100*resultH;
					}
					if (resultV!=-1) {
						return resultV;
					}
				}
			}
			System.out.println("-- T --");
			System.out.println(toTString());
			throw new RuntimeException("no smudge matches!");
		}

	}

	public static void mainPart1(String inputFile) {
		World world = new World();
		int sum = 0;
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			if (world.addRow(data.row)) {
				System.out.println("---");
				System.out.println(world.toString()); 
				sum += world.findMirrorLine();
				System.out.println("---");
				world = new World(); 
			}
		}
		System.out.println("---");
		world.addRow("");
		System.out.println(world.toString()); 
		sum += world.findMirrorLine();
		System.out.println("SUM: "+sum);
	}
	
	
	public static void mainPart2(String inputFile) {
		World world = new World();
		int sum = 0;
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			if (world.addRow(data.row)) {
				System.out.println("---");
				System.out.println(world.toString()); 
				sum += world.findUnsmudgedMirrorLine();
				System.out.println("---");
				world = new World(); 
			}
		}
		System.out.println("---");
		world.addRow("");
		System.out.println(world.toString()); 
		sum += world.findUnsmudgedMirrorLine();
		System.out.println("SUM: "+sum);
	}


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day13/Feri/input-example.txt");
		mainPart1("exercises/day13/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
//		mainPart2("exercises/day13/Feri/input-example.txt");
		mainPart2("exercises/day13/Feri/input.txt");              
		System.out.println("---------------");    //
	}
	
}
