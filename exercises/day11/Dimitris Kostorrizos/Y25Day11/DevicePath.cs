namespace Y25Day11
{
    /// <summary>
    /// Represents a path of devices
    /// </summary>
    public sealed class DevicePath
    {
        /// <summary>
        /// The separator used to create the path
        /// </summary>
        public const char Seperator = ',';

        /// <summary>
        /// The names of the devices
        /// </summary>
        private readonly List<string> _deviceNames = [];

        /// <summary>
        /// The path
        /// </summary>
        public string Path => string.Join(Seperator, _deviceNames);

        /// <summary>
        /// Creates a new instance of <see cref="DevicePath"/>
        /// </summary>
        /// <param name="device">The device</param>
        public DevicePath(Device device) : this()
        {
            ArgumentNullException.ThrowIfNull(device);

            _deviceNames.Add(device.DeviceName);
        }

        /// <summary>
        /// Creates a new instance of <see cref="DevicePath"/>
        /// </summary>
        private DevicePath() : base()
        {

        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => $"{Path}{Seperator}out";

        /// <summary>
        /// Inserts the <paramref name="device"/> at the first position
        /// </summary>
        /// <param name="device">The device</param>
        public void InsertDevice(Device device)
            => _deviceNames.Insert(0, device.DeviceName);
    }
}