import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MySolution extends MySolutionBase {


	private List<Long> seeds;
	private Map<String,MyMap> maps = new TreeMap<String,MyMap>();
	private List<MyRange> seedRanges;

	public MySolution(String inputFilename) {
        super(inputFilename);
		MyMap curMap = null;
		for (String line:getInputLinesAsList()) {
			if (line.startsWith("seeds:")) {
				this.seeds = Arrays.asList(line.split(":")[1].trim().split("\\s+")).stream().map(Long::valueOf).collect(Collectors.toList());
			} else if (line.matches(".* map:")) {
				var curMapName = line.split(" map:")[0];
				curMap = new MyMap(curMapName);
				this.maps.put(curMapName,curMap);
			} else {
				var sp = line.trim().split(" ");
				if (sp.length==3) {
					curMap.addRule(Long.parseLong(sp[0]),Long.parseLong(sp[1]),Long.parseLong(sp[2]));
				}
			}
		};

    }

    private MySolution play1() {
		var locations = this.seeds.stream()
			.map(l->this.maps.get("seed-to-soil").doMap(l))
			.map(l->this.maps.get("soil-to-fertilizer").doMap(l))
			.map(l->this.maps.get("fertilizer-to-water").doMap(l))
			.map(l->this.maps.get("water-to-light").doMap(l))
			.map(l->this.maps.get("light-to-temperature").doMap(l))
			.map(l->this.maps.get("temperature-to-humidity").doMap(l))
			.map(l->this.maps.get("humidity-to-location").doMap(l))
			.collect(Collectors.toList());
		System.out.println(locations.stream().mapToLong(Long::valueOf).min().orElse(0L));
        return this;
	}

	private MySolution play2() {
		this.seedRanges = new ArrayList<>();
		for ( int i=0;i<this.seeds.size()/2;i++) {
			this.seedRanges.add(new MyRange(this.seeds.get(2*i), this.seeds.get(2*i+1)));
		}

		var locationsRanges = Arrays.asList(this.seedRanges).stream()
			.map(l->this.maps.get("seed-to-soil").doRangeMap(l))
			.map(l->this.maps.get("soil-to-fertilizer").doRangeMap(l))
			.map(l->this.maps.get("fertilizer-to-water").doRangeMap(l))
			.map(l->this.maps.get("water-to-light").doRangeMap(l))
			.map(l->this.maps.get("light-to-temperature").doRangeMap(l))
			.map(l->this.maps.get("temperature-to-humidity").doRangeMap(l))
			.map(l->this.maps.get("humidity-to-location").doRangeMap(l))
			.collect(Collectors.toList());

		System.out.println(locationsRanges.get(0).stream().mapToLong(r->r.range_start).min().orElse(0L));
        return this;
	}

	public static void main(String args[]) {
		try {
            new MySolution("sample.txt").play1().play2();
            new MySolution("input.txt").play1().play2();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
