import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2023/day/17
 */
public class Y23Day17 {

	static Y23GUIOutput17 output;

	/*
	 * Example:
	 * 
	 * 2413432311323
	 * 3215453535623
	 * 3255245654254
	 * 3446585845452
	 * 4546657867536
	 * 1438598798454
	 * 4457876987766
	 * 3637877979653
	 * 4654967986887
	 * 4564679986453
	 * 1224686865563
	 * 2546548887735
	 * 4322674655533
	 * 
	 */

	private static final String INPUT_RX = "^([0-9]*)$";
	
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

	static String DIRS            = ">v<^";
	static int[]  DIR_ADD_X 	  = { 1,   0,  -1,   0};
	static int[]  DIR_ADD_Y 	  = { 0,   1,   0,  -1};
	
	static int DIR_EAST = 0;
	static int DIR_SOUTH = 1;
	static int DIR_WEST = 2;
	static int DIR_NORTH = 3;
	
	static int DIR_ROT_LEFT = 3;
	static int DIR_ROT_RIGHT = 1;

	static int rot(int dir, int rot) { return (dir+rot+4)%4; }


	static final int Z_HORIZONTAL = 0;
	static final int Z_VERTICAL = 1;

	static record State(int sumHeatLoss, Pos pos) {
		@Override public String toString() { return "M["+pos+"|"+sumHeatLoss+"]"; }
	}
	
	static record Pos(int x, int y, int z) {
		@Override public String toString() { return "("+x+","+y+","+z+")"; }
	}
	
	static record Move(Pos targetPos, int heatloss) {
		@Override public String toString() { return "M["+targetPos+"|"+heatloss+"]"; }
	}
	
	public static class World {
		Map<Pos, List<Move>> heatlossDirections;
		Map<Pos, Integer> minimalHeatLoss;
		PriorityQueue<State> searchPaths;
		List<String> rows;
		int[][] field;
		int maxX;
		int maxY;
		int ticks;
		public World() {
			this.rows = new ArrayList<>();
		}
		public void addRow(String row) {
			rows.add(row);
		}
		public void init() {
			ticks = 0;
			maxY = rows.size();
			maxX = rows.get(0).length();
			field = new int[maxY][maxX];
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					field[y][x] = rows.get(y).charAt(x)-'0';
				}
			}
		}
		private int get(int x, int y) {
			if ((x<0) || (y<0) || (x>=maxX) || (y>=maxY)) {
				return '?';
			}
			return field[y][x];
		}		
		public void tick() {
			ticks++;
		}
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					result.append((char) ('0'+get(x,y)));
				}
				result.append("\n");
			}
			return result.toString();
		}
		public void show() {
			StringBuilder result = new StringBuilder();
			String lastColor = "b0";
			for (int y=-1; y<=maxY; y++) {
				for (int x=-1; x<=maxX; x++) {
					char c = (char) ('0'+get(x, y));
					String color = "b0";
//						color = "byellow";
//						color = "bred";
					if (!lastColor.equals(color)) {
						lastColor = color;
						result.append(output.style(color));
					}
					result.append(c);
				}
				result.append("\n");
			}
			output.addStep(result.toString());
		}
		boolean isValidPos(int x, int y) {
			return (x>=0)&&(x<maxX)&&(y>=0)&&(y<maxY);
		}
		private List<Move> createHorizontalMoves(int x, int y) {
			List<Move> result = new ArrayList<>();
			int heatLossForward = 0;
			int heatLossBackward = 0;
			for (int dx=1; dx<=3; dx++) {
				if (isValidPos(x+dx, y)) {
					Pos targetPos = new Pos(x+dx, y, Z_VERTICAL);
					heatLossForward += get(x+dx, y);
					result.add(new Move(targetPos, heatLossForward));
				}
				if (isValidPos(x-dx, y)) {
					Pos targetPos = new Pos(x-dx, y, Z_VERTICAL);
					heatLossBackward += get(x-dx, y);
					result.add(new Move(targetPos, heatLossBackward));
				}
			}
			return result;
		}
		private List<Move> createVerticalMoves(int x, int y) {
			List<Move> result = new ArrayList<>();
			int heatLossForward = 0;
			int heatLossBackward = 0;
			for (int dy=1; dy<=3; dy++) {
				if (isValidPos(x, y+dy)) {
					Pos targetPos = new Pos(x, y+dy, Z_HORIZONTAL);
					heatLossForward += get(x, y+dy);
					result.add(new Move(targetPos, heatLossForward));
				}
				if (isValidPos(x, y-dy)) {
					Pos targetPos = new Pos(x, y-dy, Z_HORIZONTAL);
					heatLossBackward += get(x, y-dy);
					result.add(new Move(targetPos, heatLossBackward));
				}
			}
			return result;
		}
		public void createHeatlossDirectionGraph() {
			heatlossDirections = new LinkedHashMap<>();
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					heatlossDirections.put(new Pos(x,y,Z_HORIZONTAL), createHorizontalMoves(x,y));
					heatlossDirections.put(new Pos(x,y,Z_VERTICAL), createVerticalMoves(x,y));
				}
			}
		}
		private List<Move> createUltraHorizontalMoves(int x, int y) {
			List<Move> result = new ArrayList<>();
			int heatLossForward = 0;
			int heatLossBackward = 0;
			for (int dx=1; dx<=3; dx++) {
				if (isValidPos(x+dx, y)) {
					heatLossForward += get(x+dx, y);
				}
				if (isValidPos(x-dx, y)) {
					heatLossBackward += get(x-dx, y);
				}
			}
			for (int dx=4; dx<=10; dx++) {
				if (isValidPos(x+dx, y)) {
					Pos targetPos = new Pos(x+dx, y, Z_VERTICAL);
					heatLossForward += get(x+dx, y);
					result.add(new Move(targetPos, heatLossForward));
				}
				if (isValidPos(x-dx, y)) {
					Pos targetPos = new Pos(x-dx, y, Z_VERTICAL);
					heatLossBackward += get(x-dx, y);
					result.add(new Move(targetPos, heatLossBackward));
				}
			}
			return result;
		}
		private List<Move> createUltraVerticalMoves(int x, int y) {
			List<Move> result = new ArrayList<>();
			int heatLossForward = 0;
			int heatLossBackward = 0;
			for (int dy=1; dy<=3; dy++) {
				if (isValidPos(x, y+dy)) {
					heatLossForward += get(x, y+dy);
				}
				if (isValidPos(x, y-dy)) {
					heatLossBackward += get(x, y-dy);
				}
			}
			for (int dy=4; dy<=10; dy++) {
				if (isValidPos(x, y+dy)) {
					Pos targetPos = new Pos(x, y+dy, Z_HORIZONTAL);
					heatLossForward += get(x, y+dy);
					result.add(new Move(targetPos, heatLossForward));
				}
				if (isValidPos(x, y-dy)) {
					Pos targetPos = new Pos(x, y-dy, Z_HORIZONTAL);
					heatLossBackward += get(x, y-dy);
					result.add(new Move(targetPos, heatLossBackward));
				}
			}
			return result;
		}
		public void createUltraHeatlossDirectionGraph() {
			heatlossDirections = new LinkedHashMap<>();
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					heatlossDirections.put(new Pos(x,y,Z_HORIZONTAL), createUltraHorizontalMoves(x,y));
					heatlossDirections.put(new Pos(x,y,Z_VERTICAL), createUltraVerticalMoves(x,y));
				}
			}
		}
		public String showMinimalMoves() {
			StringBuilder result = new StringBuilder();
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					String minHLH = num3(minimalHeatLoss.get(new Pos(x,y,Z_HORIZONTAL)));
					String minHLV = num3(minimalHeatLoss.get(new Pos(x,y,Z_VERTICAL)));
					result.append(" "+minHLH+"/"+minHLV);
				}
				result.append("\n");
			}
			return result.toString();
		}
		private String num3(Integer n) {
			if (n==null) {
				return "***";
			}
			String result = Integer.toString(n);
			return "   ".substring(result.length(), 3)+result;
		}
		public int findMinimalHeatLoss() {
			minimalHeatLoss = new LinkedHashMap<>();
			searchPaths = new PriorityQueue<>((s1,s2)->Integer.compare(s1.sumHeatLoss,s2.sumHeatLoss));
			Pos startPosH = new Pos(0,0,Z_HORIZONTAL);
			Pos startPosV = new Pos(0,0,Z_VERTICAL);
			searchPaths.add(new State(0, startPosH));
			searchPaths.add(new State(0, startPosV));
			minimalHeatLoss.put(startPosH, 0);
			minimalHeatLoss.put(startPosH, 0);
			while (true) {
				State currentSearch = searchPaths.poll();
//				output.addStep(currentSearch+"\n"+showMinimalMoves());
//				System.out.println(currentSearch);
//				if (currentSearch.pos.toString().contains("2,1,0")) {
//					System.out.println("BREAK");
//				}
				if ((currentSearch.pos.x == maxX-1) && (currentSearch.pos.y == maxY-1)) {
					return currentSearch.sumHeatLoss;
				}
				List<Move> moves = heatlossDirections.get(currentSearch.pos);
				for (Move move:moves) {
					Integer minHL = minimalHeatLoss.get(move.targetPos);
					if ((minHL==null) || (minHL>currentSearch.sumHeatLoss+move.heatloss)) {
						minimalHeatLoss.put(move.targetPos, currentSearch.sumHeatLoss+move.heatloss);
						searchPaths.add(new State(currentSearch.sumHeatLoss+move.heatloss, move.targetPos));
					}
				}
			}
		}
	}

	public static void mainPart1(String inputFile) {
//		output = new Y23GUIOutput17("2023 day 17 Part I", true);
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			world.addRow(data.row);
		}
		world.init();
//		System.out.println(world);
		world.createHeatlossDirectionGraph();
		int heatLoss = world.findMinimalHeatLoss();
		System.out.println("MINIMAL HEATLOSS: "+heatLoss);
	}

	public static void mainPart2(String inputFile) {
//		output = new Y23GUIOutput17("2023 day 17 Part I", true);
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			world.addRow(data.row);
		}
		world.init();
//		System.out.println(world);
		world.createUltraHeatlossDirectionGraph();
		int heatLoss = world.findMinimalHeatLoss();
		System.out.println("MINIMAL HEATLOSS: "+heatLoss);		
	}


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day17/Feri/input-example.txt");
		mainPart1("exercises/day17/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
//		mainPart2("exercises/day17/Feri/input-example.txt");
//		mainPart2("exercises/day17/Feri/input-example-2.txt");
		mainPart2("exercises/day17/Feri/input.txt");
		System.out.println("---------------");    
	}
	
}
