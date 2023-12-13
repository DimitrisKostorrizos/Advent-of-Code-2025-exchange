import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2023/day/11
 */
public class Y23Day11 {

	/*
	 * Example:
	 * 
	 * ...#......
	 * .......#..
	 * #.........
	 * ..........
	 * ......#...
	 * .#........
	 * .........#
	 * ..........
	 * .......#..
	 * #...#.....
	 * 
	 */

	private static final String INPUT_RX = "^([.#]+)$";
	
	public static record InputData(String row) {
		@Override public String toString() { return row; }
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
			while (line.length() == 0) {
				line = scanner.nextLine();
			}
			if (line.matches(INPUT_RX)) {
				String row = line.replaceFirst(INPUT_RX, "$1");
				return new InputData(row);
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}

	
	static record Pos(long x, long y) {
		@Override public String toString() {
			return "("+x+","+y+")";
		}

		public long manhattenDist(Pos other) {
			return Math.abs(x-other.x) + Math.abs(y-other.y);
		}
	}
	
	public static class World {
		List<Pos> galaxies;
		int maxX;
		int maxY;
		public World() {
			this.galaxies = new ArrayList<>();
			this.maxX = 0;
			this.maxY = 0;
		}
		public void addRow(String row) {
			maxX = row.length();
			int x = row.indexOf('#');
			while (x != -1) {
				galaxies.add(new Pos(x, maxY));
				x = row.indexOf('#', x+1);
			}
			maxY++;
		}
		public void expand() {
			expand(2);
		}
		public void expand(long age) {
			int[] xs = new int[maxX];
			int[] ys = new int[maxY];
			for (Pos galaxy:galaxies) {
				xs[(int)galaxy.x]++;  
				ys[(int)galaxy.y]++;  
			}
			long[] xExpansion = new long[maxX];
			long offsetX = 0;
			for (int x=0; x<maxX; x++) {
				xExpansion[x] = x+offsetX;
				if (xs[x] == 0) {
					offsetX += age-1;
				}
			}
			long[] yExpansion = new long[maxY];
			long offsetY = 0;
			for (int y=0; y<maxY; y++) {
				yExpansion[y] = y+offsetY;
				if (ys[y] == 0) {
					offsetY += age-1;
				}
			}
			List<Pos> expandedGalaxies = new ArrayList<>();
			for (Pos galaxy:galaxies) {
				expandedGalaxies.add(new Pos(xExpansion[(int)galaxy.x], yExpansion[(int)galaxy.y]));
			}
			galaxies = expandedGalaxies;
		}
		public long calcSumGalaxyDists() {
			long result = 0;
			for (int i=0; i<galaxies.size()-1; i++) {
				Pos galaxy1 = galaxies.get(i);
				for (int j=i+1; j<galaxies.size(); j++) {
					Pos galaxy2 = galaxies.get(j);
					result += galaxy1.manhattenDist(galaxy2);
				}
			}
			return result;
		}

		@Override
		public String toString() {
			return galaxies.toString();
		}
	}
	
	public static void mainPart1(String inputFile) {
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
//			System.out.println(data);
			world.addRow(data.row);
		}
		System.out.println(world);
		world.expand();
		System.out.println(world);
		System.out.println(world.calcSumGalaxyDists());
	}
	
	
	public static void mainPart2(String inputFile, long age) {
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
//			System.out.println(data);
			world.addRow(data.row);
		}
		System.out.println(world);
		world.expand(age);
		System.out.println(world);
		System.out.println(world.calcSumGalaxyDists());
	}

	

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day11/Feri/input-example.txt");
		mainPart1("exercises/day11/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
//		mainPart2("exercises/day11/Feri/input-example.txt", 10);
//		mainPart2("exercises/day11/Feri/input-example.txt", 100);
		mainPart2("exercises/day11/Feri/input.txt", 1000000L);              
		System.out.println("---------------");    //
	}
	
}
