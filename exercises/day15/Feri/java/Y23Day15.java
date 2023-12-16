import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2023/day/15
 */
public class Y23Day15 {

	/*
	 * Example:
	 * 
	 * rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7
	 * 
	 */

	private static final String INPUT_RX = "^(.*)$";
	
	public static record InputData(String[] words) {}
	
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
				String[] words = line.replaceFirst(INPUT_RX, "$1").split(",");
				return new InputData(words);
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}

	static class HashCalc {
		int current;
		public HashCalc() {
			current = 0;
		}
		public static int calc(String word) {
			HashCalc hashCalc = new HashCalc();
			for (char c:word.toCharArray()) {
				hashCalc.addAscii(c);
			}
			return hashCalc.getHash();
		}
		public void addAscii(char c) {
			current = ((current + c)*17)%256;
		}
		public int getHash() {
			return current;
		}
	}

	static class HASHMAP {
		HashMap<String, Integer>[] boxes;
		public HASHMAP() {
			boxes = new HashMap[256];
		}
		public HashMap<String, Integer> getBox(int boxNr) {
			if (boxes[boxNr] == null) {
				boxes[boxNr] = new LinkedHashMap<>();
			}
			return boxes[boxNr];
		}
		public void remove(String key) {
			int boxNr = HashCalc.calc(key);
			getBox(boxNr).remove(key);
		}
		public void put(String key, int lens) {
			int boxNr = HashCalc.calc(key);
			getBox(boxNr).put(key, lens);
		}
		public int calcChecksum() {
			int result = 0;
			for (int i=0;i<256; i++) {
				HashMap<String,Integer> box = boxes[i];
				if (box == null) {
					continue;
				}
				int slot = 0;
				for (String key:box.keySet()) {
					slot++;
					result += (i+1)*slot*box.get(key);
				}
			}
			return result;
		}
	}

	public static void mainPart1(String inputFile) {
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			int sumHashes = 0;
			for (String word:data.words) {
				int hashValue = HashCalc.calc(word);
				System.out.println(word+" : "+hashValue);
				sumHashes += hashValue;
			}
			System.out.println("SUM HASHES: "+sumHashes);
		}
	}

	
	public static void mainPart2(String inputFile) {
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			HASHMAP hm = new HASHMAP(); 
			for (String word:data.words) {
				if (word.endsWith("-")) {
					hm.remove(word.replace("-", ""));
				}
				else {
					String[] key_value = word.split("=");
					hm.put(key_value[0], Integer.parseInt(key_value[1]));
				}
			}
			System.out.println("CHECKSUM: "+hm.calcChecksum());
		}		
	}


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day15/Feri/input-example.txt");
		mainPart1("exercises/day15/Feri/input.txt");               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
//		mainPart2("exercises/day15/Feri/input-example.txt");
		mainPart2("exercises/day15/Feri/input.txt");              // > 198485
		System.out.println("---------------");    //
	}
	
}
