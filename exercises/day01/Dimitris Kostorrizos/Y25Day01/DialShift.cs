using System.Globalization;

namespace Y25Day01
{
    /// <summary>
    /// Represents a shift in a <see cref="CircularDial"/>
    /// </summary>
    public sealed class DialShift
    {
        /// <summary>
        /// The type of shifting
        /// </summary>
        public ShiftType Type { get; }

        /// <summary>
        /// The number of shifts
        /// </summary>
        public int NumberOfShifts { get; }

        /// <summary>
        /// Creates a new instance of <see cref="DialShift"/>
        /// </summary>
        /// <param name="type">The type of shifting</param>
        /// <param name="numberOfShifts">The number of shifts</param>
        public DialShift(ShiftType type, int numberOfShifts) : base()
        {
            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(numberOfShifts);

            Type = type;

            NumberOfShifts = numberOfShifts;
        }

        /// <summary>
        /// Creates and returns a <see cref="DialShift"/> from the specified <paramref name="value"/>
        /// </summary>
        /// <param name="value">The string representation</param>
        /// <returns></returns>
        /// <exception cref="ArgumentException">An <see cref="ArgumentException"/> is thrown if the <paramref name="value"/> does not match the expected format.</exception>
        public static DialShift Create(string value)
        {
            ArgumentException.ThrowIfNullOrWhiteSpace(value);

            if (value.Length < 2)
                throw new ArgumentException($"The value: '{value}' is invalid. The dial shift instruction must contain at least two characters.", nameof(value));

            var letter = value[0];

            ShiftType type;

            if (letter == 'L')
                type = ShiftType.Left;
            else if(letter == 'R')
                type = ShiftType.Right;
            else
                throw new ArgumentException($"The value: '{value}' is invalid. The dial shift instruction must start either with the letter 'L' or 'R'.", nameof(value));

            var number = int.Parse(value.AsSpan(1), CultureInfo.InvariantCulture);

            return new(type, number);
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => $"{Type} => {NumberOfShifts}";
    }
}