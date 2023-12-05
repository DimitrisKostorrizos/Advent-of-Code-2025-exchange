
public class MyMapRule {

    public long destination_start;
    public long source_start;
    public long range_len;

    public MyMapRule(long destination_start, long source_start, long range_len) {
        this.destination_start = destination_start;
        this.source_start = source_start;
        this.range_len = range_len;
    }

    public Long doMap(Long l) {
        long rel_pos = l-this.source_start;
        return (rel_pos>=0) && (rel_pos<this.range_len) ? (this.destination_start + rel_pos) : null;
    }

}
