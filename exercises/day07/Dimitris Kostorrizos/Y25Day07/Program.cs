namespace Y25Day07
{
    internal static partial class Program
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

            var amountOfSplits = 0;

            var beamColumnIndexes = new HashSet<int>();

            await foreach (var line in fileContent)
            {
                foreach (var (index, element) in line.Index())
                {
                    if(element == 'S')
                        beamColumnIndexes.Add(index);

                    if (element == '^' && beamColumnIndexes.Contains(index))
                    {
                        beamColumnIndexes.Remove(index);

                        beamColumnIndexes.Add(index - 1);

                        beamColumnIndexes.Add(index + 1);

                        amountOfSplits++;
                    }
                }
            }

            Console.WriteLine($"The solution is {amountOfSplits}. Hope you liked it. Press any key to close the console.");

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

            var fileContent = await File.ReadAllLinesAsync(fileName);

            var timelines = new List<List<int>>();

            var startingPoint = fileContent[0].IndexOf('S');

            timelines.Add([startingPoint]);

            fileContent = [.. fileContent.Skip(1).Where(x => x.Contains('^'))];

            var timelineCount = CountCreatedTimelines(startingPoint, 0, fileContent, []);

            Console.WriteLine($"The solution is {timelineCount}. Hope you liked it. Press any key to close the console.");

            Console.Read();
        }

        /// <summary>
        /// Counts the created timelines
        /// </summary>
        /// <param name="startingPoint">The starting point</param>
        /// <param name="lineIndex">The line index</param>
        /// <param name="lines">The lines</param>
        /// <param name="cache">The cache</param>
        /// <returns></returns>
        private static long CountCreatedTimelines(int startingPoint, int lineIndex, string[] lines, Dictionary<CacheKey, long> cache)
        {
            if (lineIndex == lines.Length)
                return 1;

            var key = new CacheKey(startingPoint, lineIndex);

            if (cache.TryGetValue(key, out var cached))
                return cached;

            long result;

            var currentLine = lines[lineIndex];

            var nextLineIndex = lineIndex + 1;

            if (currentLine[startingPoint] == '^')
            {
                var leftPathResult = CountCreatedTimelines(startingPoint - 1, nextLineIndex, lines, cache);

                var rightPathResult = CountCreatedTimelines(startingPoint + 1, nextLineIndex, lines, cache);

                result = leftPathResult + rightPathResult;
            }
            else
                result = CountCreatedTimelines(startingPoint, nextLineIndex, lines, cache);

            cache[key] = result;

            return result;
        }
    }
}