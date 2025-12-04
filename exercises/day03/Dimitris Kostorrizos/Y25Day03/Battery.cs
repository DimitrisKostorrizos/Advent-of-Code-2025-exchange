using System.Globalization;

namespace Y25Day03
{
    /// <summary>
    /// Represents a battery
    /// </summary>
    public sealed class Battery
    {
        /// <summary>
        /// The joltage
        /// </summary>
        public int Joltage { get; }

        /// <summary>
        /// The <see cref="Joltage"/> as a<see cref="char"/>
        /// </summary>
        public char JoltageRepresentation { get; }

        /// <summary>
        /// Creates a new instance of <see cref="Battery"/>
        /// </summary>
        /// <param name="joltage"></param>
        public Battery(char joltage)
        {
            if (!char.IsDigit(joltage))
                throw new ArgumentOutOfRangeException(nameof(joltage), $"The '{nameof(joltage)}' has to be a single digit.");

            JoltageRepresentation = joltage;

            Joltage = (int)char.GetNumericValue(joltage);
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => Joltage.ToString(CultureInfo.InvariantCulture);
    }
}