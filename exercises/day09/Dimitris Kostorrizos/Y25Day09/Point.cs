using System.Globalization;

namespace Y25Day09
{
    /// <summary>
    /// Represents a point in a two dimensional space
    /// </summary>
    public readonly struct Point : IEquatable<Point>
    {
        /// <summary>
        /// The distance on the X axis
        /// </summary>
        public int X { get; }

        /// <summary>
        /// The distance on the Y axis
        /// </summary>
        public int Y { get; }

        /// <summary>
        /// Creates a new instance of <see cref="Point"/>
        /// </summary>
        /// <param name="x">The distance on the X axis</param>
        /// <param name="y">The distance on the Y axis</param>
        public Point(int x, int y)
        {
            ArgumentOutOfRangeException.ThrowIfNegative(x);

            ArgumentOutOfRangeException.ThrowIfNegative(y);

            X = x;

            Y = y;
        }

        /// <summary>
        /// Creates and returns a <see cref="Point"/> from the specified <paramref name="stringRepresentation"/>
        /// </summary>
        /// <param name="stringRepresentation">The string representation</param>
        /// <returns></returns>
        public static Point Create(string stringRepresentation)
        {
            ArgumentException.ThrowIfNullOrWhiteSpace(stringRepresentation);

            var parts = stringRepresentation.Split(',', StringSplitOptions.TrimEntries);

            var x = int.Parse(parts[0], CultureInfo.InvariantCulture);

            var y = int.Parse(parts[1], CultureInfo.InvariantCulture);

            return new(x, y);
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => $"({X}, {Y})";

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="obj">The value</param>
        /// <returns></returns>
        public override bool Equals(object? obj)
            => obj is Point key && Equals(key);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="other">The other object</param>
        /// <returns></returns>
        public bool Equals(Point other)
            => X == other.X &&
            Y == other.Y;

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override int GetHashCode()
            => HashCode.Combine(X, Y);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="left">The left operand</param>
        /// <param name="right">The right operand</param>
        /// <returns></returns>
        public static bool operator ==(Point left, Point right)
            => left.Equals(right);

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <param name="left">The left operand</param>
        /// <param name="right">The right operand</param>
        /// <returns></returns>
        public static bool operator !=(Point left, Point right)
            => !(left == right);
    }
}