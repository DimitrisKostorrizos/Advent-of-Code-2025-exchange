import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MySolutionBase {
	String inputFilename;
    Path inputPath;
    List<String> inputLines;
    public MySolutionBase(String inputFilename) {
        System.out.println("========== Processing "+inputFilename+" ==========");
        try {
            this.inputFilename = inputFilename;
            this.inputPath= Paths.get(MySolutionBase.class.getClassLoader().getResource(this.inputFilename).toURI());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Stream<String> getInputLinesAsStream() {
        try {
            return Files.lines(this.inputPath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Stream.of(new String[0]);
        }
    }

    
    public List<String> getInputLinesAsList() {
        if (this.inputLines==null) {
    		this.inputLines = this.getInputLinesAsStream().collect(Collectors.toList());
        }
        return this.inputLines;
    }
}
