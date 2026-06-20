namespace Y25Day12
{
    internal static class Program
    {
        /// <summary>
        /// A  flag indicating whether to use the demo input file or the actual one
        /// </summary>
        private static readonly bool _useDemoFile = false;

        /// <summary>
        /// The number of presents to be arranged
        /// </summary>
        private const int NumberOfPresents = 6;

        private static async Task Main()
        {
            var fileName = "Input.txt";

            if (_useDemoFile)
                fileName = "DemoInput.txt";

            var fileContent = await File.ReadAllLinesAsync(fileName);

            var validRegionCount = 0;

            var presentSectionSize = NumberOfPresents * Present.PresentRepresentationSize;

            var presents = fileContent
                .Take(presentSectionSize)
                .Chunk(Present.PresentRepresentationSize)
                .Select(Present.Create)
                .ToList();

            var regions = fileContent.Skip(presentSectionSize)
                .Select(x => Region.Create(x, presents));

            foreach (var region in regions)
            {
                var canFitPresents = region.CanFitPresents();

                if (canFitPresents)
                    validRegionCount++;
            }

            Console.WriteLine($"The solution is {validRegionCount}. Hope you liked it. Press any key to close the console.");

            Console.Read();
        }
    }
}