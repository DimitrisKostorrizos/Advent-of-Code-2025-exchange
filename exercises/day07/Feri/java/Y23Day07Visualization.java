import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * see: https://adventofcode.com/2023/day/07
 */
public class Y23Day07Visualization {
 
	/*
	 * 
	 * 32T3K 765
	 * T55J5 684
	 * KK677 28
	 * KTJJT 220
	 * QQQJA 483
	 * 
	 */

	private static final String INPUT_RX     = "^([2-9TJQKA]{5}) ([0-9]+)$";
	
	public static record InputData(String cards, long bet) {
		@Override public String toString() { return cards+" "+num4(bet); }
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
			if (!line.matches(INPUT_RX)) {
				throw new RuntimeException("invalid line '"+line+"'");
			}
			String cards = line.replaceFirst(INPUT_RX, "$1");
			long bet = Long.parseLong(line.replaceFirst(INPUT_RX, "$2"));
			return new InputData(cards, bet);
		}
	}

	private static String num4(long num) { return padLeft(num, 4); }
	private static String num7(long num) { return padLeft(num, 7); }
	private static String num10(long num) { return padLeft(num, 10); }
	private static String padLeft(long num, int len) { return padLeft(Long.toString(num), len); }
	private static String padLeft(String text, int len) { return spaces(len-text.length()) + text; }
	private static String spaces(int n) {
		StringBuilder result = new StringBuilder(n);
		for (int i=0; i<n; i++) {
			result.append(' ');
		}
		return result.toString();
	}
	
	static int[] CARD_TYPE = {11111,  1112, 122, 113, 23, 14, 5};
	static Map<Integer, Integer> CARD_COUNT_ORDER;
	static {
		CARD_COUNT_ORDER = new HashMap<>();
		for (int i=0; i< CARD_TYPE.length; i++) {
			CARD_COUNT_ORDER.put(CARD_TYPE[i], i);
		}
	}
	
	static String CARDS = "*23456789TJQKA";
	
	public static int getCardType(String card) {
		Map<Character, Integer> counts = new HashMap<>();
		for (char c:card.toCharArray()) {
			counts.put(c, counts.getOrDefault(c, 0)+1);
		}
		if (counts.containsKey('*')) {
			int numJokers = counts.remove('*');
			if (numJokers == 5) {
				return CARD_COUNT_ORDER.get(numJokers);
			}
			List<Integer> countValues = counts.values().stream().sorted().collect(Collectors.toList());
			countValues.set(countValues.size()-1, countValues.get(countValues.size()-1)+numJokers);
			int cardCounts = Integer.parseInt(countValues.stream().map(i->Integer.toString(i)).collect(Collectors.joining()));
			return CARD_COUNT_ORDER.get(cardCounts);
		}
		int cardCounts = Integer.parseInt(counts.values().stream().sorted().map(i->Integer.toString(i)).collect(Collectors.joining()));
		return CARD_COUNT_ORDER.get(cardCounts);
	}
	
	static int cardCompare(String card1, String card2) {
		int type1 = getCardType(card1);
		int type2 = getCardType(card2);
		if (type1 > type2) {
			return 1;
		}
		if (type1 < type2) {
			return -1;
		}
//		System.out.println(card1+" <-> "+card2);
		for (int i=0; i<5; i++) {
			char c1 = card1.charAt(i);
			char c2 = card2.charAt(i);
			int n1 = CARDS.indexOf(c1);
			int n2 = CARDS.indexOf(c2);
			if (n1>n2) {
				return 1;
			}
			if (n1<n2) {
				return -1;
			}
		}
		return 0;
	}
	
	public static class Visualization {
		Y23GUIOutput07 output;
        List<String> outputRows;
        List<String> inputRows;
		public Visualization(String title) {
			this.output = new Y23GUIOutput07(title, true);
	        this.outputRows = new ArrayList<>();
	        this.inputRows = new ArrayList<>();
		}
        public void addInput(String line) {
        	inputRows.add(line);
        }
        public void removeNextInput() {
        	inputRows.remove(0);
        	outputRows.clear();
        }
        public void addOutput(String line) {
        	outputRows.add(line);
        }
		public void show() {
			StringBuilder result = new StringBuilder();
			result.append(color("gray"));
			for (String line:outputRows) {
				result.append(line).append("\n");
			}
			result.append(color("black"));
			for (String line:inputRows) {
				result.append(line).append("\n");
			}
			output.addStep(result.toString());
		}
		public String color(String colName) { return output.color(colName); }
		public String style(String colName) { return output.style(colName); }
	}
	static Visualization vis;
	
	public static void mainPart1(String inputFile) {
	    vis = new Visualization("2023 Day 07 Part I");
		for (InputData data:new InputProcessor(inputFile)) {
			String line = data.toString();
			vis.addInput(line);
		}
		vis.show();
		Map<String, Long> cardBets = new HashMap<>();
		for (InputData data:new InputProcessor(inputFile)) {
			vis.removeNextInput();
			cardBets.put(data.cards, data.bet);
			List<String> cards = new ArrayList<>(cardBets.keySet());
			cards.sort((c1,c2) -> cardCompare(c1, c2));
			long result = 0;
			for (int i=0; i<cards.size(); i++) {
				String card = cards.get(i);
				long bet = cardBets.get(card);
				result = result + (i+1)*bet;
//				System.out.println(card+" "+bet);
				String line;
				if (card.equals(data.cards)) {
					line = vis.style("b2")+card+" "+num4(bet)+vis.color("green")+" "+num4(i+1)+"*"+num4(bet) + " ="+num7((i+1)*bet)+"  "+ num10(result)+vis.color("gray");
				}
				else {
					line = card+" "+num4(bet)+vis.color("green")+" "+num4(i+1)+"*"+num4(bet) + " ="+num7((i+1)*bet)+"  "+ num10(result)+vis.color("gray");
				}
				vis.addOutput(line);
			}
			vis.show();
		}
	}
	
	
	public static void mainPart2(String inputFile) {
	    vis = new Visualization("2023 Day 07 Part II");
		for (InputData data:new InputProcessor(inputFile)) {
			InputData jData = new InputData(data.cards.replace('J', '*'), data.bet);
			String line = jData.toString();
			vis.addInput(line);
		}
		vis.show();
		Map<String, Long> cardBets = new HashMap<>();
		for (InputData data:new InputProcessor(inputFile)) {
			vis.removeNextInput();
			InputData jData = new InputData(data.cards.replace('J', '*'), data.bet);
			Long exists = cardBets.put(jData.cards, jData.bet);
			if (exists != null) {
				throw new RuntimeException("duplicate cards "+data);
			}
			List<String> cards = new ArrayList<>(cardBets.keySet());
			cards.sort((c1,c2) -> cardCompare(c1, c2));
			long result = 0;
			System.out.println("-----");
			for (int i=0; i<cards.size(); i++) {
				String card = cards.get(i);
				long bet = cardBets.get(card);
				result = result + (i+1)*bet;
				String line;
				if (card.equals(jData.cards)) {
					line = vis.style("b2")+card+" "+num4(bet)+vis.color("green")+" "+num4(i+1)+"*"+num4(bet) + " ="+num7((i+1)*bet)+"  "+ num10(result)+vis.color("gray");
				}
				else {
					line = card+" "+num4(bet)+vis.color("green")+" "+num4(i+1)+"*"+num4(bet) + " ="+num7((i+1)*bet)+"  "+ num10(result)+vis.color("gray");
				}
				vis.addOutput(line);
			}
			vis.show();
		}
	}

	

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println(cardCompare("JJJJJ","AKAAA"));
		System.out.println(cardCompare("AA5AA","AKAAA"));
		System.out.println("--- PART I ---");
		mainPart1("exercises/day07/Feri/input-example.txt");
//		mainPart1("exercises/day07/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
		mainPart2("exercises/day07/Feri/input-example.txt");
//		mainPart2("exercises/day07/Feri/input.txt");                // > 247984518
		System.out.println("---------------");    //
	}
	
}
