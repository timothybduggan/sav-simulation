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
		this(9.0, 27.0, 30.0, 0.5);
	}
	
	public Map getMap() {
		return this.map;
	}
	
	public void setMap(Map map) {
		this.map = map;
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
		Random generator = new Random();
		Point destination = new Point(-1,-1);
		
		while (!isInServiceArea(destination)) {
			// Change this to use fig. 1
			int distance = generator.nextInt(57)+4; // 1. Sample Trip Distance (1-15 miles / 4-60 quarter-miles) [should change based on fig. 1 (data from http://nhts.ornl.gov/2009/pub/stt.pdf)]
			int i = 0;
				while (!isInServiceArea(destination)) {	// 5. If Destination not in service area, return to 2.
					i++;
					double probEast = probabilityEast(pos);								// 2. Sample E-W Direction
					boolean goEast = (generator.nextDouble() < probEast);			
					double probNorth = probabilityNorth(pos);							// 3. Sample N-S Direction
					boolean goNorth = (generator.nextDouble() < probNorth);
					destination = chooseDestination(pos, goEast, goNorth, distance);	// 4. Select Destination
					System.out.println("Distance: "+distance+"\tGoNorth: "+goNorth+"\tGoEast: "+goEast);
					System.out.println("Generated destination: "+destination.toString()+" from origin: "+pos.toString());
					
					if (i >= 20) break;	// 6. If 20 destinations have been outside the service area, return to 1.
				}
		}	// This loop keeps us going until we find a valid destination.
		
		return new Trip(pos, destination, null, currentTimeStep);
	}
	
	private double probabilityEast(Point pos) {
		int zonesEast = width - pos.x;
		int zonesWest = width - zonesEast - 1; 
		
		double prob = alpha * (zonesEast) / (zonesEast + zonesWest) + (1 - alpha) * 0.5;
		
		return prob;
	}
	
	private double probabilityNorth(Point pos) {
		int zonesNorth = height - pos.y;
		int zonesSouth = height - zonesNorth - 1;
		
		double prob = alpha * (zonesNorth) / (zonesNorth + zonesSouth) + (1 - alpha) * 0.5;
		
		return prob;
	}
	
	private Point chooseDestination(Point start, boolean goEast, boolean goNorth, int distance) {
		double probDestination = 1.0 / (distance + 1); // each destination has an even chance of being selected.
		Random generator = new Random();
		int selection = 1;
		for (int i = 0; i < 100; i++) {
			selection = (int) Math.ceil(generator.nextDouble() / probDestination);
			System.out.println(selection+"\t\t"+probDestination);
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
	
	public ArrayList<Trip> generateTrips() {
		ArrayList<Trip> newTrips = new ArrayList<Trip>();	// Arraylist of trips generated this call.
		Random generator = new Random();					// Generator for getting numbers in [0,1)
		for (int i = 0; i < width; i++) {		// for each zone...
			for (int j = 0; j < height; j++) {
				if (generator.nextDouble() < map.getGenerationRate(i,j)) { 	// if we get a number < generation rate for that zone...
					newTrips.add(this.generateTrip(new Point(i+1,j+1)));	// generate a new trip from that point!
				}
			}
		}
		// return the list of new trips!
		
		currentTimeStep++;
		return newTrips;
	}
}