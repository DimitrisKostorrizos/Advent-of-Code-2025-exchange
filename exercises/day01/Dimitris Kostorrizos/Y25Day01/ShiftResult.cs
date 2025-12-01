namespace Y25Day01
{
    /// <summary>
    /// The result of a shift operation
    /// </summary>
    public sealed class ShiftResult
    {
        /// <summary>
        /// The number of complete rotations<
        /// </summary>
        public int NumberOfCompleteRotations { get; }

        /// <summary>
        /// The old position before the application of the shift operation
        /// </summary>
        public int OldPosition { get; }

        /// <summary>
        /// The new position after the application of the shift operation
        /// </summary>
        public int NewPosition { get; }

        /// <summary>
        /// The values that were traversed during the operation
        /// </summary>
        public IEnumerable<int> TraversedValues { get; }

        /// <summary>
        /// Creates a new instance of <see cref="ShiftResult"/>
        /// </summary>
        /// <param name="numberOfCompleteRotations">The number of complete rotations</param>
        /// <param name="oldPosition">The old position before the application of the shift operation</param>
        /// <param name="newPosition">The new position after the application of the shift operation</param>
        /// <param name="traversedValues">The values that were traversed during the operation</param>
        public ShiftResult(int numberOfCompleteRotations, int oldPosition, int newPosition, IEnumerable<int> traversedValues)
        {
            ArgumentOutOfRangeException.ThrowIfNegative(numberOfCompleteRotations);

            ArgumentOutOfRangeException.ThrowIfNegative(oldPosition);

            ArgumentOutOfRangeException.ThrowIfNegative(newPosition);

            ArgumentNullException.ThrowIfNull(traversedValues);

            NumberOfCompleteRotations = numberOfCompleteRotations;

            OldPosition = oldPosition;

            NewPosition = newPosition;

            TraversedValues = traversedValues;
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => $"{OldPosition} => {NewPosition}";
    }
}