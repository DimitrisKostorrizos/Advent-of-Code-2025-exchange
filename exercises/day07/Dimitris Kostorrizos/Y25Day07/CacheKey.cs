namespace Y25Day07
{
    /// <summary>
    /// Represents an key in the cache
    /// </summary>
    public readonly struct CacheKey : IEquatable<CacheKey>
    {
        /// <summary>
        /// The starting point
        /// </summary>
        public int StartingPoint { get; }

        /// <summary>
        /// The line index
        /// </summary>
        public int LineIndex { get; }

        /// <summary>
        /// Creates a new instance of <see cref="CacheKey"/>
        /// </summary>
        /// <param name="startingPoint">The starting point</param>
        /// <param name="lineIndex">The line index</param>
        public CacheKey(int startingPoint, int lineIndex)
        {
            ArgumentOutOfRangeException.ThrowIfNegative(startingPoint);

            ArgumentOutOfRangeException.ThrowIfNegative(lineIndex);

            StartingPoint = startingPoint;

            LineIndex = lineIndex;
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => $"Point: {StartingPoint}, Line: {LineIndex}";

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="obj">The value</param>
        /// <returns></returns>
        public override bool Equals(object? obj) 
            => obj is CacheKey key && Equals(key);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="other">The other object</param>
        /// <returns></returns>
        public bool Equals(CacheKey other) 
            => StartingPoint == other.StartingPoint &&
                   LineIndex == other.LineIndex;

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override int GetHashCode() 
            => HashCode.Combine(StartingPoint, LineIndex);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="left">The left operand</param>
        /// <param name="right">The right operand</param>
        /// <returns></returns>
        public static bool operator ==(CacheKey left, CacheKey right) 
            => left.Equals(right);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="left">The left operand</param>
        /// <param name="right">The right operand</param>
        /// <returns></returns>
        public static bool operator !=(CacheKey left, CacheKey right) 
            => !(left == right);
    }
}