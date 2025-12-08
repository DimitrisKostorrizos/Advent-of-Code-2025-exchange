namespace Y25Day08
{
    /// <summary>
    /// Represents a circuit
    /// </summary>
    public sealed class Circuit
    {
        /// <summary>
        /// The field for the <see cref="JunctionBoxes"/>
        /// </summary>
        private readonly HashSet<Point> _junctionBoxes = [];

        /// <summary>
        /// The junction boxes
        /// </summary>
        public IEnumerable<Point> JunctionBoxes => _junctionBoxes;

        /// <summary>
        /// The count of <see cref="JunctionBoxes"/>
        /// </summary>
        public int Count => _junctionBoxes.Count;

        /// <summary>
        /// Creates a new instance of <see cref="Circuit"/>
        /// </summary>
        /// <param name="junctionBox">The junction box</param>
        public Circuit(Point junctionBox) : this([junctionBox])
        {

        }

        /// <summary>
        /// Creates a new instance of <see cref="Circuit"/>
        /// </summary>
        /// <param name="junctionBoxes">The junction boxes</param>
        public Circuit(IEnumerable<Point> junctionBoxes) : base()
        {
            ArgumentNullException.ThrowIfNull(junctionBoxes);

            foreach (var junctionBox in junctionBoxes)
                _junctionBoxes.Add(junctionBox);
        }

        /// <summary>
        /// <inheritdoc/>
        /// </summary>
        /// <returns></returns>
        public override string ToString() => $"Count: {Count}";

        /// <summary>
        /// Adds the <paramref name="junctionBox"/>
        /// </summary>
        /// <param name="junctionBox">The junction box</param>
        /// <returns></returns>
        public bool AddJunctionBox(Point junctionBox)
            => _junctionBoxes.Add(junctionBox);
    }
}