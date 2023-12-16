import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * see: https://adventofcode.com/2023/day/16
 */
public class Y23Day16 {

	static Y23GUIOutput16 output;

	/*
	 * Example:
	 * 
	 * .|...\....
	 * |.-.\.....
	 * .....|-...
	 * ........|.
	 * ..........
	 * .........\
	 * ..../.\\..
	 * .-.-/..|..
	 * .|....-|.\
	 * ..//.|....
	 * 
	 */

	private static final String INPUT_RX = "^([-.|\\\\/]*)$";
	
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

	
	static record Pos(int x, int y) {
		Pos move(int dir) {
			return new Pos(x+DIR_ADD_X[dir], y+DIR_ADD_Y[dir]);
		}		
		@Override public String toString() { return "("+x+","+y+")"; }
	}
	
	static record Beam(Pos pos, int dir) {
		public Beam move() {
			return new Beam(pos.move(dir), dir);
		}
		@Override public String toString() { return "B["+pos+"|"+DIRS.charAt(dir)+"]"; }
	}
	
	public static class World {
		List<String> rows;
		char[][] field;
		int maxX;
		int maxY;
		Set<Beam> beams;
		Set<Pos> energized;
		Set<Beam> history;
		int ticks;
		public World() {
			this.rows = new ArrayList<>();
			this.beams = new LinkedHashSet<>();
			this.energized = new LinkedHashSet<>();
			this.history = new LinkedHashSet<>();
		}
		public void addRow(String row) {
			rows.add(row);
		}
		public void init() {
			ticks = 0;
			beams.clear();
			energized.clear();
			history.clear();
			maxY = rows.size();
			maxX = rows.get(0).length();
			field = new char[maxY][];
			for (int y=0; y<maxY; y++) {
				field[y] = rows.get(y).toCharArray();
			}
		}
		public void addBeam(int x, int y, int dir) {
			beams.add(new Beam(new Pos(x, y), dir));
		}
		private List<Beam> follow(Beam beam) {
			List<Beam> result = new ArrayList<>();
			char c = get(beam.pos);
			switch (c) {
			case '.': 
			case '<': 
			case '>': 
			case '^': 
			case 'v': 
			case '2': 
			case '3': 
			case '4': {
				result.add(beam);
				break;
			}
			case '#': {
				break;
			}
			case '|': {
				if ((beam.dir == DIR_EAST) || (beam.dir == DIR_WEST)) {
					result.add(new Beam(beam.pos, DIR_NORTH));
					result.add(new Beam(beam.pos, DIR_SOUTH));
				}
				else {
					result.add(beam);
				}
				break;
			}
			case '-': {
				if ((beam.dir == DIR_SOUTH) || (beam.dir == DIR_NORTH)) {
					result.add(new Beam(beam.pos, DIR_EAST));
					result.add(new Beam(beam.pos, DIR_WEST));
				}
				else {
					result.add(beam);
				}
				break;
			}
			case '\\': {
				if ((beam.dir == DIR_EAST) || (beam.dir == DIR_WEST)) {
					result.add(new Beam(beam.pos, rot(beam.dir, DIR_ROT_RIGHT)));
				}
				else {
					result.add(new Beam(beam.pos, rot(beam.dir, DIR_ROT_LEFT)));
				}
				break;
			}
			case '/': {
				if ((beam.dir == DIR_EAST) || (beam.dir == DIR_WEST)) {
					result.add(new Beam(beam.pos, rot(beam.dir, DIR_ROT_LEFT)));
				}
				else {
					result.add(new Beam(beam.pos, rot(beam.dir, DIR_ROT_RIGHT)));
				}
				break;
			}
			}
			return result;
		}
		private void set(Pos pos, char c) {
			set(pos.x, pos.y, c);
		}
		private void set(int x, int y, char c) {
			if ((x<0) || (y<0) || (x>=maxX) || (y>=maxY)) {
				return;
			}
			if (field[y][x] == '.') {
				field[y][x] = c;
			}
		}
		private char get(Pos pos) {
			return get(pos.x, pos.y);
		}
		private char get(int x, int y) {
			if ((x<0) || (y<0) || (x>=maxX) || (y>=maxY)) {
				return '#';
			}
			return field[y][x];
		}		
		public boolean hasBeams() {
			return !beams.isEmpty();
		}
		public void tick() {
			ticks++;
			Set<Beam> nextBeams = new LinkedHashSet<>();
			for (Beam beam:beams) {
				energized.add(beam.pos);
				nextBeams.addAll(follow(beam));
			}
			beams.clear();
			for (Beam beam:nextBeams) {
				Beam movedBeam = move(beam);
				if (movedBeam != null) {
					beams.add(movedBeam);
				}					
			}
		}
		private Beam move(Y23Day16.Beam beam) {
			Beam result = beam.move();
			if (history.contains(result)) {
				return null;
			}
			set(beam.pos, DIRS.charAt(beam.dir));
			history.add(result);
			return result;
		}
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			for (int y=0; y<maxY; y++) {
				for (int x=0; x<maxX; x++) {
					result.append(get(x,y));
				}
				result.append("\n");
			}
			return result.toString();
		}
		public void show() {
			Set<Pos> currentBeamPositions = beams.stream().map(b->b.pos).collect(Collectors.toSet());
			StringBuilder result = new StringBuilder();
			String lastColor = "b0";
			for (int y=-1; y<=maxY; y++) {
				for (int x=-1; x<=maxX; x++) {
					char c = get(x, y);
					String color = "b0";
					Pos pos = new Pos(x,y);
					if (energized.contains(pos)) {
						color = "byellow";
					}
					if (currentBeamPositions.contains(pos)) {
						color = "bred";
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
		public long countEnergized() {
			return energized.stream().filter(pos -> ((pos.x>=0) && (pos.x<maxX) && (pos.y>=0) && (pos.y<maxY))).count();
		}

	}

	public static void mainPart1(String inputFile) {
		output = new Y23GUIOutput16("2023 day 16 Part I", true);
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			world.addRow(data.row);
		}
		world.init();
		System.out.println(world);
		world.addBeam(0, 0, DIR_EAST);
		world.show();
		while (world.hasBeams()) {
			world.tick();
			world.show();
		}
		System.out.println("ENERGIZED: "+world.countEnergized());
	}

	


	public static void mainPart2(String inputFile) {
	}


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day16/Feri/input-example.txt");
		mainPart1("exercises/day16/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
		mainPart2("exercises/day16/Feri/input-example.txt");
//		mainPart2("exercises/day16/Feri/input.txt");
		System.out.println("---------------");    
	}
	
}
