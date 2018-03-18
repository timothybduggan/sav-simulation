package model;
import java.util.ArrayList;

// Takes Trips from TripGeneration, and assigns to nearby vehicles.
public class TripAssignment {
	private ArrayList<Trip> waitList;
	private ArrayList<Trip> completed;
	
	public TripAssignment() {
		this.waitList = new ArrayList<Trip>();
		this.completed = new ArrayList<Trip>();
		
	}
	public ArrayList<Trip> getWaitList() {
		return this.waitList;
	}
}
