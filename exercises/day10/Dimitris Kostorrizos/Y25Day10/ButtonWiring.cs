using System.Globalization;

namespace Y25Day10
{
    /// <summary>
    /// Represents a button wiring
    /// </summary>
    public sealed class ButtonWiring
    {
        /// <summary>
        /// The affected positions that the button affects
        /// </summary>
        public IEnumerable<int> AffectedPositions { get; }

        /// <summary>
        /// Creates a new instance of <see cref="ButtonWiring"/>
        /// </summary>
        /// <param name="affectedPositions">The affected positions that the button affects</param>
        public ButtonWiring(IEnumerable<int> affectedPositions) : base()
        {
            ArgumentNullException.ThrowIfNull(affectedPositions);

            if (!affectedPositions.Any())
                throw new InvalidOperationException("At least one button must be specified.");

            AffectedPositions = affectedPositions;
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => string.Join(',', AffectedPositions);

        /// <summary>
        /// Creates and returns a <see cref="ButtonWiring"/> from the specified <paramref name="stringRepresentation"/>
        /// </summary>
        /// <param name="stringRepresentation">The string representation</param>
        /// <returns></returns>
        public static ButtonWiring Create(string stringRepresentation)
        {
            ArgumentException.ThrowIfNullOrWhiteSpace(stringRepresentation);

            var buttons = stringRepresentation[1..^1].Split(',', StringSplitOptions.TrimEntries)
                .Select(x => int.Parse(x, CultureInfo.InvariantCulture))
                .ToList();

            return new(buttons);
        }
    }
}