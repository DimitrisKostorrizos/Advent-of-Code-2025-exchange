import java.util.ArrayList;
import java.util.List;

public class MyMap {

    public String mapName;
    private List<MyMapRule> rules = new ArrayList<>();

    public MyMap(String mapName) {
        this.mapName = mapName;
        this.rules.add(new MyMapRule(0,0,Long.MAX_VALUE));
    }

    public void addRule(long destination_start, long source_start, long range_len) {
        this.rules.add(0,new MyMapRule(destination_start, source_start, range_len));
    }

    public Long doMap(Long l) {
        for (MyMapRule rule:this.rules) {
            Long l2 = rule.doMap(l);
            if (l2 !=null) {
                return l2;
            }
        }
        return l;
    }

    public List<MyRange> doRangeMap(List<MyRange> ranges) {
        List<MyRange> ret = new ArrayList<>();
        for(var range:ranges) {
            ret.addAll(doRangeMap1(range));
        }
        return ret;
    }

    private List<MyRange> doRangeMap1(MyRange range) {
        List<MyRange> ret = new ArrayList<>();
        while (range.range_length>0) {
            for (MyMapRule rule:this.rules) {
                Long mappedStart = rule.doMap(range.range_start);
                if (mappedStart !=null) {
                    long mappedLen = Math.min(range.range_length, rule.destination_start + rule.range_len - mappedStart);
                    ret.add(new MyRange(mappedStart, mappedLen));
                    range = new MyRange(range.range_start+mappedLen, range.range_length-mappedLen);
                    break;
                }
            }
        }
        return ret;
    }
}
