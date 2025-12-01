namespace Y25Day01
{
    /// <summary>
    /// Represents a circular dial that can shift between the <see cref="Minimum"/> and <see cref="Maximum"/> values
    /// </summary>
    public sealed partial class CircularDial
    {
        /// <summary>
        /// The field for the <see cref="CurrentValue"/>
        /// </summary>
        private int _currentValue;

        /// <summary>
        /// The maximum value
        /// </summary>
        public int Maximum { get; }

        /// <summary>
        /// The minimum value
        /// </summary>
        public int Minimum { get; }

        /// <summary>
        /// The value that dial points to
        /// </summary>
        public int CurrentValue => _currentValue;

        /// <summary>
        /// The maximum traversable distance in shifts
        /// </summary>
        public int MaximumDistance => Maximum - Minimum;

        /// <summary>
        /// The number of values contained in the dial
        /// </summary>
        public int NumberOfValues => MaximumDistance + 1;

        /// <summary>
        /// Creates a new instance of <see cref="CircularDial"/>
        /// </summary>
        /// <param name="maximum">The maximum value</param>
        /// <param name="minimum">The minimum value</param>
        /// <param name="startingValue">The starting value</param>
        public CircularDial(int maximum, int minimum, int startingValue)
        {
            ArgumentOutOfRangeException.ThrowIfNegative(minimum);

            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(maximum);

            ArgumentOutOfRangeException.ThrowIfGreaterThanOrEqual(minimum, maximum);

            if (startingValue > maximum || startingValue < minimum)
                throw new ArgumentOutOfRangeException(nameof(startingValue), $"The '{nameof(startingValue)}' must be between {minimum} and {maximum}.");

            Maximum = maximum;

            Minimum = minimum;

            _currentValue = startingValue;
        }

        /// <summary>
        /// Executes a shift operation on the dial based on the specified <paramref name="dialShift"/>
        /// </summary>
        /// <param name="dialShift">The dial shift instruction</param>
        /// <returns></returns>
        public ShiftResult Shift(DialShift dialShift)
        {
            ArgumentNullException.ThrowIfNull(dialShift);

            return Shift(dialShift.Type, dialShift.NumberOfShifts);
        }

        /// <summary>
        /// Executes a shift operation on the dial based on the specified <paramref name="shiftType"/> and <paramref name="numberOfShifts"/>
        /// </summary>
        /// <param name="shiftType">The type of shift</param>
        /// <param name="numberOfShifts">The number of shifts</param>
        /// <returns></returns>
        public ShiftResult Shift(ShiftType shiftType, int numberOfShifts)
        {
            var traversedValues = new List<int>();

            var oldPosition = _currentValue;

            if (numberOfShifts == 0)
                return new ShiftResult(0, oldPosition, _currentValue, traversedValues);

            var (numberOfCompleteRotations, lastRotationShifts) = Math.DivRem(numberOfShifts, NumberOfValues);

            int newValue;

            int startingSequenceNumber;

            int endingSequenceNumber;

            if (shiftType == ShiftType.Left)
            {
                endingSequenceNumber = _currentValue - 1;

                newValue = _currentValue - lastRotationShifts;

                startingSequenceNumber = newValue;

                if (newValue < Minimum)
                {
                    traversedValues.AddRange(GenerateSequence(Minimum, endingSequenceNumber));

                    HandleNegativeOverflow(newValue);

                    startingSequenceNumber = _currentValue;

                    endingSequenceNumber = Maximum;
                }
                else
                    _currentValue = newValue;

                traversedValues.AddRange(GenerateSequence(startingSequenceNumber, endingSequenceNumber));
            }
            else
            {
                startingSequenceNumber = _currentValue + 1;

                newValue = _currentValue + lastRotationShifts;

                endingSequenceNumber = newValue;

                if (newValue > Maximum)
                {
                    traversedValues.AddRange(GenerateSequence(startingSequenceNumber, Maximum));

                    HandlePositiveOverflow(newValue);

                    startingSequenceNumber = Minimum;

                    endingSequenceNumber = _currentValue;
                }
                else
                    _currentValue = newValue;

                traversedValues.AddRange(GenerateSequence(startingSequenceNumber, endingSequenceNumber));
            }

            for (int rotation = 0; rotation < numberOfCompleteRotations; rotation++)
            {
                traversedValues.AddRange(GenerateSequence(Minimum, Maximum));
            }

            return new ShiftResult(numberOfCompleteRotations, oldPosition, _currentValue, traversedValues);
        }

        /// <summary>
        /// Generates a sequence from the <paramref name="start"/> to the <paramref name="end"/> inclusive.
        /// </summary>
        /// <param name="start">The starting value</param>
        /// <param name="end">The ending value</param>
        /// <returns></returns>
        private static IEnumerable<int> GenerateSequence(int start, int end)
        {
            for (var i = start; i <= end; i++)
            {
                yield return i;
            }
        }

        /// <summary>
        /// Handles the overflow when the <paramref name="newValue"/> exceeds the <see cref="Maximum"/>
        /// </summary>
        /// <param name="newValue">The new value</param>
        private void HandlePositiveOverflow(int newValue)
        {
            var overflowedShifts = newValue - Maximum;

            _currentValue = Minimum + overflowedShifts - 1;
        }

        /// <summary>
        /// Handles the overflow when the <paramref name="newValue"/> exceeds the <see cref="Minimum"/>
        /// </summary>
        /// <param name="newValue">The new value</param>
        private void HandleNegativeOverflow(int newValue)
        {
            var overflowedShifts = Minimum - newValue;

            _currentValue = Maximum - overflowedShifts + 1;
        }
    }
}