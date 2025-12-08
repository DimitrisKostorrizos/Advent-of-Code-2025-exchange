namespace Y25Day08
{
    /// <summary>
    /// Represents the key for the junction box
    /// </summary>
    public readonly struct JunctionBoxKey
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
        /// Creates a new instance of <see cref="JunctionBoxKey"/>
        /// </summary>
        /// <param name="firstPoint">The first point</param>
        /// <param name="secondPoint">The second point</param>
        public JunctionBoxKey(Point firstPoint, Point secondPoint)
        {
            FirstPoint = firstPoint;

            SecondPoint = secondPoint;
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => $"{FirstPoint}, {SecondPoint}";

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="obj">The value</param>
        /// <returns></returns>
        public override bool Equals(object? obj)
            => obj is JunctionBoxKey key && Equals(key);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="other">The other object</param>
        /// <returns></returns>
        public bool Equals(JunctionBoxKey other)
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
        public static bool operator ==(JunctionBoxKey left, JunctionBoxKey right)
            => left.Equals(right);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="left">The left operand</param>
        /// <param name="right">The right operand</param>
        /// <returns></returns>
        public static bool operator !=(JunctionBoxKey left, JunctionBoxKey right)
            => !(left == right);
    }
}