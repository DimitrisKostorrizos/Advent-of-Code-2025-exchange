import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Advent of Code 2025 - Day 4: Printing Department (Part 2)
 * 
 * Simulate removing accessible rolls repeatedly until none remain accessible.
 * A roll is accessible if it has fewer than 4 adjacent rolls.
 */
public class SolutionPart2 {

    private static final int[][] DIRECTIONS = {
        {-1, 0}, {-1, 1}, {0, 1}, {1, 1},
        {1, 0}, {1, -1}, {0, -1}, {-1, -1}
    };

    public static void main(String[] args) {
        String filename = args.length > 0 ? args[0] : "input.txt";
        
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            char[][] grid = parseGrid(lines);
            int result = countTotalRemovable(grid);
            System.out.println("Part 2: " + result);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static char[][] parseGrid(List<String> lines) {
        List<String> nonEmpty = lines.stream()
            .filter(line -> !line.isEmpty())
            .toList();
        
        int rows = nonEmpty.size();
        int cols = nonEmpty.isEmpty() ? 0 : nonEmpty.get(0).length();
        char[][] grid = new char[rows][cols];
        
        for (int r = 0; r < rows; r++) {
            String line = nonEmpty.get(r);
            for (int c = 0; c < Math.min(cols, line.length()); c++) {
                grid[r][c] = line.charAt(c);
            }
        }
        return grid;
    }

    /**
     * Repeatedly remove all accessible rolls until none can be removed.
     * Returns total count of removed rolls.
     */
    private static int countTotalRemovable(char[][] grid) {
        int rows = grid.length;
        if (rows == 0) return 0;
        int cols = grid[0].length;
        
        int totalRemoved = 0;
        
        while (true) {
            // Find all currently accessible rolls
            List<int[]> toRemove = new ArrayList<>();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (grid[r][c] == '@' && isAccessible(grid, r, c)) {
                        toRemove.add(new int[]{r, c});
                    }
                }
            }
            
            if (toRemove.isEmpty()) break;
            
            // Remove all accessible rolls
            for (int[] pos : toRemove) {
                grid[pos[0]][pos[1]] = '.';
            }
            totalRemoved += toRemove.size();
        }
        
        return totalRemoved;
    }

    private static boolean isAccessible(char[][] grid, int row, int col) {
        int adjacentRolls = 0;
        int rows = grid.length;
        int cols = grid[0].length;
        
        for (int[] dir : DIRECTIONS) {
            int nr = row + dir[0];
            int nc = col + dir[1];
            
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                if (grid[nr][nc] == '@') {
                    adjacentRolls++;
                }
            }
        }
        return adjacentRolls < 4;
    }
}
