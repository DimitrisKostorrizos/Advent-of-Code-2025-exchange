import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * see: https://adventofcode.com/2025/day/08
 */
public class Y25Day08 {
	
	public static record InputData(int x, int y, int z) {}

	private static final String INPUT_RX = "^([0-9]+),([0-9]+),([0-9]+)$";
	
	public static class InputProcessor implements Iterable<InputData>, Iterator<InputData> {
		private Scanner scanner;
		public InputProcessor(String inputFile) {
			try {
				scanner = new Scanner(new File(inputFile));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		@Override public Iterator<InputData> iterator() { return this; }
		@Override public boolean hasNext() { return scanner.hasNext(); }
		@Override public InputData next() {
			String rawLine = scanner.nextLine();
			String line = rawLine.trim();
			while (line.length() == 0) {
				line = scanner.nextLine();
			}
			if (line.matches(INPUT_RX)) {
				int x = Integer.parseInt(line.replaceFirst(INPUT_RX, "$1"));
				int y = Integer.parseInt(line.replaceFirst(INPUT_RX, "$2"));
				int z = Integer.parseInt(line.replaceFirst(INPUT_RX, "$3"));
				return new InputData(x,y,z);
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}
	
	public static record Pos3D(double x, double y, double z) {
		@Override
		public final String toString() {
			return "("+x+","+y+","+z+")";
		}
		public double distanceTo(Pos3D other) {
			double dx = this.x - other.x;
			double dy = this.y - other.y;
			double dz = this.z - other.z;
			return Math.sqrt(dx*dx + dy*dy + dz*dz);
		}
	}
	
	public static record Connection(double distance, int fromJB, int toJB) {
		@Override
		public String toString() {
			return "["+distance+"|"+fromJB+"->"+toJB+"]";
		}
	}

	
	public static class World {
		List<Pos3D> junctionBoxes;
		List<Set<Integer>> circuitList;
		public World() {
			junctionBoxes = new ArrayList<>();
			circuitList = new ArrayList<>();
		}
		@Override
		public String toString() {
			return junctionBoxes.toString();
		}
		public void addJunctionBox(InputData data) {
			junctionBoxes.add(new Pos3D(data.x(), data.y(), data.z()));
		}
		public void connectCircuits(int shortestConnections) {
			List<Connection> connections = new ArrayList<>();
			for (int i=0; i<junctionBoxes.size(); i++) {
				Pos3D jb1 = junctionBoxes.get(i);
				for (int j=i+1; j<junctionBoxes.size(); j++) {
					Pos3D jb2 = junctionBoxes.get(j);
					double distance = jb1.distanceTo(jb2);
					connections.add(new Connection(distance, i, j));
				}
			}
			connections.sort((c1,c2) -> Double.compare(c1.distance, c2.distance));
			for (int con=0; con<shortestConnections; con++) {
				Connection connection = connections.get(con);
				System.out.println(con + ": "+connection.distance+"|"+junctionBoxes.get(connection.fromJB)+"->"+junctionBoxes.get(connection.toJB));
				addCircuitSets(connection.fromJB, connection.toJB);
			}
			List<Integer> circuitSizes = new ArrayList<>();
			for (int cir=0; cir<circuitList.size(); cir++) {
				int cirSize = circuitList.get(cir).size();
				System.out.println("Circuit "+cir+": "+cirSize);
				circuitSizes.add(cirSize);
			}
			System.out.println(circuitSizes);
			Collections.sort(circuitSizes);
			circuitSizes.sort((i1, i2) -> i2-i1);
			System.out.println(circuitSizes);
			long result = 1;
			for (int i=0; i<3; i++) {
				int cirSize = circuitSizes.get(i);
				System.out.println("MAX["+(i+1)+"] "+cirSize);
				result *= cirSize;
			}
			System.out.println("Result: "+result);
		}
		public void connectAllCircuits() {
			List<Connection> connections = new ArrayList<>();
			for (int i=0; i<junctionBoxes.size(); i++) {
				Pos3D jb1 = junctionBoxes.get(i);
				for (int j=i+1; j<junctionBoxes.size(); j++) {
					Pos3D jb2 = junctionBoxes.get(j);
					double distance = jb1.distanceTo(jb2);
					connections.add(new Connection(distance, i, j));
				}
			}
			connections.sort((c1,c2) -> Double.compare(c1.distance, c2.distance));
			for (int i=0; i<junctionBoxes.size()-1; i++) {
				Set<Integer> newCircuit = new HashSet<>();
				newCircuit.add(i);
				circuitList.add(newCircuit);
			}
			for (int con=0; con<connections.size(); con++) {
				Connection connection = connections.get(con);
				System.out.println(con + ": "+connection.distance+"|"+junctionBoxes.get(connection.fromJB)+"->"+junctionBoxes.get(connection.toJB));
				addCircuitSets(connection.fromJB, connection.toJB);
				if (circuitList.size() == 1) {
					Pos3D jb1 = junctionBoxes.get(connection.fromJB);
					Pos3D jb2 = junctionBoxes.get(connection.toJB);
					System.out.println(jb1+"->"+jb2);
					System.out.println("RESULT: "+jb1.x*jb2.x);
					break;
				}
			}
		}
		private void addCircuitSets(int jb1, int jb2) {
			int cir1 = findCircuit(jb1);
			int cir2 = findCircuit(jb2);
			if (cir1 == -1 && cir2 == -1) {
				Set<Integer> newCircuit = new HashSet<>();
				newCircuit.add(jb1);
				newCircuit.add(jb2);
				circuitList.add(newCircuit);
			}
			else if (cir1 == cir2) {
				return;
			}
			else if (cir2 == -1) {
				circuitList.get(cir1).add(jb2);
			}
			else if (cir1 == -1) {
				circuitList.get(cir2).add(jb1);
			}
			else {
				Set<Integer> circuit1 = circuitList.get(cir1);
				Set<Integer> circuit2 = circuitList.get(cir2);
				circuit1.addAll(circuit2);
				circuitList.remove(cir2);
			}
		}
		private int findCircuit(int jb) {
			for (int cir=0; cir<circuitList.size(); cir++) {
				if (circuitList.get(cir).contains(jb)) {
					return cir;
				}
			}
			return -1;
		}
	}

	
	public static void mainPart1(String inputFile, int numConnections) throws FileNotFoundException {
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			world.addJunctionBox(data);
		}
		System.out.println(world.toString());
		world.connectCircuits(numConnections);
	}


	public static void mainPart2(String inputFile) {
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			world.addJunctionBox(data);
		}
		System.out.println(world.toString());
		world.connectAllCircuits();
	}


	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day08/Feri/input-example.txt", 10);
		mainPart1("exercises/day08/Feri/input.txt", 1000);   // not 1472
		System.out.println("---------------");
		System.out.println("--- PART II ---");
//		mainPart2("exercises/day08/Feri/input-example.txt", 10);
		mainPart2("exercises/day08/Feri/input.txt");    // not 25272
		System.out.println("---------------");    // 
	}
	
}
