using System.Collections.Frozen;

namespace Y25Day09
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

            var redTilePoints = new List<Point>();

            await foreach (var line in fileContent)
            {
                var point = Point.Create(line);

                redTilePoints.Add(point);
            }

            var areas = new List<RectangleArea>();

            for (var firstPointIndex = 0; firstPointIndex < redTilePoints.Count; firstPointIndex++)
            {
                var firstPoint = redTilePoints[firstPointIndex];

                for (var secondPointIndex = firstPointIndex + 1; secondPointIndex < redTilePoints.Count; secondPointIndex++)
                {
                    var secondPoint = redTilePoints[secondPointIndex];

                    var area = ((ulong)Math.Abs(firstPoint.X - secondPoint.X) + 1) * ((ulong)Math.Abs(firstPoint.Y - secondPoint.Y) + 1);

                    areas.Add(new RectangleArea(firstPoint, secondPoint, area));
                }
            }

            var largestArea = areas.Max(x => x.Area);

            Console.WriteLine($"The solution is {largestArea}. Hope you liked it. Press any key to close the console.");

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

            var redTilePoints = new List<Point>();

            await foreach (var line in fileContent)
            {
                var point = Point.Create(line);

                redTilePoints.Add(point);
            }

            var areas = new List<RectangleArea>();

            for (var firstPointIndex = 0; firstPointIndex < redTilePoints.Count; firstPointIndex++)
            {
                var firstPoint = redTilePoints[firstPointIndex];

                for (var secondPointIndex = firstPointIndex + 1; secondPointIndex < redTilePoints.Count; secondPointIndex++)
                {
                    var secondPoint = redTilePoints[secondPointIndex];

                    var area = ((ulong)Math.Abs(firstPoint.X - secondPoint.X) + 1) * ((ulong)Math.Abs(firstPoint.Y - secondPoint.Y) + 1);

                    areas.Add(new RectangleArea(firstPoint, secondPoint, area));
                }
            }

            var validPoints = GeneratePerimeterPoints(redTilePoints);

            foreach (var validPoint in validPoints)
                Cache.Add(validPoint, true);

            var sameRowPoints = new Dictionary<int, IEnumerable<Point>>();

            foreach (var point in validPoints.GroupBy(x => x.X))
                sameRowPoints.Add(point.Key, point);

            SameRowPoints = sameRowPoints.ToFrozenDictionary();

            var sameColumnPoints = new Dictionary<int, IEnumerable<Point>>();

            foreach (var point in validPoints.GroupBy(x => x.Y))
            {
                sameColumnPoints.Add(point.Key, point);
            }

            SameColumnPoints = sameColumnPoints.ToFrozenDictionary();

            var largestArea = default(RectangleArea);

            foreach (var area in areas.OrderByDescending(x => x.Area))
            {
                if (!IsAreaValid(validPoints, area))
                    continue;

                if (area.Area > largestArea.Area)
                { 
                    largestArea = area;

                    break;
                }
            }

            Console.WriteLine($"The solution is {largestArea.Area}. Hope you liked it. Press any key to close the console.");

            Console.Read();
        }

        /// <summary>
        /// The cache for the valid points
        /// </summary>
        private static readonly Dictionary<Point, bool> Cache = [];

        /// <summary>
        /// Contains the points that belong to the same row
        /// </summary>
        private static FrozenDictionary<int, IEnumerable<Point>> SameRowPoints = default!;

        /// <summary>
        /// Contains the points that belong to the same column
        /// </summary>
        private static FrozenDictionary<int, IEnumerable<Point>> SameColumnPoints = default!;

        /// <summary>
        /// Returns the points in the perimeter based on the <paramref name="redTilePoints"/>
        /// </summary>
        /// <param name="redTilePoints">The red tile points</param>
        /// <returns></returns>
        private static FrozenSet<Point> GeneratePerimeterPoints(IEnumerable<Point> redTilePoints)
        {
            ArgumentNullException.ThrowIfNull(redTilePoints);

            var perimeterPoints = new HashSet<Point>(redTilePoints);

            var perimeterSequence = GetPerimeterSequence(redTilePoints);

            var edges = GenerateEdgesForPerimeter(perimeterSequence);

            foreach (var edge in edges)
            {
                foreach (var point in edge.GetPoints())
                {
                    perimeterPoints.Add(point);
                }
            }

            return perimeterPoints.ToFrozenSet();
        }

        /// <summary>
        /// Returns the edges for the perimeter
        /// </summary>
        /// <param name="perimeter">The perimeter</param>
        /// <returns></returns>
        private static List<Edge> GenerateEdgesForPerimeter(List<Point> perimeter)
        {
            ArgumentNullException.ThrowIfNull(perimeter);

            var edges = new List<Edge>();

            for (var startingIndex = 0; startingIndex < perimeter.Count; startingIndex++)
            {
                var startingPoint = perimeter[startingIndex];

                var endingIndex = startingIndex + 1;

                if (endingIndex == perimeter.Count)
                    endingIndex = 0;

                var endingPoint = perimeter[endingIndex];

                edges.Add(new Edge(startingPoint, endingPoint));
            }

            return edges;
        }

        /// <summary>
        /// Returns the perimeter sequence based on the <paramref name="redTilePoints"/>
        /// </summary>
        /// <param name="redTilePoints">The red tile points</param>
        /// <returns></returns>
        private static List<Point> GetPerimeterSequence(IEnumerable<Point> redTilePoints)
        {
            ArgumentNullException.ThrowIfNull(redTilePoints);

            var perimeter = new List<Point>();

            var remainingPoints = redTilePoints.ToList();

            var nextPoint = remainingPoints[0];

            perimeter.Add(nextPoint);

            remainingPoints.Remove(nextPoint);

            while (remainingPoints.Count > 0)
            {
                nextPoint = remainingPoints.First(x => x.X == nextPoint.X || x.Y == nextPoint.Y);

                perimeter.Add(nextPoint);

                remainingPoints.Remove(nextPoint);
            }

            return perimeter;
        }

        /// <summary>
        /// Returns whether the <paramref name="area"/> is contained in the <paramref name="validPoints"/>
        /// </summary>
        /// <param name="validPoints">The valid points</param>
        /// <param name="area">The area</param>
        /// <returns></returns>
        private static bool IsAreaValid(FrozenSet<Point> validPoints, RectangleArea area)
        {
            ArgumentNullException.ThrowIfNull(validPoints);

            if (!area.IsLine)
            {
                if (!IsPointValid(validPoints, area.SecondCorner.Value))
                    return false;

                if (!IsPointValid(validPoints, area.FourthCorner.Value))
                    return false;
            }

            var edges = area.GetEdges();

            foreach (var edge in edges)
            {
                var points = edge.GetPoints();

                if (points.Any(x => !IsPointValid(validPoints, x)))
                    return false;
            }

            return true;
        }

        /// <summary>
        /// Returns whether the <paramref name="point"/> is valid
        /// </summary>
        /// <param name="validPoints">The valid points</param>
        /// <param name="point">The point</param>
        /// <returns></returns>
        private static bool IsPointValid(FrozenSet<Point> validPoints, Point point)
        {
            ArgumentNullException.ThrowIfNull(validPoints);

            if (Cache.TryGetValue(point, out var cacheResult))
                return cacheResult;

            var result = false;

            if (validPoints.Contains(point))
                result = true;
            else
            {
                // Ray-Casting
                var sameXPoints = SameRowPoints[point.X];

                var topPointExists = sameXPoints.Any(x => x.Y <= point.Y);

                if (topPointExists)
                {
                    var bottomPointExists = sameXPoints.Any(x => x.Y >= point.Y);

                    if (bottomPointExists)
                    {
                        var sameYPoints = SameColumnPoints[point.Y];

                        var rightPointExists = sameYPoints.Any(x => x.X >= point.X);

                        if (rightPointExists)
                        {
                            var leftPointExists = sameYPoints.Any(x => x.X <= point.X);

                            if (leftPointExists)
                                result = true;
                        }
                    }
                }
            }

            Cache.Add(point, result);

            return result;
        }
    }
}