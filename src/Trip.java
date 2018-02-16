import java.awt.Point;

public class Trip {
	private Point origin;
	private Point destination;
	private int waitTime; // timeSteps Waited?
	private Vehicle assignedSAV;
	private boolean passengerPickedUp;
	
	
	public Trip(Point origin, Point destination, Vehicle assignedSAV) {
		this.origin = origin;
		this.destination = destination;
		this.waitTime = 0;
		this.assignedSAV = assignedSAV;
		this.passengerPickedUp = false;
	}
	
	// returns origin point
	public Point getOrigin() {
		return this.origin;
	}
	
	// returns destination point
	public Point getDestination() {
		return this.destination;
	}
	
	// returns current user wait time
	public int getWaitTime() {
		return this.waitTime;
	}
	
	// tries to assign a SAV to the user. 
	public void assignSAV(Vehicle assignMe) {
		if (this.assignedSAV == null) {
			this.assignedSAV = assignMe;
		}
		
		System.out.println("Vehicle already assigned to this trip.");
	}
	
	public void update() {
		if (this.passengerPickedUp) {
			this.assignedSAV.moveTowardsDestination();
		} else {
			this.assignedSAV.moveTowardsDestination(); // moves towards user
			this.waitTime += 5;
		}
		
		// something like this... probably needs a bit more.
		
	}
}
