
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2023/day/19
 */
public class Y23Day19 {

	/*
	 * Example:
	 * 
	 * px{a<2006:qkq,m>2090:A,rfg}
	 * pv{a>1716:R,A}
	 * lnx{m>1548:A,A}
	 * rfg{s<537:gd,x>2440:R,A}
	 * qs{s>3448:A,lnx}
	 * qkq{x<1416:A,crn}
	 * crn{x>2662:A,R}
	 * in{s<1351:px,qqz}
	 * qqz{s>2770:qs,m<1801:hdj,R}
	 * gd{a>3333:R,R}
	 * hdj{m>838:A,pv}
	 * 
	 * {x=787,m=2655,a=1222,s=2876}
	 * {x=1679,m=44,a=2067,s=496}
	 * {x=2036,m=264,a=79,s=2244}
	 * {x=2461,m=1339,a=466,s=291}
	 * {x=2127,m=1623,a=2188,s=1013}
	 * 
	 */

	private static final String INPUT_RX_RULE = "^([a-z]+)[{]([0-9a-zAR<>:,]+)[}]$";
	private static final String INPUT_RX_PART = "^[{]([0-9a-z=,]+)[}]$";
	
	public static record InputData(String ruleName, String ruleText, String partText) {}
	
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
			if (line.matches(INPUT_RX_RULE)) {
				String ruleName = line.replaceFirst(INPUT_RX_RULE, "$1");
				String ruleText = line.replaceFirst(INPUT_RX_RULE, "$2");
				return new InputData(ruleName, ruleText, null);
			}
			else if (line.matches(INPUT_RX_PART)) {
				String partText = line.replaceFirst(INPUT_RX_PART, "$1");
				return new InputData(null, null, partText);
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}


	public static void mainPart1(String inputFile) {
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
		}
	}

	


	public static void mainPart2(String inputFile) {
	}


	public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
		System.out.println("--- PART I ---");
		mainPart1("exercises/day19/Feri/input-example.txt");
//		mainPart1("exercises/day19/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
		URL url;
		System.out.println("--- PART I ---");
		mainPart2("exercises/day19/Feri/input-example.txt");
//		mainPart2("exercises/day19/Feri/input.txt");
		System.out.println("---------------");    
	}
	
}
