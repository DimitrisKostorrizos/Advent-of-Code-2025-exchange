import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.RowFilter;

/**
 * see: https://adventofcode.com/2023/day/03
 */
public class Y23Day03Visualization {
 
	static Y23GUIOutput03 output;
	
	/*
	 * example input: 
	 *
	 * Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
	 * Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
	 * Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
	 * Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
	 * Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green
	 * 
	 */

	private static final String INPUT_RX   = "^(.*)$";
	private static final String INPUT_TOKEN   = "([0-9]+|[^.0-9])";
	
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

	static record Pos(int x, int y) {
		@Override public String toString() { return "("+x+","+y+")"; }
	}
	
	static record Range(Pos from, Pos to) implements Iterable<Pos> {
		public List<Pos> positions() {
			List<Pos> result = new ArrayList<>();
			for (int y=from.y; y<=to.y; y++) {
				for (int x=from.x; x<=to.x; x++) {
					result.add(new Pos(x,y));
				}
			}
			return result;
		}
		public List<Pos> innerPositions() {
			List<Pos> result = new ArrayList<>();
			for (int y=from.y+1; y<to.y; y++) {
				for (int x=from.x+1; x<to.x; x++) {
					result.add(new Pos(x,y));
				}
			}
			return result;
		}
		@Override public Iterator<Y23Day03Visualization.Pos> iterator() { return positions().iterator(); }
		@Override public String toString() { return "["+from.toString()+"->"+to.toString()+"]"; }
		public boolean contains(Pos pos) { return from.x<=pos.x && from.y<=pos.y && pos.x<=to.x && pos.y<=to.y; }
	}
	
	static record PartNumber(int num, Range range) {}
	
	
	static class World {
		Map<Pos, Character> symbolPositions = new LinkedHashMap<>();
		List<PartNumber> partNumbers = new ArrayList<>();
		public void addSymbol(String symbol, int x, int y) {
			symbolPositions.put(new Pos(x,y), symbol.charAt(0));
		}
		public void addNumber(String num, int x, int y) {
			int len = num.length();
			partNumbers.add(new PartNumber(Integer.parseInt(num), new Range(new Pos(x-1,y-1), new Pos(x+len,y+1))));
		}
		public List<PartNumber> searchValidParts() {
			List<PartNumber> result = new ArrayList<>(); 
			for (PartNumber partNumber:partNumbers) {
				for (Pos pos:partNumber.range) {
					if (symbolPositions.containsKey(pos)) {
						result.add(partNumber);
						vis.markedPositions.addAll(partNumber.range.positions());
						vis.show();
						break;
					}
				}
			}
			return result;
		}
		public int calcSumGearRatio() {
			int result = 0;
			for (Entry<Pos, Character> entry:symbolPositions.entrySet()) {
				if (entry.getValue() != '*') {
					continue;
				}
				List<PartNumber> adjacentPartNumbers = findPartNumbersInRang(entry.getKey());
				if (adjacentPartNumbers.size() == 2) {
					vis.markedPositions.add(entry.getKey());
					vis.markedPositions.addAll(adjacentPartNumbers.get(0).range.innerPositions());
					vis.markedPositions.addAll(adjacentPartNumbers.get(1).range.innerPositions());
					vis.show();
					int ratio = adjacentPartNumbers.get(0).num * adjacentPartNumbers.get(1).num;
					result += ratio;
				}
			}
			return result;
		}
		private List<PartNumber> findPartNumbersInRang(Pos pos) {
			return partNumbers.stream().filter(pn -> pn.range.contains(pos)).toList();
		}
	}
	
	public static class Visualization {
		Y23GUIOutput03 output;
		public Visualization(String title) {
			output = new Y23GUIOutput03(title, true);
		}
        List<String> inputRows = new ArrayList<>();
        Set<Pos> markedPositions = new HashSet<>();
		public void show() {
			StringBuilder result = new StringBuilder();
			String lastColor = "b0";
			for (int y=0; y<inputRows.size(); y++) {
				String line = inputRows.get(y);
				for (int x=0; x<line.length(); x++) {
					char c = line.charAt(x);
					String color = (c != '.') && markedPositions.contains(new Pos(x,y)) ? "byellow" : "b0";
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
	}
	static Visualization vis;
	
	public static void mainPart1(String inputFile) {
        vis = new Visualization("2023 Day 03 Part I");
        Pattern tokenPattern = Pattern.compile(INPUT_TOKEN);
        int y = 0;
        World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			vis.inputRows.add(data.row);
			System.out.println("ROW "+y+": "+data);

	        Matcher matcher = tokenPattern.matcher(data.row);
	        while(matcher.find()) {
	        	String value = data.row.substring(matcher.start(), matcher.end());
	        	int x = matcher.start();
	            System.out.println("found: " + value + " at ("+x+","+y+")");
	            if ((value.charAt(0) >= '0') && (value.charAt(0) <= '9')) {
	            	world.addNumber(value, x, y);
	            }
	            else {
	            	world.addSymbol(value, x, y);
	            }
	        }
	        y++;
		}
		List<PartNumber> validParts = world.searchValidParts();
		int sum = validParts.stream().mapToInt(pn -> pn.num).sum();
		System.out.println(sum);
	}

	
	public static void mainPart2(String inputFile) {
        vis = new Visualization("2023 Day 03 Part II");
        Pattern tokenPattern = Pattern.compile(INPUT_TOKEN);
        int y = 0;
        World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			vis.inputRows.add(data.row);
			System.out.println("ROW "+y+": "+data);

	        Matcher matcher = tokenPattern.matcher(data.row);
	        while(matcher.find()) {
	        	String value = data.row.substring(matcher.start(), matcher.end());
	        	int x = matcher.start();
	            System.out.println("found: " + value + " at ("+x+","+y+")");
	            if ((value.charAt(0) >= '0') && (value.charAt(0) <= '9')) {
	            	world.addNumber(value, x, y);
	            }
	            else {
	            	world.addSymbol(value, x, y);
	            }
	        }
	        y++;
		}
		int sum = world.calcSumGearRatio();
		System.out.println(sum);
	}

	

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
		mainPart1("exercises/day03/Feri/input-example.txt");
//		mainPart1("exercises/day03/Feri/input.txt");
		System.out.println("---------------");
		System.out.println("--- PART II ---");
//		mainPart2("exercises/day03/Feri/input-example.txt");
		mainPart2("exercises/day03/Feri/input.txt");     
		System.out.println("---------------");    // 
	}
	
}
