namespace Y25Day10
{
    /// <summary>
    /// Represents a solver that uses Gaussian elimination mod 2 
    /// to solve a system of linear equations represented by a matrix and a target vector.
    /// </summary>
    public sealed class GaussianEliminationModTwoSolver
    {
        /// <summary>
        /// The cache for the generated binary numbers
        /// </summary>
        private static readonly Dictionary<int, List<List<int>>> BinaryNumberCache = [];

        /// <summary>
        /// The matrix
        /// </summary>
        private readonly int[,] _matrix;

        /// <summary>
        /// The target
        /// </summary>
        private readonly int[] _target;

        /// <summary>
        /// The number of rows in the <see cref="_matrix"/>
        /// </summary>
        private readonly int _numberOfRows;

        /// <summary>
        /// The number of columns in the <see cref="_matrix"/>
        /// </summary>
        private readonly int _numberOfColumns;

        /// <summary>
        /// Creates a new instance of <see cref="GaussianEliminationModTwoSolver"/>
        /// </summary>
        /// <param name="matrix">The matrix</param>
        /// <param name="target">The target</param>
        public GaussianEliminationModTwoSolver(int[,] matrix, int[] target)
        {
            ArgumentNullException.ThrowIfNull(matrix);

            ArgumentNullException.ThrowIfNull(target);

            _matrix = matrix;

            _target = target;

            _numberOfRows = matrix.GetLength(0);

            _numberOfColumns = matrix.GetLength(1);
        }

        /// <summary>
        /// Solves the system of linear equations represented by the matrix and target vector using Gaussian elimination mod 2. 
        /// It returns the minimum number of 1s in the solution vector, 
        /// which corresponds to the minimum number of button presses needed to achieve the desired state of the lights,
        /// taking into account any free variables that may exist in the system. 
        /// The method handles both the forward elimination and back substitution phases of Gaussian elimination,
        /// </summary>
        /// <returns></returns>
        public int Solve()
        {
            var pivotColumnPerRow = ForwardElimination();

            var freeVariablesPerColumn = BackSubstitution(pivotColumnPerRow);

            var freeVariableIndexes = freeVariablesPerColumn.SelectMany(x => x.Value).ToHashSet();

            var result = _target.Sum();

            var numberOfFreeVariables = freeVariableIndexes.Count;

            // If there no free variables, there is no need to check the null space...
            if (numberOfFreeVariables <= 0)
                return result;

            var nullSpaceSolutions = GenerateNullSpaceSolutions(freeVariableIndexes);

            // Apply the null space solutions to the target vector to find the solution with the minimum number of 1s,
            // which corresponds to the minimum number of button presses needed to achieve the desired state of the lights
            foreach (var nullSpaceSolution in nullSpaceSolutions)
            {
                foreach (var (row, column) in freeVariablesPerColumn.Select(x => x.Key))
                {
                    var pivot = _target[row];

                    for (var columnIndex = column + 1; columnIndex < _numberOfColumns; columnIndex++)
                    {
                        if (_matrix[row, columnIndex] == 1)
                            pivot ^= nullSpaceSolution[columnIndex];
                    }

                    nullSpaceSolution[column] = pivot;
                }

                var nullSpaceSolutionResult = nullSpaceSolution.Sum();

                result = Math.Min(result, nullSpaceSolutionResult);
            }

            return result;
        }

        /// <summary>
        /// Applies forward elimination to the matrix to convert it into an upper triangular form, 
        /// returning the pivot column index for each pivot row.
        /// </summary>
        /// <returns></returns>
        private Dictionary<int, int> ForwardElimination()
        {
            var pivotRow = 0;

            var isPivotRowSet = false;

            var pivotColumnPerRow = new Dictionary<int, int>();

            for (var columnIndex = 0; columnIndex < _numberOfColumns; columnIndex++)
            {
                for (var rowIndex = pivotRow; rowIndex < _numberOfRows; rowIndex++)
                {
                    // If the row is a valid pivot row...
                    if (_matrix[rowIndex, columnIndex] == 1)
                    {
                        // If the row is not in the correct position...
                        if (rowIndex != pivotRow)
                        {
                            // Swap rows
                            for (var i = 0; i < _numberOfColumns; i++)
                            {
                                (_matrix[pivotRow, i], _matrix[rowIndex, i]) = (_matrix[rowIndex, i], _matrix[pivotRow, i]);
                            }

                            (_target[pivotRow], _target[rowIndex]) = (_target[rowIndex], _target[pivotRow]);
                        }

                        pivotColumnPerRow.Add(pivotRow, columnIndex);

                        isPivotRowSet = true;
                    }

                    if (isPivotRowSet)
                        break;
                }

                if (!isPivotRowSet)
                    continue;

                // Eliminate the rows below the pivot
                for (var belowRowIndex = pivotRow + 1; belowRowIndex < _numberOfRows; belowRowIndex++)
                {
                    if (_matrix[belowRowIndex, columnIndex] == 1)
                    {
                        // XOR the row with the pivot row to eliminate the 1 in column i
                        for (var column = 0; column < _numberOfColumns; column++)
                            _matrix[belowRowIndex, column] ^= _matrix[pivotRow, column];

                        _target[belowRowIndex] ^= _target[pivotRow];
                    }
                }

                pivotRow++;

                isPivotRowSet = false;

                if (pivotRow == _numberOfRows)
                    break;
            }

            return pivotColumnPerRow;
        }

        /// <summary>
        /// Applies back substitution to the upper triangular matrix obtained from forward elimination
        /// mutating the <see cref="_target"/> to reflect the elimination of the pivot variables from the rows above
        /// </summary>
        /// <returns></returns>
        private Dictionary<KeyValuePair<int, int>, IEnumerable<int>> BackSubstitution(Dictionary<int, int> pivotColumnPerRow)
        {
            ArgumentNullException.ThrowIfNull(pivotColumnPerRow);

            var freeVariablesPerColumn = new Dictionary<KeyValuePair<int, int>, IEnumerable<int>>();

            var freeVariableIndexes = new HashSet<int>();

            foreach (var pivotCoordinates in pivotColumnPerRow)
            {
                var freeVariableIndexesPerRow = new HashSet<int>();

                var rowIndex = pivotCoordinates.Key;

                for (var columnIndex = pivotCoordinates.Value + 1; columnIndex < _numberOfColumns; columnIndex++)
                {
                    if (_matrix[rowIndex, columnIndex] == 1 && !pivotColumnPerRow.Any(x => x.Value == columnIndex))
                    {
                        freeVariableIndexesPerRow.Add(columnIndex);

                        freeVariableIndexes.Add(columnIndex);
                    }
                }

                freeVariablesPerColumn.Add(pivotCoordinates, freeVariableIndexesPerRow);
            }

            foreach (var (pivotRow, pivotColumn) in pivotColumnPerRow.OrderByDescending(x => x.Key))
            {
                // Back substitution to eliminate the variable from rows above
                for (var aboveRowIndex = pivotRow - 1; aboveRowIndex >= 0; aboveRowIndex--)
                {
                    if (_matrix[aboveRowIndex, pivotColumn] == 1)
                    {
                        for (var columnIndex = 0; columnIndex < _numberOfColumns; columnIndex++)
                            _matrix[aboveRowIndex, columnIndex] ^= _matrix[pivotRow, columnIndex];

                        _target[aboveRowIndex] ^= _target[pivotRow];
                    }
                }
            }

            return freeVariablesPerColumn;
        }

        /// <summary>
        /// Generates all the null-space solution based on the <paramref name="freeVariableIndexes"/>
        /// </summary>
        /// <param name="freeVariableIndexes">The free variable indexes</param>
        /// <returns></returns>
        private IEnumerable<int[]> GenerateNullSpaceSolutions(IEnumerable<int> freeVariableIndexes)
        {
            var nullSpaceSize = freeVariableIndexes.Count();

            if (nullSpaceSize == 0)
                yield break;

            var binaryNumbers = GenerateBinaryNumbers(nullSpaceSize);

            foreach (var binaryNumber in binaryNumbers)
            {
                var value = new int[_numberOfColumns];

                // Convert the binary number into the vector representation
                for (var freeVarableIndex = 0; freeVarableIndex < nullSpaceSize; freeVarableIndex++)
                {
                    var freeVariableIndex = freeVariableIndexes.ElementAt(freeVarableIndex);

                    value[freeVariableIndex] = binaryNumber[freeVarableIndex];
                }

                yield return value;
            }
        }

        /// <summary>
        /// Generates all the binary numbers with the specified length. 
        /// For example, if the length is 2, it will generate: 00, 01, 10, 11
        /// </summary>
        /// <param name="length">The length of the binary number</param>
        /// <returns></returns>
        private static List<List<int>> GenerateBinaryNumbers(int length)
        {
            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(length);

            if (BinaryNumberCache.TryGetValue(length, out var cacheResult))
                return cacheResult;

            var results = new List<List<int>>();

            for (var digitIndex = 0; digitIndex < length; digitIndex++)
            {
                if (results.Count == 0)
                {
                    results.Add([0]);

                    results.Add([1]);
                }
                else
                {
                    foreach (var result in results.ToArray())
                    {
                        var newResult = result.ToList();

                        result.Add(0);

                        newResult.Add(1);

                        results.Add(newResult);
                    }
                }
            }

            BinaryNumberCache.Add(length, results);

            return results;
        }
    }
}