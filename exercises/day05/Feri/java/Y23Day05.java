import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * see: https://adventofcode.com/2023/day/04
 */
public class Y23Day05 {
 
	/*
	 * example input: 
	 * 
	 * seeds: 79 14 55 13
	 * 
	 * seed-to-soil map:
	 * 50 98 2
	 * 52 50 48
	 * 
	 * soil-to-fertilizer map:
	 * 0 15 37
	 * 37 52 2
	 * 39 0 15
	 * 
	 * fertilizer-to-water map:
	 * 49 53 8
	 * 0 11 42
	 * 42 0 7
	 * 57 7 4
	 * 
	 * water-to-light map:
	 * 88 18 7
	 * 18 25 70
	 * 
	 * light-to-temperature map:
	 * 45 77 23
	 * 81 45 19
	 * 68 64 13
	 * 
	 * temperature-to-humidity map:
	 * 0 69 1
	 * 1 0 69
	 * 
	 * humidity-to-location map:
	 * 60 56 37
	 * 56 93 4
	 * 
	 */

	private static final String INPUT_RX_SEEDS = "^seeds: ([ 0-9]+)$";
	private static final String INPUT_RX_MAP   = "^([a-z]+)-to-([a-z]+) map:$";
	private static final String INPUT_RX_RANGE = "^([0-9]+) ([0-9]+) ([0-9]+)$";
	
	public static record InputData(
			List<Long> seeds, 
			String mapFromName,	String mapToName, 
			Long rangeToStart, Long rangeFromStart, Long rangeLength
	) {
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			if (seeds != null) {
				result.append("seeds:");
				for (long seed:seeds) {
					result.append(" ").append(Long.toString(seed));
				}
			}
			if (mapFromName != null) {
				result.append(mapFromName).append("-to-").append(mapToName).append(" map:");
			}
			if (rangeFromStart != null) {
				result.append(Long.toString(rangeToStart)).append(" ").append(Long.toString(rangeFromStart)).append(" ").append(Long.toString(rangeLength));
			}
			return result.toString();
		}

		public boolean hasSeed() { return seeds != null; }
		public boolean hasMappingNames() { return mapFromName != null; }
		public boolean hasRange() { return rangeToStart != null; }
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
			if (line.matches(INPUT_RX_SEEDS)) {
				String[] seedsArray = line.replaceFirst(INPUT_RX_SEEDS, "$1").split(" +");
				List<Long> seeds = new ArrayList<>();
				for (String seed:seedsArray) {
					seeds.add(Long.parseLong(seed));
				}
				return new InputData(seeds, null, null, null, null, null);
			}
			else if (line.matches(INPUT_RX_MAP)) {
				String mapFromName = line.replaceFirst(INPUT_RX_MAP, "$1");
				String mapToName = line.replaceFirst(INPUT_RX_MAP, "$2");
				return new InputData(null, mapFromName, mapToName, null, null, null);
			}
			else if (line.matches(INPUT_RX_RANGE)) {
				long rangeToStart = Long.parseLong(line.replaceFirst(INPUT_RX_RANGE, "$1"));
				long rangeFromStart = Long.parseLong(line.replaceFirst(INPUT_RX_RANGE, "$2"));
				long rangeLength = Long.parseLong(line.replaceFirst(INPUT_RX_RANGE, "$3"));
				return new InputData(null, null, null, rangeToStart, rangeFromStart, rangeLength);
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}

	static record RangeMapping(String sourceName, String targetName, long sourceIndexStart, long sourceIndexEnd, long targetIndexStart) {
		public Material xmap(Material sourceMat) {
			if (sourceName.equals(sourceMat.matName) && (sourceIndexStart <= sourceMat.index) && (sourceMat.index <= sourceIndexEnd)) {
				return new Material(targetName, targetIndexStart+sourceMat.index-sourceIndexStart);
			}
			return null;
		}
		public Material qMap(Material sourceMat) {
			if ((sourceIndexStart <= sourceMat.index) && (sourceMat.index <= sourceIndexEnd)) {
				return new Material(targetName, targetIndexStart+sourceMat.index-sourceIndexStart);
			}
			return null;
		}
	}
	static record Material(String matName, long index) {
		@Override public String toString() {return matName+"-"+index; }
	}

	public static record RangeWithOffset(long from, long to, long offset) {
		public List<RangeWithOffset> intersect(RangeWithOffset other) {
			List<RangeWithOffset> result = new ArrayList<>();
			long iFrom = Math.max(from, other.from);
			long iTo = Math.min(to, other.to);
			if (iFrom <= iTo) {
				result.add(new RangeWithOffset(iFrom, iTo, offset));
			}
			return result;
		}
	}
	
	static class MaterialRange {
		String matName;
		List<RangeWithOffset> ranges;
	}

	static class World {
		List<Long> seeds;
		Map<String, List<RangeMapping>> materialRangeMappings = new HashMap<>();
		String currentSource;
		String currentTarget;
		public World() {
			materialRangeMappings = new HashMap<>();
		}
		public void setSeeds(List<Long> seeds) {
			this.seeds = seeds;
		}
		public void startMapping(String source, String target) {
			this.currentSource = source;
			this.currentTarget = target;
		}
		public void addRange(long targetIndex, long sourceIndex, long length) {
			List<RangeMapping> rMaps = materialRangeMappings.getOrDefault(currentSource, new ArrayList<>());
			rMaps.add(new RangeMapping(currentSource, currentTarget, sourceIndex, sourceIndex+length-1, targetIndex));
			materialRangeMappings.put(currentSource, rMaps);
		}
		public Material map(Material sourceMat) {
			List<RangeMapping> rangeMappings = materialRangeMappings.get(sourceMat.matName);
			if (rangeMappings == null) {
				return null;
			}
			for (RangeMapping rMap:rangeMappings) {
				Material targetMat = rMap.qMap(sourceMat);
				if (targetMat != null) {
					return targetMat;
				}
			}
			return new Material(rangeMappings.get(0).targetName, sourceMat.index);
		}
		public long getLowestLocation() {
			long result = Long.MAX_VALUE;
			for (long seed:seeds) {
				Material mat = new Material("seed", seed);
				System.out.println(mat);
				while (!mat.matName.equals("location")) {
					mat = map(mat);
					System.out.println(mat);
				}
				result = Math.min(result, mat.index);
			}
			return result;
		}
		public long getLowestSeedRangeLocation() {
			long result = Long.MAX_VALUE;
			for (int i=0; i<seeds.size(); i+=2) {
				long seedFrom = seeds.get(i);
				long seedTo = seedFrom+seeds.get(i+1)-1;
				System.out.println("seed from "+seedFrom+" to "+seedTo);
				for (long seed=seedFrom; seed<=seedTo; seed++) {
					Material mat = new Material("seed", seed);
//					System.out.println(mat);
					while (!mat.matName.equals("location")) {
						mat = map(mat);
//						System.out.println(mat);
					}
					result = Math.min(result, mat.index);
				}
			}
			return result;
		}
	}
	
	public static void mainPart1(String inputFile) {
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			if (data.hasSeed()) {
				world.setSeeds(data.seeds);
			}
			else if (data.hasMappingNames()) {
				world.startMapping(data.mapFromName, data.mapToName);
			}
			else if (data.hasRange()) {
				world.addRange(data.rangeToStart, data.rangeFromStart, data.rangeLength);
			}
		}
		System.out.println();
		System.out.println("MINIMUM LOCATION: "+world.getLowestLocation());
	}
	
	
	public static void mainPart2(String inputFile) {
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			if (data.hasSeed()) {
				world.setSeeds(data.seeds);
			}
			else if (data.hasMappingNames()) {
				world.startMapping(data.mapFromName, data.mapToName);
			}
			else if (data.hasRange()) {
				world.addRange(data.rangeToStart, data.rangeFromStart, data.rangeLength);
			}
		}
		System.out.println();
		System.out.println("MINIMUM LOCATION: "+world.getLowestSeedRangeLocation());
	}

	

	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day05/Feri/input-example.txt");
		mainPart1("exercises/day05/Feri/input.txt");             
		System.out.println("---------------");
		System.out.println("--- PART II ---");
		mainPart2("exercises/day05/Feri/input-example.txt");
//		mainPart2("exercises/day05/Feri/input.txt");     
		System.out.println("---------------");    // 
	}
	
}
