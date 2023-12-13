import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * see: https://adventofcode.com/2023/day/09
 */
public class Y23Day09 {
 
	/*
	 * 
	 * 0 3 6 9 12 15
	 * 1 3 6 10 15 21
	 * 10 13 16 21 30 45
	 * 
	 */

	private static final String INPUT_RX = "^([0-9 -]+)$";
	
	public static record InputData(List<Long> sequence) {
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
				String[] strSequence = line.replaceFirst(INPUT_RX, "$1").split(" ");
				List<Long> sequence = Stream.of(strSequence).map(str -> Long.parseLong(str)).toList();
				return new InputData(sequence);
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}
	
	public static Long first(List<Long> longList) { return longList.get(0); }
	public static Long last(List<Long> longList) { return longList.get(longList.size()-1); }
	
	public static List<Long> calcNext(List<Long> sequence) {
		List<Long> result = new ArrayList<>(sequence);
		if ((first(sequence) == 0L) && (last(sequence) == 0L)) {
			result.add(0L);
		}
		else {
			List<Long> deviation = deviate(sequence);
			deviation = calcNext(deviation);
			result.add(last(result)+last(deviation));
		}
		return result;
	}

	public static List<Long> calcPrevious(List<Long> sequence) {
		List<Long> result = new ArrayList<>(sequence);
		if ((first(sequence) == 0L) && (last(sequence) == 0L)) {
			result.add(0L);
		}
		else {
			List<Long> deviation = deviate(sequence);
			deviation = calcPrevious(deviation);
			result.add(0, first(result)-first(deviation));
		}
		return result;
	}

	
	
	public static List<Long> deviate(List<Long> sequence) {
		List<Long> result = new ArrayList<>();
		for (int i=1; i<sequence.size(); i++) {
			result.add(sequence.get(i)-sequence.get(i-1));
		}
		return result;
	}
	
	public static void mainPart1(String inputFile) {
	    long sumPredictions = 0L;
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			List<Long> next = calcNext(data.sequence);
			System.out.println(next);
			sumPredictions += last(next);
		}
		System.out.println("SUMPREDICTIONS: "+sumPredictions);
	}
	
	
	public static void mainPart2(String inputFile) {
	    long sumHistory = 0L;
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			List<Long> hist = calcPrevious(data.sequence);
			System.out.println(hist);
			sumHistory += first(hist);
		}
		System.out.println("SUMPREDICTIONS: "+sumHistory);		
	}

	

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day09/Feri/input-example.txt");
		mainPart1("exercises/day09/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
//		mainPart2("exercises/day09/Feri/input-example.txt");
		mainPart2("exercises/day09/Feri/input.txt");                
		System.out.println("---------------");    //
	}
	
}
