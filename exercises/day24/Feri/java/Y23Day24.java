import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * see: https://adventofcode.com/2023/day/24
 */
public class Y23Day24 {

	/*
	 * Example:
	 * 
	 * 19, 13, 30 @ -2,  1, -2
	 * 18, 19, 22 @ -1, -1, -2
	 * 20, 25, 34 @ -2, -2, -4
	 * 12, 31, 28 @ -1, -2, -1
	 * 20, 19, 15 @  1, -5, -3
	 * 
	 */

	private static final String INPUT_RX = "^([-0-9]+), *([-0-9]+), *([-0-9]+) *@ *([-0-9]+), *([-0-9]+), *([-0-9]+)$";
	
	public static record InputData(Pos pos, Pos v) {
		@Override public String toString() { return pos+"->"+v; }
	}
	
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
			String line = scanner.nextLine().trim();
			while (line.length() == 0) {
				line = scanner.nextLine();
			}
			if (line.matches(INPUT_RX)) {
				long xFrom = Long.parseLong(line.replaceFirst(INPUT_RX, "$1"));
				long yFrom = Long.parseLong(line.replaceFirst(INPUT_RX, "$2"));
				long zFrom = Long.parseLong(line.replaceFirst(INPUT_RX, "$3"));
				long vxTo = Long.parseLong(line.replaceFirst(INPUT_RX, "$4"));
				long vyTo = Long.parseLong(line.replaceFirst(INPUT_RX, "$5"));
				long vzTo = Long.parseLong(line.replaceFirst(INPUT_RX, "$6"));
				return new InputData(new Pos(xFrom,yFrom,zFrom), new Pos(vxTo,vyTo,vzTo));
			}
			else {
				throw new RuntimeException("invalid line '"+line+"'");
			}
		}
	}


	static record Pos(double x, double y, double z) {
		@Override public String toString() { return "("+x+","+y+","+z+")"; }
		public Pos add(int dx, int dy, int dz) {
			return new Pos(x+dx, y+dy, z+dz);
		}
		public Pos add(Pos other) {
			return new Pos(x+other.x, y+other.y, z+other.z);
		}
		public Pos subtract(Pos other) {
			return new Pos(x-other.x, y-other.y, z-other.z);
		}
		public double manhattenDist(Pos other) {
			return Math.abs(x-other.x) + Math.abs(y-other.y) + Math.abs(z-other.z);
		}
		public Pos multiply(double k) {
			return new Pos(k*x, k*y, k*z);
		}
		public double magnitude() {
			return Math.sqrt(x*x+y*y+z*z);  
		}
		public Pos normalize() {
			double mag = magnitude();
			if (mag == 0) {
				return this;
			}
			return multiply(1/mag);  
		}
		
	}

	static AtomicInteger hailID = new AtomicInteger();
	
	static record Hail(String id, Pos pos, Pos v) {
		public Hail(Pos pos, Pos v) {
			this("H"+hailID.incrementAndGet(), pos, v);
		}

		// http://walter.bislins.ch/blog/index.asp?page=Schnittpunkt+zweier+Geraden+berechnen+%28JavaScript%29
		// 
		//		function IntersectLines( P, r, Q, s ) {
		//		  // line1 = P + lambda1 * r
		//		  // line2 = Q + lambda2 * s
		//		  // r and s must be normalized (length = 1)
		//		  // returns intersection point O of line1 with line2 = [ Ox, Oy ] 
		//		  // returns null if lines do not intersect or are identical
		//		  var PQx = Q[0] - P[0];
		//		  var PQy = Q[1] - P[1];
		//		  var rx = r[0];
		//		  var ry = r[1];
		//		  var rxt = -ry;
		//		  var ryt = rx;
		//		  var qx = PQx * rx + PQy * ry;
		//		  var qy = PQx * rxt + PQy * ryt;
		//		  var sx = s[0] * rx + s[1] * ry;
		//		  var sy = s[0] * rxt + s[1] * ryt;
		//		  // if lines are identical or do not cross...
		//		  if (sy == 0) return null;
		//		  var a = qx - qy * sx / sy;
		//		  return [ P[0] + a * rx, P[1] + a * ry ];
		//		}
	
		public Pos intersectXY(Y23Day24.Hail other) {
			Pos vNorm = new Pos(v.x, v.y, 0);
			double vFactor = vNorm.magnitude();
			vNorm = vNorm.normalize();
			Pos vOtherNorm = new Pos(other.v.x, other.v.y, 0).normalize();
			
			double PQx = other.pos.x - pos.x;
			double PQy = other.pos.y - pos.y;
			double rx = vNorm.x;
			double ry = vNorm.y;
			double rxt = -ry;
			double ryt = rx;
			double qx = PQx * rx + PQy * ry;
			double qy = PQx * rxt + PQy * ryt;
			double s0 = vOtherNorm.x;
			double s1 = vOtherNorm.y;
			double sx = s0 * rx + s1 * ry;
			double sy = s0 * rxt + s1 * ryt;
			if (sy == 0) {
				// lines are parallel
				return null;
			}
			double a = qx - qy * sx / sy;
			if (a<0) {
				return null;
			}
			double resultX = pos.x + a*v.x/vFactor;
			double resultY = pos.y + a*v.y/vFactor;
			double resultZ = pos.z + a*v.z/vFactor;
			return new Pos(resultX, resultY, resultZ);
		}
		
		
	}

	
	public static class World {
		List<Hail> hails;
		public World() {
			this.hails= new ArrayList<>();
		}
		public void addHail(Hail hail) {
			this.hails.add(hail);
		}
		@Override public String toString() {
			return hails.toString();
		}
		public long countIntersectionsXY(double minTargetArea, double maxTargetArea) {
			long result = 0; 
			for (int i=0; i<hails.size(); i++) {
				Hail hail1 = hails.get(i);
				for (int j=i+1; j<hails.size(); j++) {
					Hail hail2 = hails.get(j);
					Pos pos1 = hail1.intersectXY(hail2);
					Pos pos2 = hail2.intersectXY(hail1);
					if ((pos1 != null) && (pos2 != null)
							&& (pos1.x>=minTargetArea) && (pos1.x<=maxTargetArea) 
							&& (pos1.y>=minTargetArea) && (pos1.y<=maxTargetArea)) {
						System.out.println(hail1+" and "+hail2+" intersect at "+pos1);
						result++;
					}
				}
			}
			return result;
		}
	}

	public static void mainPart1(String inputFile, long minTargetArea, long maxTargetArea) {
		World world = new World();
		for (InputData data:new InputProcessor(inputFile)) {
			System.out.println(data);
			world.addHail(new Hail(data.pos, data.v));
		}
		System.out.println(world);
		long cnt = world.countIntersectionsXY(minTargetArea, maxTargetArea);
		System.out.println("INTERSECTIONS X/Y: "+cnt);
	}

	
	public static void mainPart2(String inputFile, long minTargetArea, long maxTargetArea) {
	}


	public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
		System.out.println("--- PART I ---");
//		mainPart1("exercises/day24/Feri/input-example.txt", 7L, 27L);
		mainPart1("exercises/day24/Feri/input.txt", 200000000000000L, 400000000000000L);               
		System.out.println("---------------");                           
		System.out.println("--- PART II ---");
		mainPart2("exercises/day24/Feri/input-example.txt", 7L, 27L);
//		mainPart2("exercises/day24/Feri/input.txt", 200000000000000L, 400000000000000L);
		System.out.println("---------------");    
	}
	
}
