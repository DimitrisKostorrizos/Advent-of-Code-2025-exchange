namespace Y25Day10
{
    /// <summary>
    /// Represents a solver that uses Hermite Normal Form (HNF) 
    /// to solve a system of linear equations represented by a matrix and a target vector.
    /// </summary>
    public sealed class HnfSolver
    {
        /// <summary>
        /// The tolerance used for rounding when calculating bounds
        /// </summary>
        private const double RoundingTolerance = 1e-6;

        /// <summary>
        /// The padding added to the bounds to ensure they are inclusive of the actual integer solutions
        /// </summary>
        private const int BoundPadding = 1;

        /// <summary>
        /// The threshold for treating reduced costs as negative (improving)
        /// </summary>
        private const double OptimalityTolerance = 1e-9;

        /// <summary>
        /// The threshold for treating pivot column entries as positive (valid pivots)
        /// </summary>
        private const double PivotTolerance = 1e-9;

        /// <summary>
        /// The threshold for treating ratios as tied in the min-ratio test
        /// </summary>
        private const double RatioTieTolerance = 1e-12;

        /// <summary>
        /// The maximum number of iterations allowed in the simplex algorithm to prevent infinite loops
        /// </summary>
        private const int MaxIterations = 5000;

        /// <summary>
        /// The Big-M penalty used in the simplex algorithm to force artificial variables out of the optimal basis.
        /// </summary>
        private const double BigM = 1e9;

        /// <summary>
        /// The joltage contribution per button
        /// </summary>
        private readonly int[][] _buttonJoltages;

        /// <summary>
        /// The target
        /// </summary>
        private readonly int[] _target;

        /// <summary>
        /// The number of buttons
        /// </summary>
        private readonly int _numberOfColumns;

        /// <summary>
        /// The number of joltages
        /// </summary>
        private readonly int _numberOfRows;

        /// <summary>
        /// Contains the largest number of times you can press it without overcharging any light it touches
        /// </summary>
        private readonly int[] _upperBounds;

        /// <summary>
        /// Creates a new instance of <see cref="HnfSolver"/>
        /// </summary>
        /// <param name="buttonJoltages">The joltages per button</param>
        /// <param name="target">The target</param>
        public HnfSolver(int[][] buttonJoltages, int[] target)
        {
            ArgumentNullException.ThrowIfNull(buttonJoltages);

            ArgumentNullException.ThrowIfNull(target);

            _buttonJoltages = buttonJoltages;

            _target = target;

            _numberOfColumns = buttonJoltages.Length;

            _numberOfRows = target.Length;

            _upperBounds = new int[_numberOfColumns];

            for (var buttonIndex = 0; buttonIndex < _numberOfColumns; buttonIndex++)
            {
                var upperBound = int.MaxValue;

                foreach (var maximumJoltage in buttonJoltages[buttonIndex])
                {
                    if (target[maximumJoltage] < upperBound)
                        upperBound = target[maximumJoltage];

                    _upperBounds[buttonIndex] = upperBound == int.MaxValue ? 0 : upperBound;
                }
            }
        }

        /// <summary>
        /// Solves the system of linear equations using HNF 
        /// and returns the minimum number of button presses required to reach the target.
        /// </summary>
        public int Solve()
        {
            var solution = GetSolution();

            var freeVariableCount = solution.FreeVariableCount;

            var rank = solution.HNFComponents.Rank;

            var uniModularMatrix = solution.HNFComponents
                                          .UniModularMatrix;

            var buttonPresses = solution.ButtonPresses;

            // If there are not free variables, the xpInt has the answer
            if (freeVariableCount == 0)
                return buttonPresses.Sum();

            // Contains the free changes that can be applied to the button presses, to achieve a minimum solution
            var freeChanges = new int[freeVariableCount][];

            for (var freeVariableIndex = 0; freeVariableIndex < freeVariableCount; freeVariableIndex++)
            {
                freeChanges[freeVariableIndex] = new int[_numberOfColumns];

                for (var columnIndex = 0; columnIndex < _numberOfColumns; columnIndex++)
                    freeChanges[freeVariableIndex][columnIndex] = uniModularMatrix[columnIndex, rank + freeVariableIndex];
            }

            var bounds = GetBounds(freeVariableCount, buttonPresses, freeChanges);

            var minimumSolution = int.MaxValue;

            EnumerateFreeVariables(0, freeVariableCount, bounds, freeChanges, buttonPresses, ref minimumSolution);

            return minimumSolution;
        }

        /// <summary>
        /// Returns the HNF components for the system of linear equations
        /// </summary>
        /// <returns></returns>
        private HnfComponents CreateHNF()
        {
            var hermiteMatrix = new int[_numberOfRows, _numberOfColumns];

            var uniModularMatrix = new int[_numberOfColumns, _numberOfColumns];

            for (var columnIndex = 0; columnIndex < _numberOfColumns; columnIndex++)
            {
                uniModularMatrix[columnIndex, columnIndex] = 1;

                foreach (var joltage in _buttonJoltages[columnIndex])
                    hermiteMatrix[joltage, columnIndex] = 1;
            }

            var pivotRowPerColumn = new Dictionary<int, int>();

            var rank = 0;

            for (var rowIndex = 0; rowIndex < _numberOfRows && rank < _numberOfColumns; rowIndex++)
            {
                // Reduce columns [rank..B-1] so that only one has a non-zero
                // entry in 'row' (a pivot), via repeated Bezout-style column ops.
                while (true)
                {
                    // Find the column in [rank..B-1] with non-zero entry in 'row'
                    // having the smallest absolute value (acts as pivot candidate).
                    int? pivotColumnIndex = null;

                    var absolutePivot = int.MaxValue;

                    for (var columnIndex = rank; columnIndex < _numberOfColumns; columnIndex++)
                    {
                        var columnValue = hermiteMatrix[rowIndex, columnIndex];

                        if (columnValue == 0)
                            continue;

                        var absoluteColumnValue = Math.Abs(columnValue);

                        if (absoluteColumnValue < absolutePivot)
                        {
                            absolutePivot = absoluteColumnValue;

                            pivotColumnIndex = columnIndex;
                        }
                    }

                    // If the row is all zero in remaining columns...
                    if (!pivotColumnIndex.HasValue)
                        break;

                    var pivotColumn = pivotColumnIndex.Value;

                    // Swap pivot columns to position 'rank'.
                    if (pivotColumn != rank)
                    {
                        for (var row = 0; row < _numberOfRows; row++)
                            (hermiteMatrix[row, rank], hermiteMatrix[row, pivotColumn]) = (hermiteMatrix[row, pivotColumn], hermiteMatrix[row, rank]);

                        for (var column = 0; column < _numberOfColumns; column++)
                            (uniModularMatrix[column, rank], uniModularMatrix[column, pivotColumn]) = (uniModularMatrix[column, pivotColumn], uniModularMatrix[column, rank]);
                    }

                    var anyChangeOccurred = false;

                    var pivotValue = hermiteMatrix[rowIndex, rank];

                    for (var columnIndex = rank + 1; columnIndex < _numberOfColumns; columnIndex++)
                    {
                        var value = hermiteMatrix[rowIndex, columnIndex];

                        if (value == 0)
                            continue;

                        var factor = value / pivotValue;

                        anyChangeOccurred = true;

                        for (var row = 0; row < _numberOfRows; row++)
                            hermiteMatrix[row, columnIndex] -= factor * hermiteMatrix[row, rank];

                        for (var row = 0; row < _numberOfColumns; row++)
                            uniModularMatrix[row, columnIndex] -= factor * uniModularMatrix[row, rank];
                    }

                    if (!anyChangeOccurred)
                        break;
                }

                var pivot = hermiteMatrix[rowIndex, rank];

                // After reduction, if H[row, rank] != 0 it's a real pivot.
                if (rank < _numberOfColumns && pivot != 0)
                {
                    // Normalize sign of pivot to be positive.
                    if (pivot < 0)
                    {
                        for (var row = 0; row < _numberOfRows; row++)
                            hermiteMatrix[row, rank] = -hermiteMatrix[row, rank];

                        for (var row = 0; row < _numberOfColumns; row++)
                            uniModularMatrix[row, rank] = -uniModularMatrix[row, rank];
                    }

                    pivotRowPerColumn.Add(rank, rowIndex);

                    rank++;
                }
            }

            var result = new HnfComponents()
            {
                HermiteMatrix = hermiteMatrix,
                UniModularMatrix = uniModularMatrix,
                PivotRowPerColumn = pivotRowPerColumn
            };

            return result;
        }

        /// <summary>
        /// Solves the system of linear equations using HNF and returns the possible solution
        /// </summary>
        /// <returns></returns>
        private Solution GetSolution()
        {
            var hnfResult = CreateHNF();

            var hermiteMatrix = hnfResult.HermiteMatrix;

            var rank = hnfResult.Rank;

            var pivotRowOfColumn = hnfResult.PivotRowPerColumn;

            var buttonCombinations = new int[_numberOfColumns];

            for (var pivotIndex = 0; pivotIndex < rank; pivotIndex++)
            {
                var pivotRow = pivotRowOfColumn[pivotIndex];

                var targetValue = _target[pivotRow];

                for (var rankIndex = 0; rankIndex < pivotIndex; rankIndex++)
                    targetValue -= hermiteMatrix[pivotRow, rankIndex] * buttonCombinations[rankIndex];

                var pivotValue = hermiteMatrix[pivotRow, pivotIndex];

                buttonCombinations[pivotIndex] = targetValue / pivotValue;
            }

            var uniModularMatrix = hnfResult.UniModularMatrix;

            var buttonPresses = new int[_numberOfColumns];

            for (var columnIndex = 0; columnIndex < _numberOfColumns; columnIndex++)
            {
                var sum = 0;

                for (var rankIndex = 0; rankIndex < rank; rankIndex++)
                    sum += uniModularMatrix[columnIndex, rankIndex] * buttonCombinations[rankIndex];

                buttonPresses[columnIndex] = sum;
            }

            var result = new Solution()
            {
                HNFComponents = hnfResult,
                FreeVariableCount = _numberOfColumns - rank,
                ButtonPresses = buttonPresses
            };

            return result;
        }

        /// <summary>
        /// Returns the bounds for the free variable search.
        /// Notes: Each free variable v is modeled for the LP as v = vPositive - vNegative, with both
        /// halves constrained to be non-negative.
        /// </summary>
        /// <param name="freeVariableCount">The number of free variables</param>
        /// <param name="buttonPresses">The current button presses</param>
        /// <param name="freeChanges">The changes for the free variables</param>
        /// <returns></returns>
        private FreeVariableBounds GetBounds(int freeVariableCount, int[] buttonPresses, int[][] freeChanges)
        {
            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(freeVariableCount);

            ArgumentNullException.ThrowIfNull(buttonPresses);

            ArgumentNullException.ThrowIfNull(freeChanges);

            var lpConstraints = BuildLpConstraints(freeVariableCount, buttonPresses, freeChanges);

            var minimumBounds = new int[freeVariableCount];

            var maximumBounds = new int[freeVariableCount];

            for (var freeVariableIndex = 0; freeVariableIndex < freeVariableCount; freeVariableIndex++)
            {
                var bound = BoundFreeVariable(freeVariableIndex, freeVariableCount, lpConstraints);

                minimumBounds[freeVariableIndex] = bound.Minimum;

                maximumBounds[freeVariableIndex] = bound.Maximum;
            }

            var result = new FreeVariableBounds()
            {
                MinimumBounds = minimumBounds,
                MaximumBounds = maximumBounds
            };

            return result;
        }

        /// <summary>
        /// Builds the LP constraint matrix and right-hand side for the free variable search.
        /// Notes: Each column's equality becomes an upper row and a lower row, and each free variable is
        /// split into a non-negative positive and negative part (v = vPositive - vNegative).
        /// </summary>
        /// <param name="freeVariableCount">The number of free variables</param>
        /// <param name="buttonPresses">The current button presses</param>
        /// <param name="freeChanges">The changes for the free variables</param>
        /// <returns></returns>
        private LPConstraints BuildLpConstraints(int freeVariableCount, int[] buttonPresses, int[][] freeChanges)
        {
            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(freeVariableCount);

            ArgumentNullException.ThrowIfNull(buttonPresses);

            ArgumentNullException.ThrowIfNull(freeChanges);

            var constraintRowCount = 2 * _numberOfColumns;

            var lpVariableCount = 2 * freeVariableCount;

            var constraints = new int[constraintRowCount, lpVariableCount];

            var rightHandSide = new int[constraintRowCount];

            for (var columnIndex = 0; columnIndex < _numberOfColumns; columnIndex++)
            {
                var upperRow = columnIndex;

                var lowerRow = _numberOfColumns + columnIndex;

                for (var freeVariableIndex = 0; freeVariableIndex < freeVariableCount; freeVariableIndex++)
                {
                    var positivePart = freeVariableIndex;

                    var negativePart = freeVariableCount + freeVariableIndex;

                    var change = freeChanges[freeVariableIndex][columnIndex];

                    constraints[upperRow, positivePart] = change;

                    constraints[upperRow, negativePart] = -change;

                    constraints[lowerRow, positivePart] = -change;

                    constraints[lowerRow, negativePart] = change;
                }

                rightHandSide[upperRow] = _upperBounds[columnIndex] - buttonPresses[columnIndex];

                rightHandSide[lowerRow] = buttonPresses[columnIndex];
            }

            var result = new LPConstraints()
            {
                Constraints = constraints,
                RightHandSide = rightHandSide
            };

            return result;
        }

        /// <summary>
        /// Computes the integer search bounds for a single free variable by minimizing and
        /// maximizing its value over the LP, then rounding outward so the integer enumeration
        /// cannot skip a feasible boundary.
        /// </summary>
        /// <param name="freeVariableIndex">The free variable to bound</param>
        /// <param name="freeVariableCount">The number of free variables</param>
        /// <param name="lPConstraints">The LP constraints</param>
        /// <returns></returns>
        private Bound BoundFreeVariable(int freeVariableIndex, int freeVariableCount, LPConstraints lPConstraints)
        {
            ArgumentOutOfRangeException.ThrowIfNegative(freeVariableIndex);

            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(freeVariableCount);

            ArgumentNullException.ThrowIfNull(lPConstraints);

            var constraintRowCount = 2 * _numberOfColumns;

            var lpVariableCount = 2 * freeVariableCount;

            var positivePart = freeVariableIndex;

            var negativePart = freeVariableCount + freeVariableIndex;

            // Objective selects the value of this single free variable: vPositive - vNegative.
            var objective = new int[lpVariableCount];

            objective[positivePart] = 1;

            objective[negativePart] = -1;

            // Simplex minimizes, so this directly gives the lowest feasible value.
            var minimum = GetOptimalBound(lPConstraints, objective, constraintRowCount, lpVariableCount);

            // Minimizing the negated objective yields the negation of the highest feasible value.
            for (var i = 0; i < lpVariableCount; i++)
                objective[i] = -objective[i];

            var negatedMaximum = GetOptimalBound(lPConstraints, objective, constraintRowCount, lpVariableCount);

            // Round outward (and pad by one) so the integer enumeration cannot skip a boundary.
            var minimumBound = (int)Math.Floor(minimum - RoundingTolerance) - BoundPadding;

            var maximumBound = (int)Math.Ceiling(-negatedMaximum + RoundingTolerance) + BoundPadding;

            return new Bound(minimumBound, maximumBound);
        }

        /// <summary>
        /// Returns the optimal objective value.
        /// Notes: Two-phase revised-tableau simplex on a problem
        /// minimize objective^T x where rightHandSide may have 
        /// negative entries (handled via Big-M artificials). Precision is sufficient for the small sizes here.
        /// </summary>
        /// <param name="lPConstraints">The LP constraints</param>
        /// <param name="objective">The objective coefficients to minimize</param>
        /// <param name="rowCount">The number of constraint rows</param>
        /// <param name="variableCount">The number of structural variables</param>
        /// <returns></returns>
        private static double GetOptimalBound(LPConstraints lPConstraints, int[] objective, int rowCount, int variableCount)
        {
            var simplexTableau = BuildSimplexTableau(lPConstraints, objective, rowCount, variableCount);

            var tableau = simplexTableau.Tableau;

            var basis = simplexTableau.Basis;

            var totalVariables = simplexTableau.TotalVariables;

            RunSimplexPivots(tableau, basis, rowCount, totalVariables);

            // The objective row's RHS holds the negated optimum, so negate it back.
            var optimalValue = -tableau[rowCount, totalVariables];

            return optimalValue;
        }

        /// <summary>
        /// Builds the simplex tableau in standard form: adds a slack per row, an artificial per
        /// negative right-hand side row (with the row sign-flipped so the RHS is non-negative),
        /// fills the objective row, and applies the Big-M penalty so artificials are driven out.
        /// </summary>
        /// <param name="lPConstraints">The LP constraints</param>
        /// <param name="objective">The objective coefficients to minimize</param>
        /// <param name="rowCount">The number of constraint rows</param>
        /// <param name="variableCount">The number of structural variables</param>
        /// <returns></returns>
        private static SimplexTableau BuildSimplexTableau(LPConstraints lPConstraints, int[] objective, int rowCount, int variableCount)
        {
            // Column layout of the tableau:
            //   [ 0 .. variableCount-1 ]            structural variables
            //   [ slackStart .. slackStart+m-1 ]    one slack per constraint row
            //   [ artificialStart .. ]              one artificial per negative-RHS row
            //   [ totalVariables ]                  right-hand side (last column)
            // A negative right-hand side is made non-negative by multiplying that row by -1,
            // which then needs an artificial variable to provide an initial basic feasible point.

            // structural + slacks
            var totalVariables = variableCount + rowCount;

            var rightHandSide = lPConstraints.RightHandSide;

            var artificialCount = rightHandSide.Count(x => x < 0);

            totalVariables += artificialCount;

            // Tableau: rows = rowCount + 1 (last row = objective), cols = totalVariables + 1 (RHS).
            var tableau = new double[rowCount + 1, totalVariables + 1];

            var basis = new int[rowCount];

            var slackStart = variableCount;

            var artificialStart = variableCount + rowCount;

            var artificialIndex = 0;

            var constraints = lPConstraints.Constraints;

            for (var rowIndex = 0; rowIndex < rowCount; rowIndex++)
            {
                var sign = rightHandSide[rowIndex] < 0 ? -1.0 : 1.0;

                for (var variableIndex = 0; variableIndex < variableCount; variableIndex++)
                    tableau[rowIndex, variableIndex] = sign * constraints[rowIndex, variableIndex];

                // Slack column for this row.
                tableau[rowIndex, slackStart + rowIndex] = sign;

                tableau[rowIndex, totalVariables] = sign * rightHandSide[rowIndex];

                if (rightHandSide[rowIndex] < 0)
                {
                    // Add artificial with coefficient +1, which becomes the initial basic variable.
                    tableau[rowIndex, artificialStart + artificialIndex] = 1.0;

                    basis[rowIndex] = artificialStart + artificialIndex;

                    artificialIndex++;
                }
                else
                    basis[rowIndex] = slackStart + rowIndex;
            }

            // Objective row: objective for structural vars, 0 for slacks, BigM for artificials.
            for (var variableIndex = 0; variableIndex < variableCount; variableIndex++)
                tableau[rowCount, variableIndex] = objective[variableIndex];

            for (var artificialVariableIndex = 0; artificialVariableIndex < artificialCount; artificialVariableIndex++)
                tableau[rowCount, artificialStart + artificialVariableIndex] = BigM;

            // Zero-out objective contributions of basic variables that have nonzero cost
            // (artificials that are basic): subtract BigM * row.
            for (var rowIndex = 0; rowIndex < rowCount; rowIndex++)
            {
                if (basis[rowIndex] < artificialStart)
                    continue;

                for (var totalVaraibleIndex = 0; totalVaraibleIndex <= totalVariables; totalVaraibleIndex++)
                    tableau[rowCount, totalVaraibleIndex] -= BigM * tableau[rowIndex, totalVaraibleIndex];
            }

            var result = new SimplexTableau
            {
                Tableau = tableau,
                Basis = basis,
                TotalVariables = totalVariables
            };

            return result;
        }

        /// <summary>
        /// Runs the simplex pivot loop in place until the objective row has no improving column.
        /// Uses Bland's rule (lowest-index entering variable) for cycle-free termination.
        /// </summary>
        /// <param name="tableau">The simplex tableau, mutated in place</param>
        /// <param name="basis">The basic variable per row, mutated in place</param>
        /// <param name="rowCount">The number of constraint rows</param>
        /// <param name="totalVariables">The total number of variables (RHS is the last column)</param>
        private static void RunSimplexPivots(double[,] tableau, int[] basis, int rowCount, int totalVariables)
        {
            ArgumentNullException.ThrowIfNull(tableau);

            ArgumentNullException.ThrowIfNull(basis);

            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(rowCount);

            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(totalVariables);

            for (var iterationIndex = 0; iterationIndex < MaxIterations; iterationIndex++)
            {
                var pivotColumn = -1;

                for (var totalVariableIndex = 0; totalVariableIndex < totalVariables; totalVariableIndex++)
                {
                    if (tableau[rowCount, totalVariableIndex] >= -OptimalityTolerance)
                        continue;

                    pivotColumn = totalVariableIndex;

                    break;
                }

                if (pivotColumn == -1)
                    break;

                // Min-ratio test.
                var pivotRow = -1;

                var bestRatio = double.PositiveInfinity;

                for (var rowIndex = 0; rowIndex < rowCount; rowIndex++)
                {
                    var columnValue = tableau[rowIndex, pivotColumn];

                    if (columnValue <= PivotTolerance)
                        continue;

                    var ratio = tableau[rowIndex, totalVariables] / columnValue;

                    if (ratio < bestRatio - RatioTieTolerance ||
                        (ratio < bestRatio + RatioTieTolerance &&
                         (pivotRow == -1 || basis[rowIndex] < basis[pivotRow])))
                    {
                        bestRatio = ratio;

                        pivotRow = rowIndex;
                    }
                }

                var pivotValue = tableau[pivotRow, pivotColumn];

                for (var totalVariableIndex = 0; totalVariableIndex <= totalVariables; totalVariableIndex++)
                    tableau[pivotRow, totalVariableIndex] /= pivotValue;

                for (var rowIndex = 0; rowIndex <= rowCount; rowIndex++)
                {
                    if (rowIndex == pivotRow)
                        continue;

                    var factor = tableau[rowIndex, pivotColumn];

                    if (factor == 0)
                        continue;

                    for (var totalVariableIndex = 0; totalVariableIndex <= totalVariables; totalVariableIndex++)
                        tableau[rowIndex, totalVariableIndex] -= factor * tableau[pivotRow, totalVariableIndex];
                }

                basis[pivotRow] = pivotColumn;
            }
        }

        /// <summary>
        /// Enumerates the free variables and their coefficients in the button effects,
        /// searching for the minimum solution
        /// </summary>
        /// <param name="freeVariableIndex">The starting free variable index</param>
        /// <param name="freeVariableCount">The number of free variables</param>
        /// <param name="freeVariableBounds">The bounds for each free variable</param>
        /// <param name="freeChanges">The changes in button presses for each free variable</param>
        /// <param name="buttonPresses">The current button presses</param>
        /// <param name="minimumSolution">The minimum solution found so far</param>
        private void EnumerateFreeVariables(int freeVariableIndex, int freeVariableCount, FreeVariableBounds freeVariableBounds,
            int[][] freeChanges, int[] buttonPresses, ref int minimumSolution)
        {
            ArgumentOutOfRangeException.ThrowIfNegative(freeVariableIndex);

            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(freeVariableCount);

            ArgumentNullException.ThrowIfNull(freeVariableBounds);

            ArgumentNullException.ThrowIfNull(freeChanges);

            ArgumentNullException.ThrowIfNull(buttonPresses);

            var freeVariableChanges = freeChanges[freeVariableIndex];

            var minimumBound = freeVariableBounds.MinimumBounds[freeVariableIndex];

            var maximumBound = freeVariableBounds.MaximumBounds[freeVariableIndex];

            for (var numberOfPresses = minimumBound; numberOfPresses <= maximumBound; numberOfPresses++)
            {
                for (var columnIndex = 0; columnIndex < _numberOfColumns; columnIndex++)
                    buttonPresses[columnIndex] += freeVariableChanges[columnIndex] * numberOfPresses;

                if (freeVariableIndex + 1 < freeVariableCount)
                {
                    EnumerateFreeVariables(freeVariableIndex + 1, freeVariableCount, freeVariableBounds, freeChanges, buttonPresses, ref minimumSolution);
                }
                else
                {
                    var solution = 0;

                    var isFeasibleSolution = true;

                    for (var columnIndex = 0; columnIndex < _numberOfColumns; columnIndex++)
                    {
                        var button = buttonPresses[columnIndex];

                        if (button < 0)
                        {
                            isFeasibleSolution = false;

                            break;
                        }

                        if (button > _upperBounds[columnIndex])
                        {
                            isFeasibleSolution = false;

                            break;
                        }

                        solution += button;
                    }

                    if (isFeasibleSolution)
                        minimumSolution = Math.Min(minimumSolution, solution);
                }

                // Revert the button presses for the next iteration
                for (var columnIndex = 0; columnIndex < _numberOfColumns; columnIndex++)
                    buttonPresses[columnIndex] -= freeVariableChanges[columnIndex] * numberOfPresses;
            }
        }

        /// <summary>
        /// Represents the result of the HNF operation
        /// </summary>
        private sealed record HnfComponents
        {
            /// <summary>
            /// The Hermite Normal Form matrix
            /// </summary>
            public required int[,] HermiteMatrix
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }

            /// <summary>
            /// The uni-modular transformation matrix such that H = U * A, 
            /// where H is the Hermite Normal Form of A
            /// </summary>
            public required int[,] UniModularMatrix
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }

            /// <summary>
            /// The pivot row per column
            /// </summary>
            public required IDictionary<int, int> PivotRowPerColumn
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }

            /// <summary>
            /// The rank of the <see cref="HermiteMatrix"/>
            /// </summary>
            public int Rank => PivotRowPerColumn.Count;
        }

        /// <summary>
        /// Represents the solution for the HNF operation
        /// </summary>
        private sealed record Solution
        {
            /// <summary>
            /// The HNF components
            /// </summary>
            public required HnfComponents HNFComponents
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }

            /// <summary>
            /// The amount of presses per button
            /// </summary>
            public required int[] ButtonPresses
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }

            /// <summary>
            /// The number of free variables in the solution
            /// </summary>
            public int FreeVariableCount
            {
                get;
                set
                {
                    ArgumentOutOfRangeException.ThrowIfNegative(value);

                    field = value;
                }
            }
        }

        /// <summary>
        /// Represents the boundaries for each button, that corresponds to a free variable
        /// </summary>
        private sealed record FreeVariableBounds
        {
            /// <summary>
            /// The minimum number of button presses required to reach the target
            /// </summary>
            public required int[] MinimumBounds
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }

            /// <summary>
            /// The maximum number of button presses required to reach the target
            /// </summary>
            public required int[] MaximumBounds
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }
        }

        /// <summary>
        /// Represents a free variable bound
        /// </summary>
        private readonly struct Bound
        {
            /// <summary>
            /// The minimum value for the free variable
            /// </summary>
            public int Minimum { get; }

            /// <summary>
            /// The maximum value for the free variable
            /// </summary>
            public int Maximum { get; }

            /// <summary>
            /// Creates a new instance of the <see cref="Bound"/>
            /// </summary>
            /// <param name="minimum">The minimum bound</param>
            /// <param name="maximum">The maximum bound</param>
            public Bound(int minimum, int maximum)
            {
                Minimum = minimum;

                Maximum = maximum;
            }

            /// <inheritdoc/>
            public override string ToString()
                => $"[{Minimum}, {Maximum}]";
        }

        /// <summary>
        /// Contains the constraints for the Linear Programming (LP) that bounds the free variables.
        /// </summary>
        private sealed record LPConstraints
        {
            /// <summary>
            /// The LP constraints matrix
            /// </summary>
            public required int[,] Constraints
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }

            /// <summary>
            /// The right-hand side of the constraints
            /// </summary>
            public required int[] RightHandSide
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }
        }

        /// <summary>
        /// Represents the simplex tableau and associated basis information 
        /// for the two-phase simplex method.
        /// </summary>
        private sealed record SimplexTableau
        {
            /// <summary>
            /// The tableau matrix, where the first rowCount rows are the constraints 
            /// and the last row is the negated objective.
            /// </summary>
            public required double[,] Tableau
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }

            /// <summary>
            /// The starting basis, represented as an array of variable indices (one per constraint row).
            /// </summary>
            public required int[] Basis
            {
                get;
                set
                {
                    ArgumentNullException.ThrowIfNull(value);

                    field = value;
                }
            }

            /// <summary>
            /// The total number of variables in the tableau, including structural, slack, and artificial variables.
            /// </summary>
            public required int TotalVariables
            {
                get;
                set
                {
                    ArgumentOutOfRangeException.ThrowIfNegativeOrZero(value);

                    field = value;
                }
            }
        }
    }
}