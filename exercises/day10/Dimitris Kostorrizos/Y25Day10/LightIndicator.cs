namespace Y25Day10
{
    /// <summary>
    /// Represents a light indicator
    /// </summary>
    public sealed record LightIndicator
    {
        /// <summary>
        /// The position of the light indicator
        /// </summary>
        public int Position { get; }

        /// <summary>
        /// A flag indicating whether the light indicator is on
        /// </summary>
        public bool IsOn { get; }

        /// <summary>
        /// Creates a new instance of <see cref="LightIndicator"/>
        /// </summary>
        /// <param name="position">The position of the light indicator/param>
        /// <param name="isOn">The line index</param>
        public LightIndicator(int position, bool isOn)
        {
            ArgumentOutOfRangeException.ThrowIfNegative(position);

            Position = position;

            IsOn = isOn;
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString()
        {
            var state = IsOn ? "On" : "Off";

            return $"Position: {Position} {state}";
        }
    }
}