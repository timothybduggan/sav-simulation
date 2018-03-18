package model;
import java.awt.Point;

public class Trip {
	private Point origin;
	private Point destination;
	private double waitTime; // timeSteps Waited?
	private Vehicle assignedSAV;
	private boolean passengerPickedUp;
	private boolean tripComplete;
	private boolean hasSAV;
	
	
	public Trip(Point origin, Point destination, Vehicle assignedSAV) {
		this.origin = origin;
		this.destination = destination;
		this.waitTime = 0;
		this.hasSAV = false;
		this.assignedSAV = assignedSAV;
		if (this.assignedSAV != null) {
			this.assignedSAV.setState(Vehicle_State.on_trip);
			this.assignedSAV.setTripOrigin(origin);
			this.assignedSAV.setDestination(destination);
			this.hasSAV = true;
		}
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
	public double getWaitTime() {
		return this.waitTime;
	}
	
	// returns true if an SAV is assigned to this trip
	public boolean hasAssignedSAV() {
		return this.hasSAV;
	}
	
	// returns true if passenger has been picked up 
	public boolean isPickedUp() {
		return this.passengerPickedUp;
	}
	
	// tries to assign a SAV to the user. 
	public void assignSAV(Vehicle assignMe) {
		if (this.assignedSAV == null) {
			this.assignedSAV = assignMe;
			this.assignedSAV.setState(Vehicle_State.on_trip);
			this.assignedSAV.setTripOrigin(origin);
			this.assignedSAV.setDestination(destination);
			this.hasSAV = true;
		} else {
			System.out.println("Vehicle already assigned to this trip.");
		}
	}
	
	public void update() {
		if (this.passengerPickedUp) {
			this.assignedSAV.moveTowardsDestination();
			if (assignedSAV.getPosition().equals(destination)) {
				this.tripComplete = true;
				this.assignedSAV.setState(Vehicle_State.end_trip);
			}
		} else if (this.assignedSAV != null) {
			this.passengerPickedUp = true;
			this.waitTime += this.assignedSAV.timeFrom(origin);
			this.assignedSAV.moveTowardsDestination(); // moves towards user
			this.assignedSAV.addTrip();
		} else {
			this.waitTime += 5;
		}
	}
	
	public boolean isFinished() {
		return this.tripComplete;
	}
}
