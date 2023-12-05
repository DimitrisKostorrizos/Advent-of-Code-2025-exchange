import java.text.MessageFormat;

public class MyRange {

    public Long range_start;
    public Long range_length;

    public MyRange(Long range_start, Long range_length) {
        this.range_start = range_start;
        this.range_length = range_length;
    }

    @Override
    public String toString() {
        return MessageFormat.format("[{0},{1}]", this.range_start, this.range_length);
    }

}
