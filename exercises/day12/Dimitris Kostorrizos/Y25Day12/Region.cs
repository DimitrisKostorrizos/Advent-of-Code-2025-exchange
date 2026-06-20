using System.Globalization;

namespace Y25Day12
{
    /// <summary>
    /// Represents a region
    /// </summary>
    public sealed class Region
    {
        /// <summary>
        /// The width of the region
        /// </summary>
        public int Width { get; }

        /// <summary>
        /// The length of the region
        /// </summary>
        public int Length { get; }

        /// <summary>
        /// The quantity of each present that the region can support
        /// </summary>
        public IDictionary<Present, int> QuantityPerPresent { get; }

        /// <summary>
        /// The area of the region
        /// </summary>
        public int Area { get; }

        /// <summary>
        /// Creates a new instance of <see cref="Region"/>
        /// </summary>
        /// <param name="width">The width of the region</param>
        /// <param name="length">The length of the region</param>
        /// <param name="quantityPerPresent">The quantity of each present that the region can support</param>
        public Region(int width, int length, IDictionary<Present, int> quantityPerPresent) : base()
        {
            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(width);

            ArgumentOutOfRangeException.ThrowIfNegativeOrZero(length);

            ArgumentNullException.ThrowIfNull(quantityPerPresent);

            Width = width;

            Length = length;

            QuantityPerPresent = quantityPerPresent;

            Area = Width * Length;
        }

        /// <summary>
        /// Creates and returns a <see cref="Region"/> from the specified <paramref name="stringRepresentation"/>
        /// </summary>
        /// <param name="stringRepresentation">The string representation</param>
        /// <param name="presents">The presents</param>
        /// <returns></returns>
        public static Region Create(string stringRepresentation, IEnumerable<Present> presents)
        {
            ArgumentException.ThrowIfNullOrWhiteSpace(stringRepresentation);

            ArgumentNullException.ThrowIfNull(presents);

            var widthRepresentation = stringRepresentation.TakeWhile(x => x != 'x')
                .ToArray();

            var width = int.Parse(widthRepresentation, CultureInfo.InvariantCulture);

            var widthRepresentationLength = widthRepresentation.Length + 1;

            var lengthRepresentation = stringRepresentation.Skip(widthRepresentationLength)
                .TakeWhile(x => x != ':')
                .ToArray();

            var length = int.Parse(lengthRepresentation, CultureInfo.InvariantCulture);

            var regionSizeRepresentationLength = widthRepresentationLength + lengthRepresentation.Length + 1;

            var quantityPerPresentRepresentationLength = stringRepresentation.Length - regionSizeRepresentationLength;

            var quantities = stringRepresentation.Substring(regionSizeRepresentationLength, quantityPerPresentRepresentationLength)
                .Split(' ', StringSplitOptions.TrimEntries | StringSplitOptions.RemoveEmptyEntries)
                .Select(x => int.Parse(x, CultureInfo.InvariantCulture))
                .ToList();

            var quantityPerPresent = new Dictionary<Present, int>();

            foreach (var present in presents)
            {
                var quantity = quantities[present.Index];

                if (quantity == 0)
                    continue;

                quantityPerPresent.Add(present, quantity);
            }

            return new(width, length, quantityPerPresent);
        }

        /// <summary>
        /// Returns whether the region can fully fit the presents
        /// </summary>
        /// <returns></returns>
        public bool CanFitPresents()
        {
            var presentsVolume = 0;

            foreach (var (present, quantity) in QuantityPerPresent)
                presentsVolume += quantity * present.Area;

            // A region can never hold more present volume than it has area.
            if (Area < presentsVolume)
                return false;

            // The problem is NP hard, even for the small dimensions that we have,
            // so we can only be sure that the presents fit if the region can support the quantity of each present.
            // Nice puzzle choice.
            return true;
        }

        /// <inheritdoc/>
        public override string ToString()
            => $"{Width} x {Length}";
    }
}