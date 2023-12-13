import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
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
		@Override public String toString() {
			long offset = (targetIndexStart-sourceIndexStart);
			String offsetString = offset == 0 ? "" : (offset < 0 ? "|"+offset : "|+"+offset);
			return sourceName+"->"+targetName+"["+sourceIndexStart+".."+sourceIndexEnd+offsetString+"]";
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
		@Override public String toString() {
			if (offset == 0) {
				return "["+from+".."+to+"]";				
			}
			return "["+from+".."+to+"|+"+offset+"]";
		}
		public List<RangeWithOffset> applyRangeMapping(RangeMapping rangeMapping) {
			List<RangeWithOffset> result = new ArrayList<>();
			long iFrom = Math.max(from, rangeMapping.sourceIndexStart);
			long iTo = Math.min(to, rangeMapping.sourceIndexEnd);
			if (iFrom <= iTo) {
				result.add(new RangeWithOffset(iFrom, iTo, rangeMapping.targetIndexStart-rangeMapping.sourceIndexStart));
			}
			return result;
		}
		public RangeWithOffset applyOffset() {
			return new RangeWithOffset(from+offset, to+offset, 0);
		}
		public List<RangeWithOffset> subtract(RangeWithOffset other) {
			List<RangeWithOffset> result = new ArrayList<>();
			long toLeft = other.from-1;
			long fromRight = other.to+1;
			if ((toLeft >= from) && (toLeft <= to)) {
				result.add(new RangeWithOffset(from, toLeft, offset));
			}
			if ((fromRight <= to) && (fromRight >= from)) {
				result.add(new RangeWithOffset(fromRight, to, offset));
			}
			return result;
		}
		public List<RangeWithOffset> subtract(RangeMapping rangeMapping) {
			List<RangeWithOffset> result = new ArrayList<>();
			long toLeft = rangeMapping.sourceIndexStart-1;
			long fromRight = rangeMapping.sourceIndexEnd+1;
			if ((toLeft >= from) && (toLeft <= to)) {
				result.add(new RangeWithOffset(from, toLeft, offset));
			}
			if ((fromRight <= to) && (fromRight >= from)) {
				result.add(new RangeWithOffset(fromRight, to, offset));
			}
			return result;
		}
		public boolean overlaps(RangeMapping rangeMapping) {
			return (rangeMapping.sourceIndexStart <= to) && (rangeMapping.sourceIndexEnd >= from);
		}
	}
	
	static class MaterialRanges {
		String matName;
		List<RangeWithOffset> ranges;
		public MaterialRanges(String matName) {
			this.matName = matName;
			ranges = new ArrayList<>();
		}
		public void addRange(RangeWithOffset range) {
			ranges.add(range);
		}
		public void addRanges(Collection<RangeWithOffset> newRanges) {
			ranges.addAll(newRanges);
		}
		public long minFrom() {
			long result = Long.MAX_VALUE;
			for (RangeWithOffset range:ranges) {
				result = Math.min(result, range.from);
			}
			return result;
		}
		@Override public String toString() {
			StringBuilder result = new StringBuilder();
			result.append(matName).append("{");
			for (RangeWithOffset range:ranges) {
				result.append(range);
			}
			result.append("}");
			return result.toString();
		}
		public void addIdentityMappings(RangeWithOffset sourceRange, List<RangeMapping> rangeMappings) {
			List<RangeWithOffset> idMappings = new ArrayList<>();
			idMappings.add(sourceRange);
			boolean idMappingsChanged = true;
			while (idMappingsChanged) {
				idMappingsChanged = false;
				for (RangeWithOffset idMapping:idMappings) {
					for (RangeMapping rangeMapping:rangeMappings) {
						if (idMapping.overlaps(rangeMapping)) {
							idMappings.remove(idMapping);
							idMappings.addAll(idMapping.subtract(rangeMapping));
							idMappingsChanged = true;
							break;
						}
					}
					if (idMappingsChanged) {
						break;
					}
				}
			}
			ranges.addAll(idMappings);
		}
		public void applyOffsets() {
			List<RangeWithOffset> newRanges = new ArrayList<>();
			for (RangeWithOffset range:ranges) {
				newRanges.add(range.applyOffset());
			}
			ranges = newRanges;
		}
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
		
		private MaterialRanges mapR(MaterialRanges sourceMatR) {
			List<RangeMapping> rangeMappings = materialRangeMappings.get(sourceMatR.matName);
			if (rangeMappings == null) {
				return null;
			}
			System.out.println("MAPPING: " + rangeMappings);
			MaterialRanges result = new MaterialRanges(rangeMappings.get(0).targetName);
			for (RangeWithOffset sourceRange:sourceMatR.ranges) {
				for (RangeMapping rangeMapping:rangeMappings) {
					List<RangeWithOffset> appliedRanges = sourceRange.applyRangeMapping(rangeMapping);
					result.addRanges(appliedRanges);
				}
				result.addIdentityMappings(sourceRange, rangeMappings);
			}
			result.applyOffsets();
			return result;
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
			MaterialRanges matR = new MaterialRanges("seed"); 
			for (int i=0; i<seeds.size(); i+=2) {
				long seedFrom = seeds.get(i);
				long seedTo = seedFrom+seeds.get(i+1)-1;
				matR.addRange(new RangeWithOffset(seedFrom, seedTo, 0));
			}
			System.out.println(matR);
			while (!matR.matName.equals("location")) {
				matR = mapR(matR);
				System.out.println(matR);
			}
			result = Math.min(result, matR.minFrom());
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
