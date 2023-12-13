import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2023/day/12
 */
public class Y23Day12 {

	/*
	 * Example:
	 * 
	 * ???.### 1,1,3
	 * .??..??...?##. 1,1,3
	 * ?#?#?#?#?#?#?#? 1,3,1,6
	 * ????.#...#... 4,1,1
	 * ????.######..#####. 1,6,5
	 * ?###???????? 3,2,1
	 * 
	 */

	private static final String INPUT_RX = "^([?.#]+) ([0-9,]+)$";
	
	public static record InputData(String row, int[] damagedSpringGroups) {
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(row).append(" ");
			for (int i=0; i<damagedSpringGroups.length; i++) {
				result.append(Integer.toString(damagedSpringGroups[i]));
			}
			return result.toString();
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
			while (line.length() == 0) {
				line = scanner.nextLine();
			}
			if (line.matches(INPUT_RX)) {
				String row = line.replaceFirst(INPUT_RX, "$1");
				String[] damagedSpringGroupsStr = line.replaceFirst(INPUT_RX, "$2").split(",");
				int[] damagedSpringGroups = new int[damagedSpringGroupsStr.length];
				for (int i=0; i<damagedSpringGroupsStr.length; i++) {
					damagedSpringGroups[i] = Integer.parseInt(damagedSpringGroupsStr[i]);
				}
				return new InputData(row, damagedSpringGroups);
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}

	private static String showInts(int[] arr, int start, int end) {
		StringBuilder result = new StringBuilder();
		String seperator="";
		for (int i=start; i<end; i++) {
			result.append(seperator).append(Integer.toString(arr[i]));
			seperator = ",";
		}
		return result.toString();
	}

	
	public static class BruteForceSolver {
		char[] pattern;
		int[] groups;
		
		public BruteForceSolver(String row) {
			this.pattern = row.toCharArray();
		}
		public long countSolutions(int[] groups) {
			this.groups = groups;
			return recursiveCountSolutions(0, 0, 0, "");
		}

		protected long recursiveCountSolutions(int patternPos, int groupPos, int cntLastGroup, String debugging) {
			if (patternPos == pattern.length) {
				if ((groupPos == groups.length) && (cntLastGroup==0)) {
//					System.out.println(debugging);
					return 1;
				}
				if ((groupPos == groups.length) && (cntLastGroup==groups[groupPos-1])) {
//					System.out.println(debugging);
					return 1;
				}
				return 0;
			}
			char c = pattern[patternPos];
			long result = 0;
			if (c == '?') {
				String debDot = debugging+'.';
				String debHash = debugging+'#';
				result = recursiveCountSolutions('.', patternPos, groupPos, cntLastGroup, debDot);
				result += recursiveCountSolutions('#', patternPos, groupPos, cntLastGroup, debHash);
			}
			else {
				String deb = debugging+c;
				result = recursiveCountSolutions(c, patternPos, groupPos, cntLastGroup, deb);
			}
			return result;
		}
	
		private long recursiveCountSolutions(char c, int patternPos, int groupPos, int cntLastGroup, String debugging) {
//			System.out.println("POS: "+patternPos+" CHAR: "+c+" GROUPS:"+showInts(groups, 0, groupPos));
//			System.out.println(new String(pattern, 0, patternPos) + "[" + c + "]" + new String(pattern, patternPos+1, pattern.length-patternPos-1));
//			System.out.println(debugging + " | " + new String(pattern, patternPos+1, pattern.length-patternPos-1));
//			System.out.println(showInts(groups, 0, groupPos) + " | " + showInts(groups, groupPos, groups.length));
//			System.out.println("###################".substring(0, cntLastGroup));
//			System.out.println();
			
			if (c=='.') {
				if (cntLastGroup == 0) {
					return recursiveCountSolutions(patternPos+1, groupPos, 0, debugging);
				}
				if (groups[groupPos-1] != cntLastGroup) {
					return 0;
				}
				return recursiveCountSolutions(patternPos+1, groupPos, 0, debugging);
			}
			if (c!='#') {
				throw new RuntimeException("invalid pattern '"+c+"' at pos "+patternPos);
			}
			if (cntLastGroup == 0) {
				if (groupPos == groups.length) {
					return 0;
				}
				return recursiveCountSolutions(patternPos+1, groupPos+1, 1, debugging);
			}
			if (cntLastGroup>=groups[groupPos-1]) {
				return 0;
			}
			return recursiveCountSolutions(patternPos+1, groupPos, cntLastGroup+1, debugging);
		}
	}

	public static void mainPart1(String inputFile) {
		long sumArrangements = 0;
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			BruteForceSolver bfs = new BruteForceSolver(data.row);
			long solutions = bfs.countSolutions(data.damagedSpringGroups);
			sumArrangements += solutions;
			System.out.println("SOLUTIONS: "+solutions);
		}
		System.out.println("SUM ARRANGEMENTS: "+sumArrangements);
	}
	
	
	public static String replicate(String text) {
		StringBuilder result = new StringBuilder();
		String seperator="";
		for (int i=0; i<5; i++) {
			result.append(seperator).append(text);
			seperator = "?";
		}
		return result.toString();
	}
	
	public static int[] replicate(int[] arr) {
		int[] result = new int[5*arr.length];
		for (int i=0; i<5; i++) {
			for (int j=0; j<arr.length; j++) {
				result[i*arr.length+j] = arr[j];
			}
		}
		return result;
	}

	private static record State(int patternPos, int groupPos, int cntLastGroup) {}

	public static class CachedBruteForceSolver extends BruteForceSolver {
		private Map<State, Long> cache;
		public CachedBruteForceSolver(String row) {
			super(row);
			cache = new HashMap<>();
		}
		@Override protected long recursiveCountSolutions(int patternPos, int groupPos, int cntLastGroup, String debug) {
			State state = new State(patternPos, groupPos, cntLastGroup);
			if (cache.containsKey(state)) {
				return cache.get(state);
			}
			long result = super.recursiveCountSolutions(patternPos, groupPos, cntLastGroup, debug);
			cache.put(state, result);
			return result;
		}
	}

	public static void mainPart2(String inputFile) {
		long sumArrangements = 0;
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			BruteForceSolver bfs = new CachedBruteForceSolver(replicate(data.row));
			long solutions = bfs.countSolutions(replicate(data.damagedSpringGroups));
			sumArrangements += solutions;
			System.out.println("SOLUTIONS: "+solutions);
		}
		System.out.println("SUM ARRANGEMENTS: "+sumArrangements);
	}


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day12/Feri/input-example.txt");
		mainPart1("exercises/day12/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
//		mainPart2("exercises/day12/Feri/input-example.txt");
		mainPart2("exercises/day12/Feri/input.txt");              
		System.out.println("---------------");    //
	}
	
}
