namespace Y25Day08
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

            var junctionBoxes = new List<Point>();

            var distancePerPoints = new Dictionary<JunctionBoxKey, double>();

            await foreach (var line in fileContent)
            {
                var point = Point.Create(line);

                junctionBoxes.Add(point);
            }

            foreach (var junctionBox in junctionBoxes)
            {
                foreach (var otherJunctionBox in junctionBoxes)
                {
                    if (junctionBox == otherJunctionBox)
                        continue;

                    var key = new JunctionBoxKey(junctionBox, otherJunctionBox);

                    var reversedKey = new JunctionBoxKey(otherJunctionBox, junctionBox);

                    if (distancePerPoints.ContainsKey(key) || distancePerPoints.ContainsKey(reversedKey))
                        continue;

                    distancePerPoints.Add(key, junctionBox.GetDistance(otherJunctionBox));
                }
            }

            var numberOfConnections = 1000;

            if (_useDemoFile)
                numberOfConnections = 10;

            var sortedDistancePerPoint = new PriorityQueue<JunctionBoxKey, double>();

            foreach (var item in distancePerPoints)
                sortedDistancePerPoint.Enqueue(item.Key, item.Value);

            var circuits = junctionBoxes.Select(x => new Circuit(x)).ToList();

            while (numberOfConnections > 0)
            {
                var minimumDistanceCircuit = sortedDistancePerPoint.Dequeue();

                var matchingCircuitsForFirstPoint = circuits.FirstOrDefault(x => x.JunctionBoxes.Contains(minimumDistanceCircuit.FirstPoint));

                var matchingCircuitsForSecondPoint = circuits.FirstOrDefault(x => x.JunctionBoxes.Contains(minimumDistanceCircuit.SecondPoint));

                // If the point would connect to different circuits...
                if (matchingCircuitsForFirstPoint is not null &&
                    matchingCircuitsForSecondPoint is not null &&
                    matchingCircuitsForFirstPoint != matchingCircuitsForSecondPoint)
                {
                    // Merge the circuits
                    circuits.Remove(matchingCircuitsForSecondPoint);

                    foreach (var points in matchingCircuitsForSecondPoint.JunctionBoxes)
                        matchingCircuitsForFirstPoint.AddJunctionBox(points);
                }

                numberOfConnections--;
            }

            var largestCircuitsCount = circuits.Select(x => x.Count).OrderDescending().Take(3);

            var result = 1;

            foreach (var count in largestCircuitsCount)
                result *= count;

            Console.WriteLine($"The solution is {result}. Hope you liked it. Press any key to close the console.");

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

            var junctionBoxes = new List<Point>();

            var distancePerPoints = new Dictionary<JunctionBoxKey, double>();

            await foreach (var line in fileContent)
            {
                var point = Point.Create(line);

                junctionBoxes.Add(point);
            }

            foreach (var junctionBox in junctionBoxes)
            {
                foreach (var otherJunctionBox in junctionBoxes)
                {
                    if (junctionBox == otherJunctionBox)
                        continue;

                    var key = new JunctionBoxKey(junctionBox, otherJunctionBox);

                    var reversedKey = new JunctionBoxKey(otherJunctionBox, junctionBox);

                    if (distancePerPoints.ContainsKey(key) || distancePerPoints.ContainsKey(reversedKey))
                        continue;

                    distancePerPoints[key] = junctionBox.GetDistance(otherJunctionBox);
                }
            }

            var connectingJunctionBox = default(JunctionBoxKey);

            var sortedDistancePerPoint = new PriorityQueue<JunctionBoxKey, double>();

            foreach (var item in distancePerPoints)
                sortedDistancePerPoint.Enqueue(item.Key, item.Value);

            var circuits = junctionBoxes.Select(x => new Circuit(x)).ToList();

            while (connectingJunctionBox == default)
            {
                var minimumDistanceCircuit = sortedDistancePerPoint.Dequeue();

                var matchingCircuitsForFirstPoint = circuits.First(x => x.JunctionBoxes.Contains(minimumDistanceCircuit.FirstPoint));

                var matchingCircuitsForSecondPoint = circuits.First(x => x.JunctionBoxes.Contains(minimumDistanceCircuit.SecondPoint));

                // If the point would connect to different circuits...
                if (matchingCircuitsForFirstPoint != matchingCircuitsForSecondPoint)
                {
                    if (circuits.Count == 2)
                        connectingJunctionBox = minimumDistanceCircuit;

                    // Merge the circuits
                    circuits.Remove(matchingCircuitsForSecondPoint);

                    foreach (var points in matchingCircuitsForSecondPoint.JunctionBoxes)
                        matchingCircuitsForFirstPoint.AddJunctionBox(points);
                }
            }

            var result = connectingJunctionBox.FirstPoint.X * (long)connectingJunctionBox.SecondPoint.X;

            Console.WriteLine($"The solution is {result}. Hope you liked it. Press any key to close the console.");

            Console.Read();
        }
    }
}