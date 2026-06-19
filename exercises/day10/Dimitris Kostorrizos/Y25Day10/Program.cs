namespace Y25Day10
{
    internal static class Program
    {
        /// <summary>
        /// A  flag indicating whether to use the demo input file or the actual one
        /// </summary>
        private static readonly bool _useDemoFile = false;

        private static async Task Main()
        {
            await ExecuteFirstHalfAsync();

            await ExecuteSecondHalfAsync();
        }

        /// <summary>
        /// Executes the code for the first half of the exercise
        /// </summary>
        /// <returns></returns>
        public static async Task ExecuteFirstHalfAsync()
        {
            var fileName = "Input.txt";

            if (_useDemoFile)
                fileName = "DemoInput.txt";

            var fileContent = File.ReadLinesAsync(fileName);

            var fewestButtonPresses = 0;

            await foreach (var line in fileContent)
            {
                var machine = Machine.Create(line);

                fewestButtonPresses += machine.GetMinimumButtonPressesForLights();
            }

            Console.WriteLine($"The solution is {fewestButtonPresses}. Hope you liked it. Press any key to close the console.");

            Console.Read();
        }

        /// <summary>
        /// Executes the code for the second half of the exercise
        /// </summary>
        /// <returns></returns>
        public static async Task ExecuteSecondHalfAsync()
        {
            var fileName = "Input.txt";

            if (_useDemoFile)
                fileName = "DemoInput.txt";

            var fileContent = File.ReadLinesAsync(fileName);

            var fewestButtonPresses = 0;

            await foreach (var line in fileContent)
            {
                var machine = Machine.Create(line);

                fewestButtonPresses += machine.GetMinimumButtonPressesForJoltage();
            }

            Console.WriteLine($"The solution is {fewestButtonPresses}. Hope you liked it. Press any key to close the console.");

            Console.Read();
        }
    }
}