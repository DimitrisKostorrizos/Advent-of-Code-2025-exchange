namespace Y25Day11
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
                fileName = "DemoInputFirstHalf.txt";

            var fileContent = await File.ReadAllLinesAsync(fileName);

            var startingDeviceName = "you";

            var deviceAndLines = new Dictionary<Device, string>();

            foreach (var line in fileContent)
            {
                var deviceName = line[..line.IndexOf(':')];

                deviceAndLines.Add(new Device(deviceName), line);
            }

            foreach (var device in deviceAndLines)
            {
                var deviceNameAndDelimiterLength = device.Key
                    .DeviceName
                    .Length + 1;

                var outputDeviceNames = device.Value[deviceNameAndDelimiterLength..]
                    .Split(' ', StringSplitOptions.RemoveEmptyEntries)
                    .Where(x => x != "out");

                foreach (var outputDeviceName in outputDeviceNames)
                {
                    var outputDevice = deviceAndLines.First(x => x.Key.DeviceName == outputDeviceName).Key;

                    device.Key.AddOutputDevice(outputDevice);
                }
            }

            var startingDevice = deviceAndLines.Select(x => x.Key)
                .First(x => x.DeviceName == startingDeviceName);

            var deviceSequences = GetPaths(startingDevice);

            var uniquePathCount = deviceSequences.ToHashSet().Count;

            Console.WriteLine($"The solution is {uniquePathCount}. Hope you liked it. Press any key to close the console.");

            Console.Read();
        }

        /// <summary>
        /// Returns the paths using the <paramref name="device"/> as the starting device
        /// </summary>
        /// <param name="device">The device</param>
        /// <returns></returns>
        private static List<DevicePath> GetPaths(Device device)
        {
            ArgumentNullException.ThrowIfNull(device);

            var results = new List<DevicePath>();

            if (!device.HasOutputDevices)
                results.Add(new DevicePath(device));
            else
            {
                foreach (var outputDevice in device.OutputDevices)
                {
                    var paths = GetPaths(outputDevice);

                    foreach (var path in paths)
                    {
                        path.InsertDevice(device);

                        results.Add(path);
                    }
                }
            }

            return results;
        }

        /// <summary>
        /// Executes the code for the second half of the exercise
        /// </summary>
        /// <returns></returns>
        public static async Task ExecuteSecondHalfAsync()
        {
            var fileName = "Input.txt";

            if (_useDemoFile)
                fileName = "DemoInputSecondHalf.txt";

            var fileContent = await File.ReadAllLinesAsync(fileName);

            var startingDeviceName = "svr";

            var deviceAndLines = new Dictionary<Device, string>();

            foreach (var line in fileContent)
            {
                var deviceName = line[..line.IndexOf(':')];

                deviceAndLines.Add(new Device(deviceName), line);
            }

            foreach (var device in deviceAndLines)
            {
                var deviceNameAndDelimiterLength = device.Key
                    .DeviceName
                    .Length + 1;

                var outputDeviceNames = device.Value[deviceNameAndDelimiterLength..]
                    .Split(' ', StringSplitOptions.RemoveEmptyEntries)
                    .Where(x => x != "out");

                foreach (var outputDeviceName in outputDeviceNames)
                {
                    var outputDevice = deviceAndLines.First(x => x.Key.DeviceName == outputDeviceName).Key;

                    device.Key.AddOutputDevice(outputDevice);
                }
            }

            var startingDevice = deviceAndLines.Select(x => x.Key)
                .First(x => x.DeviceName == startingDeviceName);

            var count = GetPathCount(startingDevice);

            Console.WriteLine($"The solution is {count}. Hope you liked it. Press any key to close the console.");

            Console.Read();
        }

        /// <summary>
        /// The cache for the each device traversal
        /// </summary>
        private static readonly Dictionary<(Device, bool, bool), ulong> _cache = [];

        /// <summary>
        /// Returns the count of paths starting from the <paramref name="device"/>
        /// </summary>
        /// <param name="device">The device</param>
        /// <param name="hasPassedThroughFFT">A flag indicating whether the path contains the FFT device</param>
        /// <param name="hasPassedThroughDAC">A flag indicating whether the path contains the DAC device</param>
        /// <returns></returns>
        private static ulong GetPathCount(Device device, bool hasPassedThroughFFT = false, bool hasPassedThroughDAC = false)
        {
            var key = (device, hasPassedThroughFFT, hasPassedThroughDAC);

            if (_cache.TryGetValue(key, out var cached))
            {
                return cached;
            }

            if (!device.HasOutputDevices && hasPassedThroughFFT && hasPassedThroughDAC)
            {
                return 1;
            }

            var isCurrentDeviceFFT = hasPassedThroughFFT;

            if (device.DeviceName == "fft")
                isCurrentDeviceFFT = true;

            var isCurrentDeviceDAC = hasPassedThroughDAC;

            if (device.DeviceName == "dac")
                isCurrentDeviceDAC = true;

            var result = 0UL;

            foreach (var outputDevice in device.OutputDevices)
            {
                result+= GetPathCount(outputDevice, isCurrentDeviceFFT, isCurrentDeviceDAC);
            }

            _cache[key] = result;

            return result;
        }
    }
}