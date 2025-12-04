import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Advent of Code 2025 - Day 4: Printing Department
 * 
 * Count paper rolls (@) that can be accessed by forklifts.
 * A roll is accessible if fewer than 4 rolls are in adjacent positions.
 */
public class Solution {

    // 8 directions: N, NE, E, SE, S, SW, W, NW
    private static final int[][] DIRECTIONS = {
        {-1, 0}, {-1, 1}, {0, 1}, {1, 1},
        {1, 0}, {1, -1}, {0, -1}, {-1, -1}
    };

    public static void main(String[] args) {
        String filename = args.length > 0 ? args[0] : "input.txt";
        
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            char[][] grid = parseGrid(lines);
            int result = countAccessibleRolls(grid);
            System.out.println("Part 1: " + result);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static char[][] parseGrid(List<String> lines) {
        // Filter empty lines and convert to 2D array
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

    private static int countAccessibleRolls(char[][] grid) {
        int rows = grid.length;
        if (rows == 0) return 0;
        int cols = grid[0].length;
        
        int accessible = 0;
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == '@' && isAccessible(grid, r, c)) {
                    accessible++;
                }
            }
        }
        return accessible;
    }

    /**
     * A roll is accessible if fewer than 4 adjacent positions contain rolls.
     */
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
