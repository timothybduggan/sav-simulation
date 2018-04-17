package model;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class TripGeneration {
	// This should take care of the 7 steps (bottom of page 3)
	// Will look at current time, and 'demand' model
	private double outerServiceGenerationRate;	// ~9  trips / zone / day -- Chance of generating a trip = 9/288 trips / zone / step. Use this for P(trip generated) in outer core?
	private double outerCoreGenerationRate;		// ~27 trips / zone / day -- 27/288
	private double innerCoreGenerationRate;		// ~30 trips / zone / day -- 30/288
	private double alpha;	// before noon, alpha = 1. Afternoon, alpha = .77
	private int height = 40;
	private int width = 40;
	//private double[][] generationRates; // instead, get this from the map.
	private Map map;
	private int currentTimeStep;
	
	public TripGeneration(double osgr, double ocgr, double icgr, double alpha) {
		this.outerServiceGenerationRate = osgr / 288.0;
		this.outerCoreGenerationRate = ocgr / 288.0;
		this.innerCoreGenerationRate = icgr / 288.0;
		this.alpha = alpha;
		this.map = new Map(null, this);
		this.map.calculateZoneGenerationRates();
		this.currentTimeStep = 0;
	}
	
	public TripGeneration() {
		this(9.0, 27.0, 30.0, 0.99);
	}
	
	public Map getMap() {
		return this.map;
	}
	
	public void setMap(Map map) {
		this.map = map;
		this.map.calculateZoneGenerationRates();
	}
	
	public double getOuterServiceGenerationRate() {
		return this.outerServiceGenerationRate;
	}
	
	public double getOuterCoreGenerationRate() {
		return this.outerCoreGenerationRate;
	}
	
	public double getInnerCoreGenerationRate() {
		return this.innerCoreGenerationRate;
	}
	
	public Trip generateTrip(Point pos) {
		return this.generateTrip(pos, currentTimeStep);
	}
	
	public Trip generateTrip(Point pos, int timeStep) {
		Random generator = new Random();
		Point destination = new Point(-1,-1);
		
		while (!isInServiceArea(destination)) {
			// Change this to use fig. 1
			int distance = sampleTripDistance(); // 1. Sample Trip Distance (1-15 miles / 4-60 quarter-miles) [should change based on fig. 1 (data from http://nhts.ornl.gov/2009/pub/stt.pdf)]
			int i = 0;
				while (!isInServiceArea(destination)) {	// 5. If Destination not in service area, return to 2.
					i++;
					double probEast = probabilityEast(pos);								// 2. Sample E-W Direction
					boolean goEast = (generator.nextDouble() < probEast);			
					double probNorth = probabilityNorth(pos);							// 3. Sample N-S Direction
					boolean goNorth = (generator.nextDouble() < probNorth);
					destination = chooseDestination(pos, goEast, goNorth, distance);	// 4. Select Destination
//					System.out.println("Distance: "+distance+"\tGoNorth: "+goNorth+"\tGoEast: "+goEast);
//					System.out.println("Generated destination: "+destination.toString()+" from origin: "+pos.toString());
					
					if (i >= 20) break;	// 6. If 20 destinations have been outside the service area, return to 1.
				}
		}	// This loop keeps us going until we find a valid destination.
		
		return new Trip(pos, destination, null, timeStep);
	}
	
	private int sampleTripDistance() {
		double random = Math.random();
		if (random < .036) return 4;
		else if (random < .0745) return 5;
		else if (random < .1155) return 6;
		else if (random < .1585) return 7;
		else if (random < .2035) return 8;
		else if (random < .2465) return 9;
		else if (random < .2875) return 10;
		else if (random < .3255) return 11;
		else if (random < .3625) return 12;
		else if (random < .3965) return 13;
		else if (random < .4285) return 14;
		else if (random < .4570) return 15;
		else if (random < .4845) return 16;
		else if (random < .5120) return 17;
		else if (random < .5395) return 18;
		else if (random < .5670) return 19;
		else if (random < .5945) return 20;
		else if (random < .6185) return 21;
		else if (random < .6405) return 22;
		else if (random < .6604) return 23;
		else if (random < .6779) return 24;
		else if (random < .6944) return 25;
		else if (random < .7104) return 26;
		else if (random < .7259) return 27;
		else if (random < .7409) return 28;
		else if (random < .7554) return 29;
		else if (random < .7694) return 30;
		else if (random < .7829) return 31;
		else if (random < .7959) return 32;
		else if (random < .8084) return 33;
		else if (random < .8204) return 34;
		else if (random < .8304) return 35;
		else if (random < .8394) return 36;
		else if (random < .8486) return 37;
		else if (random < .8580) return 38;
		else if (random < .8676) return 39;
		else if (random < .8774) return 40;
		else if (random < .8864) return 41;
		else if (random < .8949) return 42;
		else if (random < .9031) return 43;
		else if (random < .9101) return 44;
		else if (random < .9173) return 45;
		else if (random < .9247) return 46;
		else if (random < .9323) return 47;
		else if (random < .9401) return 48;
		else if (random < .9470) return 49;
		else if (random < .9525) return 50;
		else if (random < .9590) return 51;
		else if (random < .9639) return 52;
		else if (random < .9687) return 53;
		else if (random < .97345) return 54;
		else if (random < .97795) return 55;
		else if (random < .98195) return 56;
		else if (random < .98695) return 57;
		else if (random < .99295) return 58;
		else if (random < .9995) return 59;
		else  return 60;
	}
	
	private double probabilityEast(Point pos) {
		int zonesEast = width - pos.x;
		int zonesWest = width - zonesEast - 1; 
		
		double prob = alpha * (zonesEast) / (zonesEast + zonesWest) + (1.0 - alpha) * 0.5;
		
		return prob;
	}
	
	private double probabilityNorth(Point pos) {
		int zonesNorth = height - pos.y;
		int zonesSouth = height - zonesNorth - 1;
		
		double prob = alpha * (zonesNorth) / (zonesNorth + zonesSouth) + (1.0 - alpha) * 0.5;
		
		return prob;
	}
	
	private Point chooseDestination(Point start, boolean goEast, boolean goNorth, int distance) {
		double probDestination = 1.0 / (distance + 1); // each destination has an even chance of being selected.
		Random generator = new Random();
		int selection = 1;
		for (int i = 0; i < 100; i++) {
			selection = (int) Math.ceil(generator.nextDouble() / probDestination);
//			System.out.println(selection+"\t\t"+probDestination);
		}
		int distanceNorth = distance - (selection - 1);
		if (!goNorth) {	// if we aren't going north, we should go south.
			distanceNorth *= -1;// (going south is going in the negative y)
		}
		int distanceEast = selection - 1;
		if (!goEast) {	// if we aren't going east, we should go west.
			distanceEast *= -1;	// (going west is going in the negative x)
		}
		
		Point destination = new Point(start.x + distanceNorth, start.y + distanceEast);
		
		return destination;
	}
	
	private boolean isInServiceArea(Point pos) {
		if (pos.x < 1) return false;
		if (pos.x > width) return false;
		if (pos.y < 1) return false;
		if (pos.y > height) return false;
		
		return true;
	}

	public boolean isInnerCore(Point pos) {
		// if (within 2.5 miles of center), true
		if (pos.x > 15 && pos.x < 26) {
			if (pos.y > 15 && pos.y < 26) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isOuterCore(Point pos) {
		if (!isInnerCore(pos)) {
			if (pos.x > 10 && pos.x < 31) {
				if (pos.y > 10 && pos.y < 31) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isOuterServiceArea(Point pos) {
		if (!isInnerCore(pos) && !isOuterCore(pos)) return true; // if we are not in the core, we are in the outer service area
		return false;
	}
	
	public ArrayList<Trip> generateTrips(int timeStep) {
		ArrayList<Trip> newTrips = new ArrayList<Trip>();	// Arraylist of trips generated this call.
		Random generator = new Random();					// Generator for getting numbers in [0,1)
		for (int i = 0; i < width; i++) {		// for each zone...
			for (int j = 0; j < height; j++) {
				if (generator.nextDouble() < map.getGenerationRate(i,j)) { 	// if we get a number < generation rate for that zone...
					newTrips.add(this.generateTrip(new Point(i+1,j+1), timeStep));	// generate a new trip from that point!
				}
			}
		}
		// return the list of new trips!
		
		return newTrips;
	}
}
