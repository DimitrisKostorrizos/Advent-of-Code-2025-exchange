namespace Y25Day09
{
    /// <summary>
    /// Represents an edge
    /// </summary>
    public readonly struct Edge
    {
        /// <summary>
        /// The first point
        /// </summary>
        public Point FirstPoint { get; }

        /// <summary>
        /// The second point
        /// </summary>
        public Point SecondPoint { get; }

        /// <summary>
        /// The minimum Y across the <see cref="FirstPoint"/> and <see cref="SecondPoint"/>
        /// </summary>
        public int MinimumY { get; }

        /// <summary>
        /// The maximum Y across the <see cref="FirstPoint"/> and <see cref="SecondPoint"/>
        /// </summary>
        public int MaximumY { get; }

        /// <summary>
        /// The minimum X across the <see cref="FirstPoint"/> and <see cref="SecondPoint"/>
        /// </summary>
        public int MinimumX { get; }

        /// <summary>
        /// The maximum X across the <see cref="FirstPoint"/> and <see cref="SecondPoint"/>
        /// </summary>
        public int MaximumX { get; }

        /// <summary>
        /// A flag indicating whether the edge is vertical
        /// </summary>
        public bool IsVertical => FirstPoint.X == SecondPoint.X;

        /// <summary>
        /// Creates a new instance of <see cref="Edge"/>
        /// </summary>
        /// <param name="firstPoint">The first point</param>
        /// <param name="secondPoint">The second point</param>
        public Edge(Point firstPoint, Point secondPoint)
        {
            FirstPoint = firstPoint;

            SecondPoint = secondPoint;

            // If the edge is diagonal...
            if (FirstPoint.Y != SecondPoint.Y && FirstPoint.X != SecondPoint.X)
                throw new InvalidOperationException("No diagonal edges are allowed.");

            MinimumY = Math.Min(FirstPoint.Y, SecondPoint.Y);

            MinimumX = Math.Min(FirstPoint.X, SecondPoint.X);

            MaximumY = Math.Max(FirstPoint.Y, SecondPoint.Y);

            MaximumX = Math.Max(FirstPoint.X, SecondPoint.X);
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => $"{FirstPoint}, {SecondPoint}";

        /// <summary>
        /// Returns the points contained in the edge
        /// </summary>
        /// <returns></returns>
        public IEnumerable<Point> GetPoints()
        {
            // If the edge is vertical...
            if (IsVertical)
            {
                for (var y = MinimumY; y <= MaximumY; y++)
                    yield return new Point(MaximumX, y);
            }
            else
            {
                for (var x = MinimumX; x <= MaximumX; x++)
                    yield return new Point(x, MaximumY);
            }
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="obj">The value</param>
        /// <returns></returns>
        public override bool Equals(object? obj)
            => obj is Edge key && Equals(key);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="other">The other object</param>
        /// <returns></returns>
        public bool Equals(Edge other)
            => FirstPoint == other.FirstPoint &&
            SecondPoint == other.SecondPoint;

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override int GetHashCode()
            => HashCode.Combine(FirstPoint, SecondPoint);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="left">The left operand</param>
        /// <param name="right">The right operand</param>
        /// <returns></returns>
        public static bool operator ==(Edge left, Edge right)
            => left.Equals(right);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="left">The left operand</param>
        /// <param name="right">The right operand</param>
        /// <returns></returns>
        public static bool operator !=(Edge left, Edge right)
            => !(left == right);
    }
}