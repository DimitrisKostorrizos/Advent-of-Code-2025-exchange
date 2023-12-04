import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AoCDay02 {

    public static void main(String[] args) {

        List<String> stringList = readTextFile(args[0]);

        // Part 1:
        System.out.println("Part 1");
        long start = System.currentTimeMillis();

        List<Set<Map<String, Integer>>> gameList = new ArrayList<>();
        Map<String, Integer> newColorMap;
        Map<String, Integer> bagMap = new HashMap<>();
        bagMap.put("red", 12);
        bagMap.put("green", 13);
        bagMap.put("blue", 14);
        Set<Map<String, Integer>> newCubeSet;
        int idCount = 0;
        String[] parts, parts1, parts2;
        int gameId = 1;
        boolean possible;
        for (String line : stringList) {
            newCubeSet = new HashSet<>();
            parts = line.split(": ");
            parts = parts[1].split("; ");
            for (String setString : parts) {
                newColorMap = new HashMap<>();
                parts1 = setString.split(", ");
                for (String colorString : parts1) {
                    parts2 = colorString.split(" ");
                    newColorMap.put(parts2[1], Integer.parseInt(parts2[0]));
                }
                newCubeSet.add(newColorMap);
            }
            gameList.add(newCubeSet);
            possible = true;
            for (Map<String, Integer> colorMap : newCubeSet) {
                for (String color : colorMap.keySet()) {
                    if (colorMap.get(color) > bagMap.get(color))
                        possible = false;
                }
            }
            if (possible) {
                idCount += gameId;
            }
            System.out.println("game "+gameId+": "+possible);
            gameId++;
        }
        System.out.println(idCount);

        long finish = System.currentTimeMillis();
        System.out.println("runtime: "+(finish-start));
        System.out.println();


        // Part 2:
        System.out.println("Part 2");
        start = System.currentTimeMillis();

        int powerSum = 0;
        int power;
        for (Set<Map<String, Integer>> cubeSet : gameList) {
            power = 1;
            bagMap = new HashMap<>();
            for (Map<String, Integer> colorMap : cubeSet) {
                for (String color : colorMap.keySet()) {
                    if (colorMap.get(color) > bagMap.getOrDefault(color, 0)) {
                        bagMap.put(color, colorMap.get(color));
                    }
                }
            }
            for (String color : bagMap.keySet()) {
                power *= bagMap.get(color);
            }
            powerSum += power;
        }
        System.out.println(powerSum);

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


}
