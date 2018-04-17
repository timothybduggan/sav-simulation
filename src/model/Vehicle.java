package model;
import java.awt.Point;

public class Vehicle {
	private Point position;	// current position. x = columns, y = rows (1-40);
	private Point previousPosition; // previous position (where it was last time step)
	private float milesDriven;	// total miles driven
	private float unoccupiedMiles;	// miles driven without an occupant
	private double r1Miles;
	private double r2Miles;
	private double r3Miles;
	private double r4Miles;
	private int timeSinceLastStart;	// for tracking cold starts (in num ticks)
	private int coldStarts;	// number of coldstarts for this SAV
	private int numTrips;	// how many trips has this SAV done so far?
	private Point tripOrigin;	// where is this Vehicle currently moving?
	private Point destination;
	private Vehicle_State currentState;
	private int currentTimeStep;
	
	private int timeStep = 5; // in units minutes
	
	public Vehicle(int row, int col) {
		position = new Point(row, col);
		destination = null;
		milesDriven = 0;
		unoccupiedMiles = 0;
		timeSinceLastStart = 0;
		numTrips = 0;
		timeSinceLastStart = 0;
		currentState = Vehicle_State.available;
		r1Miles = 0;
		r2Miles = 0;
		r3Miles = 0;
		r4Miles = 0;
	}
	
	public Vehicle() {
		this(1,1);
	}
	
	public Point getPosition() {
		return position;
	}
	
	public Point getPreviousPosition() {
		return previousPosition;
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
		if (this.currentState == Vehicle_State.on_trip && this.tripOrigin == null) {
			return true;
		}
		return false;
	}
	
	public int getNumTrips() {
		return numTrips;
	}
	
	public boolean inUse() {
		if (currentState == Vehicle_State.on_trip)
			return true;
		return false;
	}
	
	public void addTrip() {
		this.numTrips++;
	}
	
	public Vehicle_State getState() {
		return this.currentState;
	}
	
	public void setState(Vehicle_State state) {
		this.currentState = state;
	}
	
	public void setTripOrigin(Point origin) {
		this.tripOrigin = origin;
	}
	
	public void setDestination(Point destination) {
		this.destination = destination;
	}
	
	public void update(int timeStep) {
		this.currentTimeStep = timeStep;
		this.previousPosition = (Point) this.position.clone();
		
		if (this.destination != null) {
			this.moveTowardsDestination(this.getMaxSpeed());
		}
	}
	
	public void moveTowardsDestination() {
		this.moveTowardsDestination(this.getMaxSpeed());		
	}
	
	public int getMaxSpeed() {
		if (this.isPeakHours()) {
			return 7;
		} else {
			return 11;
		}	
	}
	
	public int distanceFrom(Point point) {
		int distance = 0;
		distance += Math.abs(point.x - position.x);
		distance += Math.abs(point.y - position.y);
		return distance;
	}
	
	public double timeFrom(Point point) {
		int distance = distanceFrom(point);
		double time = ((double) distance) / this.getMaxSpeed() * timeStep;
		return time;
	}
	
	private void moveTowardsDestination(int maxShifts) {
		//this.previousPosition = (Point) this.position.clone(); // we are about to move, so our current position becomes our previous position
		int remainingShifts = maxShifts;	// number of 1/4-mile blocks we have moved
		
		if (this.tripOrigin != null) {
			
			if (position.x < tripOrigin.x) {
				remainingShifts -= (tripOrigin.x - position.x);
				position.x = tripOrigin.x;
			} else if (position.x > tripOrigin.x) {
				remainingShifts -= (position.x - tripOrigin.x);
				position.x = tripOrigin.x;
			}
			
			if (position.y < tripOrigin.y) {
				remainingShifts -= (tripOrigin.y - position.y);
				position.y = tripOrigin.y;
			} else if (position.y > tripOrigin.y) {
				remainingShifts -= (position.y - tripOrigin.y);
				position.y = tripOrigin.y;
			}
			
			tripOrigin = null;
			unoccupiedMiles += (maxShifts - remainingShifts) / 4.0;
		}
		
		if (this.destination != null) {
			if (position.x < destination.x && remainingShifts > 0) {
				if (remainingShifts < (destination.x - position.x)) { // fewer moves than we need
					position.x = position.x + remainingShifts;
					remainingShifts = 0;
				} else {
					remainingShifts -= (destination.x - position.x);
					position.x = destination.x;
				}
			} else if (position.x > destination.x && remainingShifts > 0) {
				if (remainingShifts < (position.x - destination.x)) { // fewer moves than needed
					position.x = position.x - remainingShifts;
					remainingShifts = 0;
				} else {
					remainingShifts -= (position.x - destination.x);
					position.x = destination.x;
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
			if (this.currentState != Vehicle_State.on_trip) {
				unoccupiedMiles += (this.getMaxSpeed() - remainingShifts) / 4.0;
				switch(this.currentState) {
				case on_relocation_r1:
					r1Miles += (this.getMaxSpeed() - remainingShifts) / 4.0;
					break;
				case on_relocation_r2:
					r2Miles += (this.getMaxSpeed() - remainingShifts) / 4.0;
					break;
				case on_relocation_r3:
					r3Miles += (this.getMaxSpeed() - remainingShifts) / 4.0;
					break;
				case on_relocation_r4:
					r4Miles += (this.getMaxSpeed() - remainingShifts) / 4.0;
				default:
					break;
				}
			}
			if (this.position.equals(this.destination)) { // when we arrive, remove the destination
				this.destination = null;
			}
			
			milesDriven += (this.getMaxSpeed() - remainingShifts) / 4.0;
		}
	}
	
	private boolean isPeakHours() {
		int currentTime = currentTimeStep % 288; // step today. 0-11 : 0000-0100 , 12-23 : 0100-0200 , erc.
		if (currentTime >= 84 && currentTime < 96)
			return true;
		if (currentTime >= 192 && currentTime < 222)
			return true;
		
		return false;
	}

	public static boolean isPeakHours(int timeStep) {
		timeStep %= 288;
		
		if (timeStep >= 84  && timeStep < 96 ) return true;
		if (timeStep >= 192 && timeStep < 222) return true;
		
		return false;
	}
	
	public double getMilesR1() {
		return this.r1Miles;
	}
	
	public double getMilesR2() {
		return this.r2Miles;
	}
	
	public double getMilesR3() {
		return this.r3Miles;
	}
	
	public double getMilesR4() {
		return this.r4Miles;
	}
}
