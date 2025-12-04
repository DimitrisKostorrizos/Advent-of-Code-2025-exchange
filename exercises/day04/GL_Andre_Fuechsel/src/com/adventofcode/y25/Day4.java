package com.adventofcode.y25;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Day4 {

    public static void main(String[] args) throws IOException {
        Day4 day4 = new Day4();
        day4.execute();
    }

    public void execute() throws IOException {
        List<String> lines = Files.readAllLines(Path.of("input.txt"));

        // create grid with empty lines all around
        int width = lines.get(1).length() + 2;
        int height = lines.size() + 2;
        char[][] grid = new char[height][width];

        // init grid
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = '.';
            }
        }

        // fill grid
        for (int y = 0; y < height - 2; y++) {
            for (int x = 0; x < width - 2; x++) {
                grid[y + 1][x + 1] = lines.get(y).charAt(x);
            }
        }

        long maxRolls1 = checkGrid(grid);

        // for part 2, repeat until no more rolls possible
        long maxRolls2 = maxRolls1;
        while (removeRolls(grid) > 0) {
            maxRolls2 += checkGrid(grid);
        }

        System.out.println("Maximum rolls (solution part 1): " + maxRolls1);
        System.out.println("Maximum rolls (solution part 2): " + maxRolls2);
    }

    private long checkGrid(char[][] grid) {
        long rolls = 0;

        for (int y = 1; y < grid.length - 1; y++) {
            for (int x = 1; x < grid[y].length - 1; x++) {
                if (grid[y][x] == '@') {
                    if (checkCell(grid, y, x) < 4) {
                        rolls++;
                        grid[y][x] = 'x';   // can be removed
                    }
                }
            }
        }
        return rolls;
    }

    private int checkCell(char[][] grid, int y, int x) {
        int count = 0;

        // check all 8 directions
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dy == 0 && dx == 0) continue; // skip self
                if (grid[y + dy][x + dx] != '.') {
                    count++;
                }
            }
        }

        return count;
    }

    private int removeRolls(char[][] grid) {
        int removed = 0;

        for (int y = 1; y < grid.length - 1; y++) {
            for (int x = 1; x < grid[y].length - 1; x++) {
                if (grid[y][x] == 'x') {
                    grid[y][x] = '.';   // remove roll
                    removed++;
                }
            }
        }
        return removed;
    }
}