import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AoCDay04 {

    public static void main(String[] args) {

        List<String> stringList = readTextFile(args[0]);

        // Part 1:
        System.out.println("Part 1");
        long start = System.currentTimeMillis();

        String[] parts, winningNumbers, numbersYouHave;
        Set<Integer> winningSet, numbersSet;
        int pointSum = 0;
        for (String line : stringList) {
            line = line.replace("  ", " ");
            parts = line.split(": ");
            line = parts[1];
            winningSet = new HashSet<>();
            numbersSet = new HashSet<>();
            parts = line.split(" \\| ");
            winningNumbers = parts[0].split(" ");
            for (String numberString : winningNumbers) {
                winningSet.add(Integer.parseInt(numberString));
            }
            numbersYouHave = parts[1].split(" ");
            for (String numberString : numbersYouHave) {
                numbersSet.add(Integer.parseInt(numberString));
            }
            int value = 0;
            for (int number : numbersSet) {
                if (winningSet.contains(number)) {
                    if (value == 0) {
                        value++;
                    } else {
                        value *= 2;
                    }
                }
            }
            pointSum += value;
        }
        System.out.println(pointSum);


        long finish = System.currentTimeMillis();
        System.out.println("runtime: "+(finish-start));
        System.out.println();


        // Part 2:
        System.out.println("Part 2");
        start = System.currentTimeMillis();

        String line;
        List<Integer> multList = new ArrayList<>();
        for (int i=0; i<stringList.size(); i++) {
            multList.add(1);
        }
        for (int i=0; i<stringList.size(); i++) {
            line = stringList.get(i).replace("  ", " ");
            parts = line.split(": ");
            line = parts[1];
            winningSet = new HashSet<>();
            numbersSet = new HashSet<>();
            parts = line.split(" \\| ");
            winningNumbers = parts[0].split(" ");
            for (String numberString : winningNumbers) {
                winningSet.add(Integer.parseInt(numberString));
            }
            numbersYouHave = parts[1].split(" ");
            for (String numberString : numbersYouHave) {
                numbersSet.add(Integer.parseInt(numberString));
            }
            int value = 0;
            for (int number : numbersSet) {
                if (winningSet.contains(number)) {
                    value++;
                }
            }
            for (int k=0; k<multList.get(i); k++) {
                for (int j=i+1; (j<i+1+value) && (j<multList.size()); j++) {
                    multList.set(j, multList.get(j)+1);
                }
            }
        }
        pointSum = 0;
        for (Integer integer : multList) {
            pointSum += integer;
        }
        System.out.println(pointSum);


        finish = System.currentTimeMillis();
        System.out.println("runtime: "+(finish-start));
    }

    private static List<String> readTextFile(String sourceFile) {
        String line;
        List<String> resultList = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(sourceFile));
            while ((line = in.readLine()) != null)
                resultList.add(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    private record Room(String name, int sectorId, String checkSum) {
    }


}
