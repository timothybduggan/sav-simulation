import java.awt.Point;
import java.util.Random;

public class TripGeneration {
	// This should take care of the 7 steps (bottom of page 3)
	// Will look at current time, and 'demand' model
	private double outerServiceGenerationRate;	// ~9  trips / zone / day -- Chance of generating a trip = 9/288 trips / zone / step. Use this for P(trip generated) in outer core?
	private double outerCoreGenerationRate;		// ~27 trips / zone / day -- 27/288
	private double innerCoreGenerationRate;		// ~30 trips / zone / day -- 30/288
	private double alpha;	// before noon, alpha = 1. Afternoon, alpha = .77
	
	public TripGeneration(float osgr, float ocgr, float icgr, float alpha) {
		this.outerServiceGenerationRate = osgr;
		this.outerCoreGenerationRate = ocgr;
		this.innerCoreGenerationRate = icgr;
		this.alpha = alpha;
	}
	
	public TripGeneration() {
		this.outerServiceGenerationRate = 0.2;
		this.outerCoreGenerationRate = 0.3;
		this.innerCoreGenerationRate = 0.5;
		this.alpha = 0.5;
	}
	
	public Point generateTrip(Point pos) {
		Random generator = new Random();
		Point destination = new Point(-1,-1);
		
		while (!isInServiceArea(destination)) {
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
		
		return destination;
	}
	
	private double probabilityEast(Point pos) {
		int zonesEast = 40 - pos.x;
		int zonesWest = 40 - zonesEast - 1; 
		
		double prob = alpha * (zonesEast) / (zonesEast + zonesWest) + (1 - alpha) * 0.5;
		
		return prob;
	}
	
	private double probabilityNorth(Point pos) {
		int zonesNorth = 40 - pos.y;
		int zonesSouth = 40 - zonesNorth - 1;
		
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
		
		Point destination = new Point(start.x + distanceEast, start.y + distanceNorth);
		
		return destination;
	}
	
	private boolean isInServiceArea(Point pos) {
		if (pos.x < 1) return false;
		if (pos.x > 40) return false;
		if (pos.y < 1) return false;
		if (pos.y > 40) return false;
		
		return true;
	}
}
