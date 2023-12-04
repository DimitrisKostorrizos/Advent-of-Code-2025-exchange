import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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

/**
 * see: https://adventofcode.com/2023/day/04
 */
public class Y23Day04 {
 
	/*
	 * example input: 
	 * 
	 * Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
	 * Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
	 * Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
	 * Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
	 * Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
	 * Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11
	 * 
	 */

	private static final String INPUT_RX   = "^Card \\s*([0-9]+): ([ 0-9]+) [|] ([ 0-9]+)$";
	
	public static record InputData(int cardnum, Set<Integer> winNums, Set<Integer> ownNums) {}
	
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
				int cardNum = Integer.parseInt(line.replaceFirst(INPUT_RX, "$1"));
				String[] winNumArray = line.replaceFirst(INPUT_RX, "$2").trim().split(" +");
				String[] ownNumArray = line.replaceFirst(INPUT_RX, "$3").trim().split(" +");
				Set<Integer> winNums = new HashSet();
				for (String winNum:winNumArray) {
					winNums.add(Integer.parseInt(winNum));
				}
				Set<Integer> ownNums = new HashSet();
				for (String ownNum:ownNumArray) {
					ownNums.add(Integer.parseInt(ownNum));
				}
				return new InputData(cardNum, winNums, ownNums);
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}

	
	public static void mainPart1(String inputFile) {
		int sumPoints = 0;
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			data.ownNums.retainAll(data.winNums);
			int cntWins = data.ownNums.size();
			System.out.println(cntWins);
			int winPoints = cntWins == 0 ? 0 : (int) Math.pow(2, cntWins-1);
			System.out.println(winPoints);
			sumPoints+=winPoints;
		}
		System.out.println(sumPoints);
	}

	
	public static void mainPart2(String inputFile) {
	}

	

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day04/Feri/input-example.txt");
		mainPart1("exercises/day04/Feri/input.txt");            // < 104960
		System.out.println("---------------");
		System.out.println("--- PART II ---");
		mainPart2("exercises/day04/Feri/input-example.txt");
//		mainPart2("exercises/day04/Feri/input.txt");     
		System.out.println("---------------");    // 
	}
	
}
