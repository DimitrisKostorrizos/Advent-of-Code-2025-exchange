using System.Diagnostics.CodeAnalysis;

namespace Y25Day09
{
    /// <summary>
    /// Represents a rectangle area
    /// </summary>
    public readonly struct RectangleArea
    {
        /// <summary>
        /// The first corner
        /// </summary>
        public Point FirstCorner { get; }

        /// <summary>
        /// The second corner
        /// </summary>
        public Point? SecondCorner { get; }

        /// <summary>
        /// The third corner
        /// </summary>
        public Point ThirdCorner { get; }

        /// <summary>
        /// The fourth corner
        /// </summary>
        public Point? FourthCorner { get; }

        /// <summary>
        /// A flag indicating whether the area is a line
        /// </summary>
        [MemberNotNullWhen(false, nameof(SecondCorner), nameof(FourthCorner))]
        public bool IsLine => !SecondCorner.HasValue && !FourthCorner.HasValue;

        /// <summary>
        /// The area
        /// </summary>
        public ulong Area { get; }

        /// <summary>
        /// Creates a new instance of <see cref="RectangleArea"/>
        /// </summary>
        /// <param name="firstCorner">The first corner</param>
        /// <param name="thirdCorner">The third corner</param>
        public RectangleArea(Point firstCorner, Point thirdCorner, ulong area)
        {
            FirstCorner = firstCorner;

            ThirdCorner = thirdCorner;

            // If the points are not on the same line...
            if (FirstCorner.X != ThirdCorner.X && FirstCorner.Y != ThirdCorner.Y)
            {
                SecondCorner = new Point(FirstCorner.X, ThirdCorner.Y);

                FourthCorner = new Point(ThirdCorner.X, FirstCorner.Y);
            }

            Area = area;
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString()
        {
            if(IsLine)
                return $"{FirstCorner}, {ThirdCorner}";

            return $"{FirstCorner}, {SecondCorner}, {ThirdCorner}, {FourthCorner}";
        }

        /// <summary>
        /// Returns the edges of the rectangle
        /// </summary>
        /// <returns></returns>
        public IEnumerable<Edge> GetEdges()
        {
            if (IsLine)
                yield return new Edge(FirstCorner, ThirdCorner);
            else
            {
                yield return new Edge(FirstCorner, SecondCorner.Value);

                yield return new Edge(SecondCorner.Value, ThirdCorner);

                yield return new Edge(ThirdCorner, FourthCorner.Value);

                yield return new Edge(FourthCorner.Value, FirstCorner);
            }
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="obj">The value</param>
        /// <returns></returns>
        public override bool Equals(object? obj)
            => obj is RectangleArea key && Equals(key);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="other">The other object</param>
        /// <returns></returns>
        public bool Equals(RectangleArea other)
            => FirstCorner == other.FirstCorner &&
            ThirdCorner == other.ThirdCorner;

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override int GetHashCode()
            => HashCode.Combine(FirstCorner, ThirdCorner);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="left">The left operand</param>
        /// <param name="right">The right operand</param>
        /// <returns></returns>
        public static bool operator ==(RectangleArea left, RectangleArea right)
            => left.Equals(right);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="left">The left operand</param>
        /// <param name="right">The right operand</param>
        /// <returns></returns>
        public static bool operator !=(RectangleArea left, RectangleArea right)
            => !(left == right);
    }
}