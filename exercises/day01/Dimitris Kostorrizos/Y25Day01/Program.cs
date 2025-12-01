namespace Y25Day01
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

            var zeroPointingCounter = 0;

            var circularDial = new CircularDial(99, 00, 50);

            await foreach (var line in fileContent)
            {
                var dialShift = DialShift.Create(line);

                var shiftResult = circularDial.Shift(dialShift.Type, dialShift.NumberOfShifts);

                if (shiftResult.NewPosition == 0)
                    zeroPointingCounter++;
            }

            Console.WriteLine($"The solution is {zeroPointingCounter}. Hope you liked it. Press any key to close the console.");

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

            var zeroTraversingCounter = 0;

            var circularDial = new CircularDial(99, 00, 50);

            await foreach (var line in fileContent)
            {
                var dialShift = DialShift.Create(line);

                var shiftResult = circularDial.Shift(dialShift.Type, dialShift.NumberOfShifts);

                zeroTraversingCounter += shiftResult.TraversedValues.Count(x => x == 0);
            }

            Console.WriteLine($"The solution is {zeroTraversingCounter}. Hope you liked it. Press any key to close the console.");

            Console.Read();
        }
    }
}