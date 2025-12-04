using System.Globalization;

namespace Y25Day03
{
    /// <summary>
    /// Represents a range of ids
    /// </summary>
    public sealed class BatteryBank
    {
        /// <summary>
        /// The string representation for the current object
        /// </summary>
        private readonly string _representation;

        /// <summary>
        /// The batteries in the current bank
        /// </summary>
        private readonly List<Battery> _batteries;

        /// <summary>
        /// Creates a new instance of <see cref="BatteryBank"/>
        /// </summary>
        /// <param name="batteries">The joltages</param>
        public BatteryBank(IEnumerable<char> batteries) : base()
        {
            ArgumentNullException.ThrowIfNull(batteries);

            var inputAsArray = batteries.ToArray();

            _representation = new string(inputAsArray);

            _batteries = [.. inputAsArray.Select(x => new Battery(x))];
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => _representation;

        /// <summary>
        /// Returns the maximum joltage for the first half of the exercise
        /// </summary>
        /// <returns></returns>
        public long GetMaximumJoltageForFirstHalf()
            => GetHighestJoltage(2);

        /// <summary>
        /// Returns the maximum joltage for the second half of the exercise
        /// </summary>
        /// <returns></returns>
        public long GetMaximumJoltageForSecondHalf()
            => GetHighestJoltage(12);

        /// <summary>
        /// Returns the highest possible joltage for  the specified <paramref name="numberOfBatteries"/>
        /// </summary>
        /// <param name="numberOfBatteries">The number of batteries</param>
        /// <returns></returns>
        private long GetHighestJoltage(int numberOfBatteries)
        {
            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(numberOfBatteries);

            var highestBatteries = GetHighestJoltageBatteries(numberOfBatteries);

            Span<char> joltage = stackalloc char[numberOfBatteries];

            int index = 0;

            foreach (var pair in highestBatteries.OrderBy(x => x.Key))
            {
                joltage[index] = pair.Value.JoltageRepresentation;

                index++;
            }

            return long.Parse(joltage, CultureInfo.InvariantCulture);
        }

        /// <summary>
        /// Returns the batteries equal to <paramref name="numberOfBatteries"/>, that would generate the highest joltage
        /// </summary>
        /// <param name="numberOfBatteries">The number of batteries</param>
        /// <returns></returns>
        private Dictionary<int, Battery> GetHighestJoltageBatteries(int numberOfBatteries)
        {
            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(numberOfBatteries);

            var remainingBatteries = _batteries.ToList();

            var batteriesToBeFound = numberOfBatteries - 1;

            var results = new Dictionary<int, Battery>();

            for (int i = 0; i < numberOfBatteries; i++)
            {
                var excludedBatteries = new List<Battery>();

                var isValidBattery = false;

                var maximumJoltageBattery = default(Battery);

                int maximumJoltageBatteryPositionInRemainingBatteries = 0;

                while (!isValidBattery)
                {
                    maximumJoltageBattery = remainingBatteries.Except(excludedBatteries)
                        .MaxBy(x => x.Joltage)
                        ?? throw new InvalidOperationException("Not enough batteries exist.");

                    maximumJoltageBatteryPositionInRemainingBatteries = remainingBatteries.IndexOf(maximumJoltageBattery);

                    isValidBattery = true;

                    // The batteries before the current maximum would be ignored
                    var remainingBatteryCount = remainingBatteries.Count - maximumJoltageBatteryPositionInRemainingBatteries - 1;

                    if (batteriesToBeFound > 0 && remainingBatteryCount < batteriesToBeFound)
                    {
                        excludedBatteries.Add(maximumJoltageBattery);

                        isValidBattery = false;
                    }
                }

                if (maximumJoltageBattery is null)
                    throw new InvalidOperationException("Not enough batteries exist.");

                results.Add(_batteries.IndexOf(maximumJoltageBattery), maximumJoltageBattery);

                remainingBatteries.RemoveRange(0, maximumJoltageBatteryPositionInRemainingBatteries + 1);

                batteriesToBeFound--;
            }

            return results;
        }
    }
}