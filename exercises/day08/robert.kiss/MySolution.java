import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MySolution extends MySolutionBase {

	private final char[] instructions;
	private final Map<String, Node> nodes = new TreeMap<>();

	public MySolution(String inputFilename) {
        super(inputFilename);
		this.instructions = getInputLinesAsList().get(0).toCharArray();
		for (String line:getInputLinesAsList().subList(2,getInputLinesAsList().size())) {
			var node = Node.createNode(line);
			if (node!=null) {
				this.nodes.put(node.nodeid, node);
			}
		}
    }

    private MySolution play1() {
		int step = 0;
		var myNode = this.nodes.get("AAA");
		while (!myNode.nodeid.equals("ZZZ")) {
			var instruction = instructions[step % instructions.length];
			myNode = nodes.get(myNode.getNextNodeId(instruction));
			step +=1;
		}
		System.out.println("Part 1: "+step);
        return this;
	}


	private Position nextPosition(Position pos0) {
		Node node0 = this.nodes.get(pos0.nodeid);
		char instruction = this.instructions[pos0.ipos];
		return new Position(node0.getNextNodeId(instruction), (pos0.ipos + 1) % this.instructions.length);
	}

	Map<Position,Jump> jumps = new TreeMap<>();

	private Route routeToNextEndPosition(Route route0) {
		Jump jump = jumps.get(route0.position);
		if (jump == null){
			Route route1 = new Route(0L,route0.position);
			while ( route1.step==0 || !route1.position.nodeid.endsWith("Z")) {
				route1 = new Route(route1.step + 1, nextPosition(route1.position));
			}
			jump = new Jump(route1.step, route1.position);
			jumps.put(route0.position,jump);
		}
		return new Route(route0.step + jump.dist, jump.position);
	}

	private MySolution play2() {
		var myRoutes = this.nodes.values().stream()
				.filter(node->node.nodeid.endsWith("A"))
				.map(node->new Route(0L,new Position(node.nodeid,0)))
				.collect(Collectors.toList()).toArray(new Route[0]);
		long minStep = 0L;
		long maxStep = 1L;
		while (minStep<maxStep) {
			minStep = maxStep;
			for (int i=0;i<myRoutes.length;i++) {
				while (myRoutes[i].step<maxStep){
					myRoutes[i] = routeToNextEndPosition(myRoutes[i]);
				}
				maxStep = myRoutes[i].step;
			}
		}
		//System.out.println("Jump cache:\n"+jumps.entrySet().stream().map(e->e.getKey().toString() + " -> "+e.getValue().toString()).collect(Collectors.joining("\n")));
		System.out.println("Part 2: "+maxStep);
		return this;
	}

	public static void main(String args[]) {
		try {
			Instant start = Instant.now();
            new MySolution("sample.txt").play1().play2();
			new MySolution("sample2.txt").play2();
            new MySolution("input.txt").play1().play2();
			Instant end = Instant.now();
			var duration = Duration.between(start, end);
			System.out.println(String.format("==================================\nTime elapsed (hh:mm:ss) %02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

/*

========== Processing sample.txt ==========
Part 1: 2
Part 2: 2
========== Processing sample2.txt ==========
Part 2: 6
========== Processing input.txt ==========
Part 1: 18727
Part 2: 18024643846273
==================================
Time elapsed (hh:mm:ss) 00:01:42

 */
