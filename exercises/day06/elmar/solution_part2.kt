import java.io.File

/*
 * Advent of Code 2025 - Day 6: Trash Compactor (Part 2)
 *
 * Cephalopod math: each COLUMN is a number (digits read top-to-bottom).
 * Problems still separated by all-space columns, operator at bottom.
 */

data class Problem(val numbers: List<Long>, val operator: Char)

fun parseProblems(lines: List<String>): List<Problem> {
    if (lines.isEmpty()) return emptyList()

    val width = lines.maxOf { it.length }
    val grid = lines.map { it.padEnd(width) }

    val problems = mutableListOf<Problem>()
    var col = 0

    while (col < width) {
        // Skip separator columns (all spaces)
        if (grid.all { it[col] == ' ' }) {
            col++
            continue
        }

        // Find extent of this problem (until all-space column)
        val startCol = col
        while (col < width && !grid.all { it[col] == ' ' }) {
            col++
        }

        // Find operator (bottom row, within problem columns)
        var operator = '+'
        for (c in startCol until col) {
            val char = grid.last()[c]
            if (char == '+' || char == '*') {
                operator = char
                break
            }
        }

        // Each column forms a number (digits top-to-bottom, excluding operator row)
        val numbers = mutableListOf<Long>()
        for (c in startCol until col) {
            val digits = StringBuilder()
            for (row in 0 until grid.size - 1) {
                val char = grid[row][c]
                if (char.isDigit()) {
                    digits.append(char)
                }
            }
            if (digits.isNotEmpty()) {
                numbers.add(digits.toString().toLong())
            }
        }

        if (numbers.isNotEmpty()) {
            problems.add(Problem(numbers, operator))
        }
    }

    return problems
}

fun solve(filename: String): Long {
    val lines = File(filename).readLines()
    val problems = parseProblems(lines)

    return problems.sumOf { p ->
        when (p.operator) {
            '+' -> p.numbers.sum()
            '*' -> p.numbers.reduce { acc, n -> acc * n }
            else -> 0L
        }
    }
}

fun main(args: Array<String>) {
    val filename = args.firstOrNull() ?: "input.txt"
    val result = solve(filename)
    println("Part 2: $result")
}
