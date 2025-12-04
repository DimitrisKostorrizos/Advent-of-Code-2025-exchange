import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


/**
 * see: https://adventofcode.com/2025/day/2
 *
 */
public class Y25Day02withGUI {

	
	static Y25GUIOutput02 output;

	
	public static record InputData(String row) {}

	private static final String INPUT_RX = "^([-0-9,]+)$";
	
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

	
 
	public static void mainPart1(String inputFile) throws FileNotFoundException {
		
		output = new Y25GUIOutput02("2025 Day 02 Part 1", true);
		
		long sum_invalid = 0;
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			String[] ranges = data.row().split(",");
			for (String range:ranges) {
				String[] from_to = range.split("-");
				if (from_to[0].startsWith("0") || from_to[1].startsWith("0")) {
					throw new RuntimeException("invalid from '"+from_to[0]+"'");
				}
				long from = Long.parseLong(from_to[0]);
				long to = Long.parseLong(from_to[1]);
				System.out.println("  range: "+from+" - "+to);
				for (long i=from; i<=to; i++) {
					if (checkInvalid(i)) {
						System.out.println("    invalid number: "+i);
						sum_invalid += i;
					}
				}
			}
		}
		System.out.println("sum invalid: "+sum_invalid);
	}

	
	private static boolean checkInvalid(long n) {
		String s = Long.toString(n);
		if (s.length() % 2 != 0) {
			return false;
		}
		int half_len = s.length() / 2;
		String n_left = s.substring(0, half_len);
		String n_right = s.substring(half_len);
		return n_left.equals(n_right);
	}


	public static record OutputLine(String text, boolean isError) {}
	
	public static class ScrollingOutput {
		List<OutputLine> lines;
		int maxLines = 20;
		int lPad;
		public ScrollingOutput(int maxLine, int lPad) {
			this.lines = new ArrayList<>();
			this.maxLines = maxLine;
			this.lPad = lPad;
		}

		public void addLine(String text, boolean isError) {
			lines.add(new OutputLine(text, isError));
			if (lines.size() > maxLines) {
				int remLine = 0;
				while (remLine<maxLines-1 && lines.get(remLine).isError) {
					remLine++;
				}
				lines.remove(remLine);
			}
		}
		
		public void show() {
			String colEven = "°bor;";
			String colOdd = "°bye;";
			StringBuilder sb = new StringBuilder();
			for (OutputLine line:lines) {
				String text = line.text;
				String prefix = (text.length() >= lPad) ? "" : "                              ".substring(0, lPad - text.length());
				if (line.isError) {
					String helper = colOdd;
					colOdd = colEven;
					colEven = helper;
					int partSize = findPartSize(text);
					StringBuilder coloredText = new StringBuilder();
					for (int part=0; part<text.length()/partSize; part++) {
						coloredText.append((part % 2 == 0) ? colEven : colOdd);
						coloredText.append(text.substring(0, partSize));
					}
					coloredText.append("°c0;");
					text = coloredText.toString();
				}
				sb.append(prefix).append(text).append("\n");
			}
			output.addStep(sb.toString());
		}

		private int findPartSize(String text) {
			for (int partSize=1; partSize<=text.length()/2; partSize++) {
				if (text.length() % partSize != 0) {
					continue;
				}
				String part = text.substring(0, partSize);
				StringBuilder comp = new StringBuilder();
				for (int i=0; i<text.length()/partSize; i++) {
					comp.append(part);
				}
				if (comp.toString().equals(text)) {
					return partSize;
				}
			}
			return 0;
		}
		
	}
	
	public static void mainPart2(String inputFile) {

		output = new Y25GUIOutput02("2025 Day 02 Part 2", true);
		
		ScrollingOutput scrollingOutput = new ScrollingOutput(20, 11);
		scrollingOutput.show();
		
		long sum_invalid = 0;
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			String[] ranges = data.row().split(",");
			for (String range:ranges) {
				String[] from_to = range.split("-");
				if (from_to[0].startsWith("0") || from_to[1].startsWith("0")) {
					throw new RuntimeException("invalid from '"+from_to[0]+"'");
				}
				long from = Long.parseLong(from_to[0]);
				long to = Long.parseLong(from_to[1]);
				System.out.println("  range: "+from+" - "+to);
				for (long i=from; i<=to; i++) {
					boolean invalid = checkInvalid2(i);
					if (invalid) {
						System.out.println("    invalid number: "+i);
						sum_invalid += i;
					}
					scrollingOutput.addLine(Long.toString(i), invalid);
					scrollingOutput.show();
				}
			}
			
			scrollingOutput.addLine("==========", false);
			scrollingOutput.addLine(""+sum_invalid, false);
			for (int n=0; n<5; n++) {
				scrollingOutput.addLine("", false);
				scrollingOutput.show();
			}
		}
		System.out.println("sum invalid: "+sum_invalid);
	}

	private static boolean checkInvalid2(long n) {
		String s = Long.toString(n);
		for (int repeats=2; repeats<=s.length(); repeats++) {
			if (checkInvalid2repeats(n, repeats)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean checkInvalid2repeats(long n, int repeats) {
		String s = Long.toString(n);
		if (s.length() % repeats != 0) {
			return false;
		}
		int part_len = s.length() / repeats;
		String n_first = s.substring(0, part_len);
		String comp = "";
		for (int i=0; i<repeats; i++) {
			comp += n_first;
		}
		return s.equals(comp);
	}
		
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I  ---");
//		mainPart1("exercises/day02/feri/input-example.txt");
//		mainPart1("exercises/day02/feri/input.txt");     
		System.out.println("---------------");
		System.out.println();
		System.out.println("--- PART II ---");
		mainPart2("exercises/day02/feri/input-example.txt");
//		mainPart2("exercises/day02/feri/input.txt");
		System.out.println("---------------");
	}

	
}