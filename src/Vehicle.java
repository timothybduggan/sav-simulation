import java.awt.Point;

public class Vehicle {
	private Point position;	// current position. x = columns, y = rows (1-40);
	private float milesDriven;	// total miles driven
	private float unoccupiedMiles;	// miles driven without an occupant
	private int timeSinceLastStart;	// for tracking cold starts (in num ticks)
	private int coldStarts;	// number of coldstarts for this SAV
	private boolean occupied; // is there an occupant?
	private boolean onTrip;	// is this vehicle driving a person / to pick someone up?
	private int numTrips;	// how many trips has this SAV done so far?
	private Point destination;	// where is this Vehicle currently moving?
	
	private int timeStep = 5; // in units minutes
	
	public Vehicle(int row, int col) {
		position = new Point(col, row);
		destination = new Point();
		milesDriven = 0;
		unoccupiedMiles = 0;
		timeSinceLastStart = 0;
		onTrip = false;
		occupied = false;
		numTrips = 0;
	}
	
	public Vehicle() {
		this(1,1);
	}
	
	public Point getPosition() {
		return position;
	}
	
	public float getMilesDriven() {
		return milesDriven;
	}
	
	public float getUnoccupiedMiles() {
		return unoccupiedMiles;
	}
	
	public int getColdStarts() {
		return coldStarts;
	}
	
	public boolean isOccupied() {
		return occupied;
	}
	
	public int getNumTrips() {
		return numTrips;
	}
	
	public boolean inUse() {
		return onTrip;
	}
	
	public void setDestination(Point destination) {
		this.destination = destination;
	}
	
	public void moveTowardsDestination() {
		int maxShifts = 2*timeStep; // number of 1/4-mile blocks we can move
		int remainingShifts = maxShifts;	// number of 1/4-mile blocks we have moved
		
		if (position.x < destination.x) { // if we are west of the destination...
			remainingShifts -= (destination.x - position.x);
			if (remainingShifts >= 0) {
				position.x = destination.x;
			}
			else {
				position.x = position.x + maxShifts;
			}
		} else if (position.x > destination.x) {
			remainingShifts -= (position.x - destination.x);
			if (remainingShifts >= 0) {
				position.x = destination.x;
			}
			else {
				position.x = position.x - maxShifts;
			}
		}
		
		if (position.y < destination.y && remainingShifts > 0) {
			if (remainingShifts < (destination.y - position.y)) { // fewer moves than we need
				position.y = position.y + remainingShifts;
				remainingShifts = 0;
			} else {
				remainingShifts -= (destination.y - position.y);
				position.y = destination.y;
			}
		} else if (position.y > destination.y && remainingShifts > 0) {
			if (remainingShifts < (position.y - destination.y)) { // fewer moves than needed
				position.y = position.y - remainingShifts;
				remainingShifts = 0;
			} else {
				remainingShifts -= (position.y - destination.y);
				position.y = destination.y;
			}
		}
		
		if (remainingShifts > 0) { // steps remain...
			// we've made it early, we can start moving to secondaryDestination? (might be dumb)
		}
	}
	
}
