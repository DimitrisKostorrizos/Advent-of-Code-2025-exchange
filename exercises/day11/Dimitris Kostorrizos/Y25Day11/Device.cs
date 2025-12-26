namespace Y25Day11
{
    /// <summary>
    /// Represents a device
    /// </summary>
    public sealed class Device
    {
        /// <summary>
        /// The field for the <see cref="OutputDevices"/>
        /// </summary>
        private readonly List<Device> _outputDevices = [];

        /// <summary>
        /// The output devices
        /// </summary>
        public IEnumerable<Device> OutputDevices => _outputDevices;

        /// <summary>
        /// The name of the device
        /// </summary>
        public string DeviceName { get; }

        /// <summary>
        /// A flag indicating whether the device has output devices
        /// </summary>
        public bool HasOutputDevices => _outputDevices.Count > 0;

        /// <summary>
        /// Creates a new instance of <see cref="Device"/>
        /// </summary>
        /// <param name="deviceName">The name of the device</param>
        public Device(string deviceName) : base()
        {
            ArgumentException.ThrowIfNullOrWhiteSpace(deviceName);

            DeviceName = deviceName;
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => DeviceName;

        /// <summary>
        /// Adds the <paramref name="device"/> as an output deice
        /// </summary>
        /// <param name="device">The device</param>
        public void AddOutputDevice(Device device)
        {
            ArgumentNullException.ThrowIfNull(device);

            _outputDevices.Add(device);
        }
    }
}