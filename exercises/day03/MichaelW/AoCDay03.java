import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static java.lang.Character.isDigit;

public class AoCDay03 {

    public static void main(String[] args) {

        List<String> stringList = readTextFile(args[0]);

        // Part 1:
        System.out.println("Part 1");
        long start = System.currentTimeMillis();

        Set<Point> symbolSet = new HashSet<>();
        int y=0;
        for (String line : stringList) {
            for (int x=0; x<line.length(); x++) {
                if ((line.charAt(x) != '.') && !isDigit(line.charAt(x))) {
                    symbolSet.add(new Point(x,y));
                }
            }
            y++;
        }

        int partSum = 0;
        int currentNumber;
        boolean numberFound;
        boolean hasSymbol = false;
        y=0;
        for (String line : stringList) {
            currentNumber = 0;
            numberFound = false;
            for (int x=0; x<line.length(); x++) {
                if (isDigit(line.charAt(x))) {
                    if (!numberFound) hasSymbol = false;
                    currentNumber = 10*currentNumber + (Integer.parseInt(line.substring(x, x+1)));
                    numberFound = true;
                    hasSymbol = hasSymbol || hasAdjacentSymbol(x,y,symbolSet);
                } else {
                    if (numberFound) {
                        if (hasSymbol) partSum += currentNumber;
                        currentNumber = 0;
                        numberFound = false;
                    }
                }
            }
            if (numberFound && hasSymbol) partSum += currentNumber;
            y++;
        }
        System.out.println(partSum);

        long finish = System.currentTimeMillis();
        System.out.println("runtime: "+(finish-start));
        System.out.println();


        // Part 2:
        System.out.println("Part 2");
        start = System.currentTimeMillis();

        symbolSet = new HashSet<>();
        y=0;
        for (String line : stringList) {
            for (int x=0; x<line.length(); x++) {
                if (line.charAt(x) == '*') {
                    symbolSet.add(new Point(x,y));
                }
            }
            y++;
        }

        Map<Point, Set<Integer>> gearMap = new HashMap<>();
        Set<Integer> currentSet;
        Point currentPoint = new Point(0,0);
        Point adjacentSymbol;
        hasSymbol = false;
        y=0;
        for (String line : stringList) {
            currentNumber = 0;
            numberFound = false;
            for (int x=0; x<line.length(); x++) {
                if (isDigit(line.charAt(x))) {
                    if (!numberFound) {
                        hasSymbol = false;
                    }
                    currentNumber = 10*currentNumber + (Integer.parseInt(line.substring(x, x+1)));
                    numberFound = true;
                    adjacentSymbol = getAdjacentSymbol(x,y,symbolSet);
                    if (adjacentSymbol != null) {
                        currentPoint = adjacentSymbol;
                        hasSymbol = true;
                    }
                } else {
                    if (numberFound) {
                        if (hasSymbol) {
                            currentSet = gearMap.getOrDefault(currentPoint, new HashSet<>());
                            currentSet.add(currentNumber);
                            gearMap.put(currentPoint, currentSet);
                        }
                        currentNumber = 0;
                        numberFound = false;
                    }
                }
            }
            if (numberFound && hasSymbol) {
                currentSet = gearMap.getOrDefault(currentPoint, new HashSet<>());
                currentSet.add(currentNumber);
                gearMap.put(currentPoint, currentSet);
            }
            y++;
        }

        int sum = 0;
        int product;
        for (Set<Integer> gearSet : gearMap.values()) {
            if (gearSet.size() == 2) {
                product = 1;
                for (int value : gearSet) {
                    product *= value;
                }
                sum += product;
            }
        }
        System.out.println(sum);

        finish = System.currentTimeMillis();
        System.out.println("runtime: "+(finish-start));
    }

    private static boolean hasAdjacentSymbol(int x, int y, Set<Point> symbolSet) {
        boolean hasSymbol = symbolSet.contains(new Point(x - 1, y - 1));
        if (symbolSet.contains(new Point(x, y-1))) hasSymbol = true;
        if (symbolSet.contains(new Point(x+1, y-1))) hasSymbol = true;
        if (symbolSet.contains(new Point(x-1, y))) hasSymbol = true;
        if (symbolSet.contains(new Point(x+1, y))) hasSymbol = true;
        if (symbolSet.contains(new Point(x-1, y+1))) hasSymbol = true;
        if (symbolSet.contains(new Point(x, y+1))) hasSymbol = true;
        if (symbolSet.contains(new Point(x+1, y+1))) hasSymbol = true;
        return hasSymbol;
    }

    private static Point getAdjacentSymbol(int x, int y, Set<Point> symbolSet) {
        Point symbol = null;
        Point point;
        point = new Point(x - 1, y - 1);
        if (symbolSet.contains(point)) symbol = point;
        point = new Point(x, y - 1);
        if (symbolSet.contains(point)) symbol = point;
        point = new Point(x + 1, y - 1);
        if (symbolSet.contains(point)) symbol = point;
        point = new Point(x - 1, y);
        if (symbolSet.contains(point)) symbol = point;
        point = new Point(x + 1, y);
        if (symbolSet.contains(point)) symbol = point;
        point = new Point(x - 1, y + 1);
        if (symbolSet.contains(point)) symbol = point;
        point = new Point(x, y + 1);
        if (symbolSet.contains(point)) symbol = point;
        point = new Point(x + 1, y + 1);
        if (symbolSet.contains(point)) symbol = point;
        return symbol;
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
