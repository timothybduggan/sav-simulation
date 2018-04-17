package model;
import java.awt.Point;

public class Trip {
	private Point origin;
	private Point destination;
	private int startTime; // to determine when futureTrip -> waitList
	private double waitTime; // timeSteps Waited?
	private Vehicle assignedSAV;
	private boolean passengerPickedUp;
	private boolean tripComplete;
	private boolean hasSAV;
	private boolean isReturn;
	
	public Trip(Point origin, Point destination, Vehicle assignedSAV) {
		this(origin, destination, assignedSAV, 0);
	}
	
	public Trip(Point origin, Point destination, Vehicle assignedSAV, int startTime) {
		this.origin = origin;
		this.destination = destination;
		this.startTime = startTime;
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
		this.isReturn = false;
	}
	
	public Trip(Point origin, Point destination, Vehicle assignedSAV, int startTime, boolean isReturn) {
		this(origin, destination, assignedSAV, startTime);
		this.isReturn = isReturn;
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
	
	// returns start time (useful for starting 'future' trips)
	public int getStartTime() {
		return this.startTime;
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
	public boolean assignSAV(Vehicle assignMe) {
		if (this.assignedSAV == null) {
			this.assignedSAV = assignMe;
			this.assignedSAV.setState(Vehicle_State.on_trip);
			this.assignedSAV.setTripOrigin(origin);
			this.assignedSAV.setDestination(destination);
			this.hasSAV = true;
			return true;
		} else {
			System.out.println("Vehicle already assigned to this trip.");
			return false;
		}
	}
	
	public void update() {
		update(0);
	}
	
	public void update(int timeStep) {
		if (this.passengerPickedUp) {
			this.assignedSAV.update(timeStep);
			if (assignedSAV.getPosition().equals(destination)) {
				this.tripComplete = true;
				this.assignedSAV.setState(Vehicle_State.end_trip);
			}
		} else if (this.assignedSAV != null) {
			this.passengerPickedUp = true;
			this.waitTime += this.assignedSAV.timeFrom(origin);
			this.assignedSAV.update(timeStep); // moves towards user
			this.assignedSAV.addTrip();
		} else {
			this.waitTime += 5;
		}
	}
	
	public boolean isFinished() {
		return this.tripComplete;
	}
	
	public Point getCurrentPosition() {
		if (assignedSAV != null) {
			return assignedSAV.getPosition();
		}
		
		return null;
	}
	
	public String toString() {
		return origin.toString() + " -> " + destination.toString();
	}
	
	public boolean isReturnTrip() {
		return this.isReturn;
	}
}
