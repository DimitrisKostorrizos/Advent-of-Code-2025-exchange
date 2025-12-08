import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2025/day/07
 */
public class Y25Day07WithGUI {

	
	static Y25GUIOutput07 output;

	
	public static record InputData(String row) {
	}

	private static final String INPUT_RX = "^([.S^]+)$";
	
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
			String rawLine = scanner.nextLine();
			String line = rawLine.trim();
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
	
	public static record Pos(int row, int col) {
		@Override
		public final String toString() {
			return "("+col+","+row+")";
		}
	}

	
	public static class World {
		List<char[]> matrix;
		int numRows;
		int numCols;
		Pos startPos;;
		public World() {
			matrix = new ArrayList<>();
			numRows = 0;
			numCols = 0;
			startPos = null;
		}
		public void addRow(String row) {
			int sCol = row.indexOf('S');
			if (sCol >= 0) {
				startPos = new Pos(numRows, sCol);
			}
			matrix.add(row.toCharArray());
			numRows++;
			numCols = row.length();
		}
		public char get(int row, int col) {
			return matrix.get(row)[col];
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int row = 0; row<numRows; row++) {
				for (int col = 0; col<numCols; col++) {
					sb.append(get(row,col));
				}
				sb.append('\n');
			}
			return sb.toString();
		}
		public int calcSplitBeams() {
			int result = 0;
			Set<Pos> allBeamPositions = new HashSet<>();
			showWorldWithBeams(allBeamPositions);
			Set<Pos> beamPositions = new HashSet<>();
			beamPositions.add(startPos);
			for (int row = startPos.row+1; row<numRows; row++) {
				Set<Pos> nextBeamPositions = new HashSet<>();
				for (Pos beamPos:beamPositions) {
					if (get(row, beamPos.col) == '^') {
						result++;
						nextBeamPositions.add(new Pos(row, beamPos.col-1));
						nextBeamPositions.add(new Pos(row, beamPos.col+1));
					}
					else {
						nextBeamPositions.add(new Pos(row, beamPos.col));
					}
				}
				beamPositions = nextBeamPositions;
				allBeamPositions.addAll(beamPositions);
				showWorldWithBeams(allBeamPositions);
			}
			return result;
		}
		private void showWorldWithBeams(Set<Pos> allBeamPositions) {
			StringBuilder sb = new StringBuilder();
			for (int row = 0; row<numRows; row++) {
				for (int col = 0; col<numCols; col++) {
					if (allBeamPositions.contains(new Pos(row, col))) {
						sb.append('|');
					}
					else {
						sb.append(get(row,col));
					}
				}
				sb.append('\n');
			}
			output.addStep(sb.toString());
		}
		public long calcQuantumSplitBeams() {
			Map<Pos, Long> beamPositions = new HashMap<>();
			beamPositions.put(startPos, 1L);
			for (int row = startPos.row+1; row<numRows; row++) {
				System.out.println("row "+row+"/"+numRows+": beam positions: "+beamPositions.values().stream().reduce(0L, (sum,n)->sum+n));
				Map<Pos, Long> nextBeamPositions = new HashMap<>();
				for (Pos beamPos:beamPositions.keySet()) {
					long count = beamPositions.get(beamPos);
					if (get(row, beamPos.col) == '^') {
						Long leftCount = nextBeamPositions.getOrDefault(new Pos(row, beamPos.col-1), 0L);
						nextBeamPositions.put(new Pos(row, beamPos.col-1), count + leftCount);
						Long rightCount = nextBeamPositions.getOrDefault(new Pos(row, beamPos.col+1), 0L);
						nextBeamPositions.put(new Pos(row, beamPos.col+1), count + rightCount);
					}
					else {
						Long colCount = nextBeamPositions.getOrDefault(new Pos(row, beamPos.col), 0L);
						nextBeamPositions.put(new Pos(row, beamPos.col), count + colCount);
					}
				}
				beamPositions = nextBeamPositions;
			}
			long result = 0L;
			for (Long count:beamPositions.values()) {
				result += count;
			}
			return result;
		}
	}

	
	public static void mainPart1(String inputFile) throws FileNotFoundException {
		output = new Y25GUIOutput07("2025 Day 07 Part 1", true);
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			world.addRow(data.row);
		}
		System.out.println(world.toString());
		int splitBeams = world.calcSplitBeams();
		System.out.println("split beams: "+splitBeams);
	}


	public static void mainPart2(String inputFile) {
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			world.addRow(data.row);
		}
		System.out.println(world.toString());
		long quantumSplitBeams = world.calcQuantumSplitBeams();
		System.out.println("quantum split beams: "+quantumSplitBeams);
	}


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
		mainPart1("exercises/day07/Feri/input-example.txt");
//		mainPart1("exercises/day07/Feri/input.txt");
		System.out.println("---------------");
		System.out.println("--- PART II ---");
//		mainPart2("exercises/day07/Feri/input-example.txt");
//		mainPart2("exercises/day07/Feri/input.txt");
		System.out.println("---------------");    // 
	}
	
}
