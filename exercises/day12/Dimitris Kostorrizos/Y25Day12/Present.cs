using System.Globalization;

namespace Y25Day12
{
    /// <summary>
    /// Represents a present
    /// </summary>
    public sealed class Present
    {
        /// <summary>
        /// The size of each dimension of the present
        /// </summary>
        public const int PresentDimension = 3;

        /// <summary>
        /// The size of the string representation of the present
        /// </summary>
        public const int PresentRepresentationSize = 5;

        /// <summary>
        /// The index of the present
        /// </summary>
        public int Index { get; }

        /// <summary>
        /// The grid that represents the shape of the present
        /// </summary>
        public bool[,] ShapeGrid { get; }

        /// <summary>
        /// The area that the present requires
        /// </summary>
        public int Area { get; }

        /// <summary>
        /// Creates a new instance of <see cref="Present"/>
        /// </summary>
        /// <param name="index">The index of the present</param>
        /// <param name="shapeGrid">The grid that represents the shape of the present</param>
        public Present(int index, bool[,] shapeGrid) : base()
        {
            ArgumentOutOfRangeException.ThrowIfNegative(index);

            ArgumentNullException.ThrowIfNull(shapeGrid);

            var length = shapeGrid.GetLength(0);

            if (length != PresentDimension)
                throw new InvalidOperationException($"The length of the present '{index}' has to be {PresentDimension}.");

            var width = shapeGrid.GetLength(1);

            if (width != PresentDimension)
                throw new InvalidOperationException($"The width of the present '{index}' has to be {PresentDimension}.");

            Index = index;

            ShapeGrid = shapeGrid;

            var area = 0;

            for (var row = 0; row < PresentDimension; row++ )
            {
                for (var column = 0; column < PresentDimension; column++)
                {
                    if (ShapeGrid[row, column])
                        area++;
                }
            }

            Area = area;
        }

        /// <summary>
        /// Creates and returns a <see cref="Present"/> from the specified <paramref name="stringRepresentation"/>
        /// </summary>
        /// <param name="stringRepresentation">The string representation</param>
        /// <returns></returns>
        public static Present Create(IEnumerable<string> stringRepresentation)
        {
            ArgumentNullException.ThrowIfNull(stringRepresentation);

            if(stringRepresentation.Count() != PresentRepresentationSize)
                throw new InvalidOperationException($"The size of the present representation has to be {PresentRepresentationSize}.");

            var index = -1;

            var shape = new bool[3, 3];

            var rowIndex = 0;

            foreach (var (line, lineIndex) in stringRepresentation.Select((line, lineIndex) => (line, lineIndex)))
            {
                if (string.IsNullOrWhiteSpace(line))
                    continue;

                if (lineIndex == 0)
                {
                    var indexRepresentation = line.TakeWhile(x => x != ':')
                        .ToArray();

                    index = int.Parse(indexRepresentation, CultureInfo.InvariantCulture);
                }
                else
                {
                    foreach (var (cell, columnIndex) in line.Select((cell, cellIndex) => (cell, cellIndex)))
                    {
                        var isInShape = cell == '#';

                        shape[rowIndex, columnIndex] = isInShape;
                    }

                    rowIndex++;
                }
            }

            return new(index, shape);
        }

        /// <inheritdoc/>
        public override string ToString()
            => Index.ToString();
    }
}