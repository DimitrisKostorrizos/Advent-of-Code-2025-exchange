
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

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
	private static final String INPUT_RX_RULE_COND = "^([xmas])([<>])([0-9]+)[:]([a-zRA]+)$";
	private static final String INPUT_RX_RULE_FOLLOWUP = "^([a-zRA]+)$";
	private static final String INPUT_RX_PART = "^[{]([0-9a-z=,]+)[}]$";
	private static final String INPUT_RX_PART_TEXT = "^x=([0-9]+),m=([0-9]+),a=([0-9]+),s=([0-9]+)$";
	
	public static record InputData(String ruleName, String ruleText, String partText) {
		public boolean isRule() { return ruleName != null; }
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


	static record Condition(char category, char operator, int value, String acceptName) {
		public static Condition create(String str) {
			if (str.matches(INPUT_RX_RULE_COND)) {
				char category = str.replaceFirst(INPUT_RX_RULE_COND, "$1").charAt(0);
				char operator = str.replaceFirst(INPUT_RX_RULE_COND, "$2").charAt(0);
				int value = Integer.parseInt(str.replaceFirst(INPUT_RX_RULE_COND, "$3"));
				String acceptName = str.replaceFirst(INPUT_RX_RULE_COND, "$4");
				return new Condition(category, operator, value, acceptName);
			}
			else if (str.matches(INPUT_RX_RULE_FOLLOWUP)) {
				return new Condition('*', '*', 0, str);
			}
			else {
				throw new RuntimeException("invalid condition '"+str+"'");
			}
		}
		public boolean hasCondition() {
			return category != '*'; 
		}
		@Override public String toString() {
			if (hasCondition()) {
				return Character.toString(category)+Character.toString(operator)+value+":"+acceptName;
			}
			return acceptName; 
		}

		public boolean check(Part part) {
			if (category == '*') {
				return true;
			}
			int compValue = part.get(category);
			if (operator == '<') {
				return compValue<value;
			}
			return compValue>value;
		}
	}

	static class Rule {
		String name;
		List<Condition> conditions;
		public Rule(String name, List<Condition> conditions) {
			this.name = name;
			this.conditions = conditions;
		}
		public String check(Part part) {
			for (Condition condition:conditions) {
				if (condition.check(part)) {
					return condition.acceptName;
				}
			}
			throw new RuntimeException("RULE does not have a default result "+this);
		}
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(name);
			String seperator = "{";
			for (Condition condition:conditions) {
				result.append(seperator).append(condition.toString());
				seperator = ",";
			}
			result.append("}");
			return result.toString();
		}
	}

	static record Part(int x, int m, int a, int s) {
		int get(char category) {
			switch (category) {
			case 'x': return x;
			case 'm': return m;
			case 'a': return a;
			case 's': return s;
			default: throw new RuntimeException("invalid category "+category);
			}
		}
		@Override public String toString() {
			return "{x="+x+",m="+m+",a="+a+",s="+s+"}";
		}
		public static Part create(String partText) {
			int x = Integer.parseInt(partText.replaceFirst(INPUT_RX_PART_TEXT, "$1"));
			int m = Integer.parseInt(partText.replaceFirst(INPUT_RX_PART_TEXT, "$2"));
			int a = Integer.parseInt(partText.replaceFirst(INPUT_RX_PART_TEXT, "$3"));
			int s = Integer.parseInt(partText.replaceFirst(INPUT_RX_PART_TEXT, "$4"));
			return new Part(x,m,a,s);
		}
		public int sum() {
			return x+m+a+s;
		}
	}


	public static class World {
		List<Rule> rules;
		Map<String, Rule> rulesByName;
		List<Part> parts;
		public World() {
			this.rulesByName = new LinkedHashMap<>();
			this.rules = new ArrayList<>();
			this.parts = new ArrayList<>();
		}
		public void addRule(String ruleName, String ruleText) {
			List<Condition> conditions = Stream.of(ruleText.split(",")).map(str -> Condition.create(str)).toList();
			Rule rule = new Rule(ruleName, conditions);
			rules.add(rule);
			rulesByName.put(ruleName, rule);
		}
		public void addPart(String partText) {
			parts.add(Part.create(partText));
			
		}
		@Override
		public String toString() {
			return rules.toString() + "\n" + parts.toString();
		}
		public List<Part> filterAcceptedParts() {
			return parts.stream().filter(part->checkAccepted(part)).toList();
		}
		private boolean checkAccepted(Part part) {
			String nextRuleName = "in";
			while ((!nextRuleName.equals("A"))&&(!nextRuleName.equals("R"))) {
				Rule rule = rulesByName.get(nextRuleName);
				nextRuleName = rule.check(part);
			}
			System.out.println(part+" -> "+nextRuleName);
			return nextRuleName.equals("A");
		}
		public long calcAlternatives() {
			Alternatives alts = new Alternatives();
			System.out.println(alts.calcNumberOfAlternatives());
			return recursiveCalcAlternatives(alts, "in");
		}
		private long recursiveCalcAlternatives(Alternatives alts, String ruleName) {
			if (ruleName.equals("R")) {
				return 0;
			}
			if (ruleName.equals("A")) {
				return alts.calcNumberOfAlternatives();
			}
			long result = 0;
			Rule rule = rulesByName.get(ruleName);
			Alternatives remainingAlts = alts;
			for (Condition condition:rule.conditions) {
				if (!condition.hasCondition()) {
					result += recursiveCalcAlternatives(remainingAlts, condition.acceptName);
				}
				else {
					Alternatives conditionAlts = remainingAlts.filterCondition(condition.category, condition.operator, condition.value);
					if (conditionAlts != null) {
						result += recursiveCalcAlternatives(conditionAlts, condition.acceptName); 
					}
					remainingAlts = remainingAlts.removeCondition(condition);
					if (remainingAlts == null) {
						return result;
					}
				}
			}
			throw new RuntimeException("rule not catiching all cases: "+rule);
		}
	}
	
	static record NumRange(int from, int to) {
		public boolean overlaps(NumRange other) {
			return (from>=other.to) && (to<=other.from);
		}
		public List<NumRange> intersect(NumRange other) {
			List<NumRange> result = new ArrayList<>();
			int maxFrom = Math.max(from, other.from);
			int minTo = Math.min(to, other.to);
			if (maxFrom<=minTo) {
				result.add(new NumRange(maxFrom, minTo));
			}
			return result;
		}

		@Override public String toString() {
			return from+".."+to;
		}
		public long calcNumberOfAlternatives() {
			return to-from+1;
		}
	}
	
	static record NumRanges(List<NumRange> numRanges) {

		public long calcNumberOfAlternatives() {
			long result = 0;
			for (NumRange nr:numRanges) {
				result += nr.calcNumberOfAlternatives();
			}
			return result;
		}}
	
	static class Alternatives {
		NumRanges xAlternatives;
		NumRanges mAlternatives;
		NumRanges aAlternatives;
		NumRanges sAlternatives;
		public Alternatives() {
			xAlternatives = initNumRanges(1,4000);
			mAlternatives = initNumRanges(1,4000);
			aAlternatives = initNumRanges(1,4000);
			sAlternatives = initNumRanges(1,4000);
		}
		public Alternatives removeCondition(Condition condition) {
			if (condition.operator == '<') {
				return filterCondition(condition.category, '>', condition.value-1);
			}
			return filterCondition(condition.category, '<', condition.value+1);
		}
		public Alternatives(NumRanges xAlternatives, NumRanges mAlternatives, NumRanges aAlternatives, NumRanges sAlternatives) {
			this.xAlternatives = xAlternatives;
			this.mAlternatives = mAlternatives;
			this.aAlternatives = aAlternatives;
			this.sAlternatives = sAlternatives;
		}
		public Alternatives filterCondition(char category, char operator, int value) {
			NumRanges currentNRS = get(category);
			NumRange condNR;
			if (operator == '<') {
				condNR = new NumRange(1,value-1);
			}
			else {
				condNR = new NumRange(value, 4000);
			}
			NumRanges filteredNRS = condNRS.filter(condNR);
			if (filteredNRS.calcNumberOfAlternatives()==0) {
				return null;
			}
			Alternatives result = new Alternatives(xAlternatives, mAlternatives, aAlternatives, sAlternatives);
			result.set(category, filteredNRS);
			return result;
		}
		private NumRanges initNumRanges(int from, int to) {
			List<NumRange> nrList = new ArrayList<>();
			nrList.add(new NumRange(from, to));
			return new NumRanges(nrList);
		}
		NumRanges get(char category) {
			switch (category) {
			case 'x': return xAlternatives;
			case 'm': return mAlternatives;
			case 'a': return aAlternatives;
			case 's': return sAlternatives;
			default: throw new RuntimeException("invalid category '"+category+"'");
			}
		}
		void set(char category, NumRanges nrs) {
			switch (category) {
			case 'x': 
				xAlternatives = nrs;
				break;
			case 'm': 
				mAlternatives = nrs;
				break;
			case 'a': 
				aAlternatives = nrs;
				break;
			case 's': 
				sAlternatives = nrs;
				break;
			default: throw new RuntimeException("invalid category '"+category+"'");
			}
		}
		public long calcNumberOfAlternatives() {
			return xAlternatives.calcNumberOfAlternatives() * mAlternatives.calcNumberOfAlternatives() * aAlternatives.calcNumberOfAlternatives() * sAlternatives.calcNumberOfAlternatives(); 
		}
	}
	
	
	public static void mainPart1(String inputFile) {
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
//			System.out.println(data);
			if (data.isRule()) {
				world.addRule(data.ruleName, data.ruleText);
			}
			else {
				world.addPart(data.partText);
			}
		}
		List<Part> acceptedParts = world.filterAcceptedParts();
//		System.out.println(acceptedParts);
		int sum = 0;
		for (Part part:acceptedParts) {
			sum += part.sum();
		}
		System.out.println("ACCEPTEDSUM: "+sum);
	}

	


	public static void mainPart2(String inputFile) {
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
//			System.out.println(data);
			if (data.isRule()) {
				world.addRule(data.ruleName, data.ruleText);
			}
			else {
				world.addPart(data.partText);
			}
		}
		System.out.println(world.calcAlternatives());
	}


	public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day19/Feri/input-example.txt");
		mainPart1("exercises/day19/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
		URL url;
		System.out.println("--- PART I ---");
		mainPart2("exercises/day19/Feri/input-example.txt");
//		mainPart2("exercises/day19/Feri/input.txt");
		System.out.println("---------------");    
	}
	
}
