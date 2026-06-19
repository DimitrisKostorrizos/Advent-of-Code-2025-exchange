using System.Globalization;

namespace Y25Day10
{
    /// <summary>
    /// Represents a machine
    /// </summary>
    public sealed class Machine
    {
        /// <summary>
        /// The schematics for light indicators
        /// </summary>
        public IEnumerable<LightIndicator> LightIndicators { get; }

        /// <summary>
        /// The buttons wirings
        /// </summary>
        public IEnumerable<ButtonWiring> ButtonWirings { get; }

        /// <summary>
        /// The joltages
        /// </summary>
        public IEnumerable<int> Joltages { get; }

        /// <summary>
        /// Creates a new instance of <see cref="Machine"/>
        /// </summary>
        /// <param name="lightIndicatorsSchematics">The schematics for light indicators</param>
        /// <param name="buttonWirings">The buttons wirings</param>
        /// <param name="joltages">The joltages</param>
        public Machine(IEnumerable<LightIndicator> lightIndicatorsSchematics, IEnumerable<ButtonWiring> buttonWirings, IEnumerable<int> joltages) : base()
        {
            ArgumentNullException.ThrowIfNull(lightIndicatorsSchematics);

            if (!lightIndicatorsSchematics.Any())
                throw new InvalidOperationException("At least one light indicator must be specified.");

            ArgumentNullException.ThrowIfNull(buttonWirings);

            if (!buttonWirings.Any())
                throw new InvalidOperationException("At least one button wiring must be specified.");

            ArgumentNullException.ThrowIfNull(joltages);

            if (!joltages.Any())
                throw new InvalidOperationException("At least one joltage must be specified.");

            LightIndicators = lightIndicatorsSchematics;

            ButtonWirings = buttonWirings;

            Joltages = joltages;
        }

        /// <summary>
        /// Creates and returns a <see cref="Machine"/> from the specified <paramref name="stringRepresentation"/>
        /// </summary>
        /// <param name="stringRepresentation">The string representation</param>
        /// <returns></returns>
        public static Machine Create(string stringRepresentation)
        {
            ArgumentException.ThrowIfNullOrWhiteSpace(stringRepresentation);

            var lightIndicatorEndingIndex = stringRepresentation.IndexOf(']');

            var lightIndicatorPart = stringRepresentation[1..lightIndicatorEndingIndex];

            var lightIndicators = new List<LightIndicator>();

            for (var i = 0; i < lightIndicatorPart.Length; i++)
            {
                var lightIndicatorValue = false;

                if (lightIndicatorPart[i] == '#')
                    lightIndicatorValue = true;

                lightIndicators.Add(new LightIndicator(i, lightIndicatorValue));
            }

            var buttonWiringEndingIndex = stringRepresentation.LastIndexOf(')');

            var buttonWiringPart = stringRepresentation.Substring(lightIndicatorEndingIndex + 2, buttonWiringEndingIndex - lightIndicatorEndingIndex);

            var buttonWirings = new List<ButtonWiring>();

            foreach (var buttonWiring in buttonWiringPart.Split(' ', StringSplitOptions.RemoveEmptyEntries))
                buttonWirings.Add(ButtonWiring.Create(buttonWiring));

            var joltagesStartingIndex = buttonWiringEndingIndex + 3;

            var joltagesPart = stringRepresentation[joltagesStartingIndex..^1];

            var joltages = joltagesPart.Split(',', StringSplitOptions.TrimEntries)
                .Select(x => int.Parse(x, CultureInfo.InvariantCulture))
                .ToList();

            return new(lightIndicators, buttonWirings, joltages);
        }

        /// <summary>
        /// Returns the minimum number of presses to light the <see cref="LightIndicators"/>
        /// </summary>
        /// <returns></returns>
        public int GetMinimumButtonPressesForLights()
        {
            var numberOfButtons = ButtonWirings.Count();

            var numberOfLightIndicators = LightIndicators.Count();

            var buttonMatrix = new int[numberOfLightIndicators, numberOfButtons];

            var result = LightIndicators.Select(x => x.IsOn ? 1 : 0).ToArray();

            for (var rowIndex = 0; rowIndex < numberOfLightIndicators; rowIndex++)
            {
                var columnIndex = 0;

                foreach (var button in ButtonWirings)
                {
                    var value = 0;

                    if (button.AffectedPositions.Contains(rowIndex))
                        value = 1;

                    buttonMatrix[rowIndex, columnIndex] = value;

                    columnIndex++;
                }
            }

            var solver = new GaussianEliminationModTwoSolver(buttonMatrix, result);

            return solver.Solve();
        }

        /// <summary>
        /// Returns the minimum number of presses to match the <see cref="Joltages"/>
        /// </summary>
        /// <returns></returns>
        public int GetMinimumButtonPressesForJoltage()
        {
            var buttonMatrix = ButtonWirings.Select(x => x.AffectedPositions.ToArray()).ToArray();

            var solver = new HnfSolver(buttonMatrix, [.. Joltages]);

            return solver.Solve();
        }
    }
}