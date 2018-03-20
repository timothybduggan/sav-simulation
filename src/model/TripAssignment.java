package model;
import java.awt.Point;
import java.util.ArrayList;

// Takes Trips from TripGeneration, and assigns to nearby vehicles.
public class TripAssignment {
	private ArrayList<Trip> waitList; // trips w/o SAV assigned
	private ArrayList<Trip> futureTrips; // for 'return' trips 
	private ArrayList<Trip> inProgress; // trips w/ SAV assigned
	private ArrayList<Trip> completed; // trips that have been completed
	private TripGeneration tripGeneration; // local reference to tripGeneration
	private Map map; // local reference to the map
	private int currentTimeStep;
	private int[][] waitListMap;
	
	
	public TripAssignment(TripGeneration tripGeneration, Map map) {
		this.waitList = new ArrayList<Trip>();
		this.inProgress = new ArrayList<Trip>();
		this.futureTrips = new ArrayList<Trip>();
		this.completed = new ArrayList<Trip>();
		
		this.tripGeneration = tripGeneration;
		this.map = map;
		
		this.waitListMap = new int[40][40];
	}
	
	public ArrayList<Trip> getWaitList() {
		return this.waitList;
	}
	
	public void update() {
		ArrayList<Trip> newTrips = tripGeneration.generateTrips();
		for (Trip trip : newTrips) {
			waitList.add(trip);
		}
		checkFutureTrips(); // if any future trips match current time step, we start them now.
		assignTrips(); // all trips that can be serviced within 5 minutes are assigned a SAV
		updateTrips();
		currentTimeStep++;
	}
	
	private void checkFutureTrips() {
		for (Trip trip : futureTrips) {
			if (trip.getStartTime() == this.currentTimeStep) {
				futureTrips.remove(trip);
				waitList.add(trip);
			}
		}
	}
	
	private void assignTrips() {
		for (Trip trip : waitList) {
			assignTrip(trip);
		}
	}
	
	private void assignTrip(Trip trip) {
		Vehicle freeSAV = map.findFreeVehicle(trip.getOrigin());
		if (freeSAV != null) {
			waitList.remove(trip);
			inProgress.add(trip);
		}
	}
	
	private void updateTrips() {
		
		// if in waitlist, waitTime += 5
		for (Trip trip : waitList) {
			trip.update();
		}
		// if SAV assigned, progress through trip
		for (Trip trip : inProgress) {
			trip.update();
		}
		// check if any trips have finished
		checkForCompletedTrips();
		updateWaitListMap();
	}
	
	private void checkForCompletedTrips() {
		// for all trips in inProgress, if we've finished move it
		for (Trip trip : inProgress) {
			if (trip.isFinished()) {
				inProgress.remove(trip);
				completed.add(trip);
			}
		}
	}
	
	private void updateWaitListMap() {
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				waitListMap[i][j] = 0;
			}
		}
		
		for (Trip trip : waitList) {
			Point tripPos = trip.getOrigin();
			waitListMap[tripPos.x-1][tripPos.y-1] += 1;
		}
	}
	
	public int[][] getWaitListMap() {
		return this.waitListMap;
	}
}
