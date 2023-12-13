import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2023/day/10
 */
public class Y23Day10 {

	static Y23GUIOutput10 output;
	
	
	/*
	 * Example:
	 * 
	 * 7-F7-
	 * .FJ|7
	 * SJLL7
	 * |F--J
	 * LJ.LJ
	 * 
	 */

	private static final String INPUT_RX = "^([-F7LJ|.S]+)$";
	
	public static record InputData(String row) {}
	
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
	
	static int DIR_ROT_LEFT = -1;
	static int DIR_ROT_RIGHT = 1;
	
	static int rot(int dir, int rot) { return (dir+rot+4)%4; }
	
	static String[] NEXT_DIR_CHAR   = {  // "<LEFT><STRAIGHT><RIGHT>"
			// >   
			"J-7",
			// v
			"L|J",
			// <
			"F-L",
			// ^
			"7|F"
		};
	static int nextDir(int currentDir, char road) {
		int rotPos = NEXT_DIR_CHAR[currentDir].indexOf(road);
		if (rotPos == -1) {
			return -1;
		}
		return (currentDir+rotPos+3)%4;
	}
	
	
	static record Pos(int x, int y) {
		Pos move(int dir) {
			return new Pos(x+DIR_ADD_X[dir], y+DIR_ADD_Y[dir]);
		}
		@Override public String toString() {
			return "("+x+","+y+")";
		}
		public List<Y23Day10.Pos> neighbours() {
			List<Pos> result = new ArrayList<>();
			result.add(move(DIR_EAST));
			result.add(move(DIR_SOUTH));
			result.add(move(DIR_WEST));
			result.add(move(DIR_NORTH));
			return result;
		}
	}
	
	public static class World {
		List<String> field;
		int maxX;
		int maxY;
		Pos startPos;
		Pos previousStartPos;
		int startDir;
		Pos currentPos;
		Pos previousPos;
		Pos check1Pos;
		Pos check2Pos;
		Set<Pos> path;
		Set<Pos> fill;
		int dir;
		int ticks;
		public World() {
			this.field = new ArrayList<>();
			this.ticks = 0;
			this.path = new LinkedHashSet<>();
			this.fill = Collections.emptySet();
		}
		public char getRoad(Pos pos) {
			return getRoad(pos.x, pos.y);
		}
		public char getRoad(int x, int y) {
			if ((x<0) || (x>=maxX) || (y<0) || (y>=maxY)) {
				return '.';
			}
			return field.get(y).charAt(x);
		}
		public void addRow(String row) {
			field.add(row);
			maxY = field.size();
			if (row.contains("S")) {
				int x = row.indexOf('S');
				startPos = new Pos(x, maxY-1);
				currentPos = startPos;
				previousPos = currentPos;
				maxX = row.length();
			}
		}
		public void initStartDir() {
			for (dir = DIR_EAST; dir<=DIR_NORTH; dir++) {
				char road = getRoad(startPos.move(dir));
				if (nextDir(dir, road) != -1) {
					startDir = dir;
					break;
				}
			}
			for (dir = DIR_EAST; dir<=DIR_NORTH; dir++) {
				if (dir == startDir) {
					continue;
				}
				char road = getRoad(startPos.move(dir));
				if (nextDir(dir, road) != -1) {
					int previousDir = dir;
					previousStartPos = startPos.move(previousDir);
					previousPos = previousStartPos;
					break;
				}
			}
			path.add(startPos);
		}
		
		public boolean reachedStart() {
			return getRoad(currentPos) == 'S';
		}
		public boolean tick() {
			ticks++;
//			System.out.println("MOVE "+currentPos+" "+DIRS.charAt(dir));
			previousPos = currentPos;
			currentPos = currentPos.move(dir);
			path.add(currentPos);
			char road = getRoad(currentPos);
			dir = nextDir(dir, road);
			return dir != -1;
		}
		public int countInnerFields(int fillRot) {
			fill = new LinkedHashSet<>(path);
			dir = startDir;
			currentPos = startPos;
			previousPos = previousStartPos;
			do {
				int fillDir = rot(dir, fillRot);
				Pos fillPos = currentPos.move(fillDir);
//				System.out.println("AT "+currentPos+" "+getRoad(currentPos)+" "+DIRS.charAt(dir)+" CHECKING "+fillPos);
				check1Pos = fillPos;
				check2Pos = fillPos;
				if (!fill.contains(fillPos)) {
					System.out.println("ADDING "+fillPos);
					if (!fillField(fillPos)) {
						return -1;
					}
				}
				if (!fillPos.equals(previousPos)) {
					int fillDir2 = rot(fillDir, fillRot);
					Pos fillPos2 = currentPos.move(fillDir2);
//					System.out.println("AT "+currentPos+" "+getRoad(currentPos)+" "+DIRS.charAt(dir)+" CHECKING2 "+fillPos2);
					check2Pos = fillPos;
					if (!fill.contains(fillPos2)) {
//						System.out.println("ADDING "+fillPos2);
						if (!fillField(fillPos2)) {
							return -1;
						}
					}
				}
				show();
			} while (tick());
			return fill.size() - path.size();
		}
		private boolean fillField(Pos fillPos) {
			if (fill.contains(fillPos)) {
				return true;
			}
			if ((fillPos.x<-3) || (fillPos.y<-3) || (fillPos.x>maxX+2) || (fillPos.y>maxY+2)) {
				return false;
			}
//			System.out.println("  ADD "+fillPos);
			fill.add(fillPos);
			List<Pos> neighbourPositions = fillPos.neighbours();
			for (Pos neighbourPos:neighbourPositions) {
				if (!fillField(neighbourPos)) {
					return false;
				}
			}
			return true;
		}
		public void show() {
			StringBuilder result = new StringBuilder();
			String lastColor = "b0";
			for (int y=-2; y<maxY+2; y++) {
				for (int x=-2; x<maxX+2; x++) {
					char c = getRoad(x, y);
					String color = "b0";
					Pos pos = new Pos(x,y);
					if (fill.contains(pos)) {
						color = "bred";
					}
					if (path.contains(pos)) {
						color = "byellow";
					}
					if (pos.equals(check1Pos) || pos.equals(check2Pos)) {
						if (!fill.contains(pos) || path.contains(pos)) {
							color = "borange";
						}
					}
					if (pos.equals(currentPos) || pos.equals(previousPos)) {
						color = "bblack";
					}
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
		public void framePath() {
			for (int x=-1; x<=maxX+1; x++) {
				path.add(new Pos(x, -1));
				path.add(new Pos(x, 0));
				path.add(new Pos(x, maxY));
				path.add(new Pos(x, maxY+1));
			}
			for (int y=-1; y<=maxY+1; y++) {
				path.add(new Pos(-1, y));
				path.add(new Pos(0, y));
				path.add(new Pos(maxX, y));
				path.add(new Pos(maxX+1, y));
			}
		}
	}
	
	public static void mainPart1(String inputFile) {
		output = new Y23GUIOutput10("2023 Day 10 Part I", true);
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			world.addRow(data.row);
		}
		world.initStartDir();
		world.tick();
		while (!world.reachedStart()) {
			world.show();
			world.tick();
		}
		world.show();
		System.out.println("ticks="+world.ticks);
		System.out.println("MAX_DIST: "+world.ticks/2);
	}
	
	
	public static void mainPart2(String inputFile) {
		output = new Y23GUIOutput10("2023 Day 10 Part II", true);
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			world.addRow(data.row);
		}
		world.initStartDir();
		world.tick();
		while (!world.reachedStart()) {
			world.tick();
		}
		int cnt;

//		if (false) {
//			world.framePath();
//			cnt = world.countInnerFields(DIR_ROT_RIGHT);
//			world.show();
//			System.out.println(cnt);
//			return;
//		}

		
		cnt = world.countInnerFields(DIR_ROT_LEFT);
		System.out.println("CNT: "+cnt);
		world.show();
		if (cnt == -1) {
			System.out.println("LEFT RETURNED -1, TRYING RIGHT");
			cnt = world.countInnerFields(DIR_ROT_RIGHT);
			world.show();
		}
		System.out.println("INNER FIELDS: "+cnt);
	}

	

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
		mainPart1("exercises/day10/Feri/input-example.txt");
//		mainPart1("exercises/day10/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
		mainPart2("exercises/day10/Feri/input-example-2.txt");
//		mainPart2("exercises/day10/Feri/input-example-3.txt");
//		mainPart2("exercises/day10/Feri/input-example-4.txt");
//		mainPart2("exercises/day10/Feri/input.txt");                      
		System.out.println("---------------");    //
	}
	
}
