package model;
import java.awt.Point;
import java.util.ArrayList;

// Takes Trips from TripGeneration, and assigns to nearby vehicles.
public class TripAssignment {
	private ArrayList<Trip> newTrips;
	private ArrayList<Trip> waitList; // trips w/o SAV assigned
	private ArrayList<Trip> futureTrips; // for 'return' trips 
	private ArrayList<Trip> inProgress; // trips w/ SAV assigned
	private ArrayList<Trip> completed; // trips that have been completed
	private TripGeneration tripGeneration; // local reference to tripGeneration
	private Map map; // local reference to the map
	private int currentTimeStep;
	private int[][] waitListMap;
	private int[][] demandMap;
	
	public TripAssignment(TripGeneration tripGeneration, Map map) {
		this.waitList = new ArrayList<Trip>();
		this.inProgress = new ArrayList<Trip>();
		this.futureTrips = new ArrayList<Trip>();
		this.completed = new ArrayList<Trip>();
		this.newTrips = new ArrayList<Trip>();
		
		this.tripGeneration = tripGeneration;
		this.map = map;
		
		this.waitListMap = new int[40][40];
		this.demandMap = new int[40][40];
	}
	
	public ArrayList<Trip> getWaitList() {
		return this.waitList;
	}
	
	public ArrayList<Trip> getFutureTrips() {
		return this.futureTrips;
	}
	
	public ArrayList<Trip> getCompletedTrips() {
		return this.completed;
	}
	
	public void addFutureTrip(Trip newTrip) {
		if (newTrip == null) return;
		
		futureTrips.add(newTrip);
	}
	
	public void update(int timeStep) {
		currentTimeStep = timeStep;
		newTrips = tripGeneration.generateTrips(timeStep);
		for (Trip trip : newTrips) {
			waitList.add(trip);
		}
		checkFutureTrips(); // if any future trips match current time step, we start them now.
		assignTrips(); // all trips that can be serviced within 5 minutes are assigned a SAV
		updateTrips();
		updateTotalDemandMap();
	}
	
	private void checkFutureTrips() {
		ArrayList<Trip> toRemove = new ArrayList<Trip>();
		
		for (Trip trip : futureTrips) {
			if (trip.getStartTime() == this.currentTimeStep) {
				toRemove.add(trip);
				waitList.add(trip);
			}
			if (trip.getStartTime() > this.currentTimeStep) break;
		}
		
		for (Trip trip : toRemove) {
			futureTrips.remove(trip);
		}
	}
	
	private void assignTrips() {
		for (Trip trip : waitList) {
			assignTrip(trip);
		}
		for (Trip trip : inProgress) {
			if (waitList.contains(trip)) {
				waitList.remove(trip);
			}
		}
	}
	
	private void assignTrip(Trip trip) {
		Vehicle freeSAV = map.findFreeVehicle(trip.getOrigin());
		if (freeSAV != null) {
			//waitList.remove(trip);
			inProgress.add(trip);
			trip.assignSAV(freeSAV);
			freeSAV.setState(Vehicle_State.on_trip);
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
		ArrayList<Trip> toRemove = new ArrayList<Trip>();
		for (Trip trip : inProgress) {
			if (trip.isFinished()) {
//				System.out.print("Trip Complete - ");
				toRemove.add(trip);
				completed.add(trip);
				if (!trip.isReturnTrip()) { // only get return trips for "first timers"
					if (Math.random() < 0.78) generateFutureTrip(trip);
				}
			}
		}
		
		if (futureTrips.size() > 160000) reduceNumFutureTrips();
		
		for (Trip trip : toRemove) {
			inProgress.remove(trip);
		}
		
	}
	
	private void generateFutureTrip(Trip trip) {
		// We are going to generate a trip for a future time step.
		// Look at current time (this is trip finish time)
//		System.out.print("Generating Future Trip - ");
		Time_Set time = currentTimeSet();
		int futureStartTime = getFutureTripStartTime(time);
		futureTrips.add(new Trip(trip.getDestination(), trip.getOrigin(), null, futureStartTime, true));
//		System.out.println("Generated.");
	}
	
	private Time_Set currentTimeSet() {
		if (currentTimeStep % 288 < 36) return Time_Set.zero_three;
		if (currentTimeStep % 288 < 72) return Time_Set.three_six;
		if (currentTimeStep % 288 < 108) return Time_Set.six_nine;
		if (currentTimeStep % 288 < 144) return Time_Set.nine_twelve;
		if (currentTimeStep % 288 < 180) return Time_Set.twelve_fifteen;
		if (currentTimeStep % 288 < 216) return Time_Set.fifteen_eighteen;
		if (currentTimeStep % 288 < 252) return Time_Set.eighteen_twentyone;
		return Time_Set.twentyone_twentyfour;
	}
	
	private int getFutureTripStartTime(Time_Set time) {
		double change = Math.random();
		switch(time) {
		case zero_three:
			if (change < 0.82) return currentTimeStep + (int)(12 * 0.5);
			if (change < 0.85) return currentTimeStep + (int)(12 * 1.0);
			if (change < 0.92) return currentTimeStep + (int)(12 * 1.5);
			if (change < 0.94) return currentTimeStep + (int)(12 * 2.0);
			if (change < 0.97) return currentTimeStep + (int)(12 * 2.5);
			return currentTimeStep + (int)(12 * 3.0);
		case three_six:
			if (change < 0.2) return currentTimeStep + (int)(12 * 0.5);
			if (change < 0.26) return currentTimeStep + (int)(12 * 1.0);
			if (change < 0.31) return currentTimeStep + (int)(12 * 1.5);
			if (change < 0.35) return currentTimeStep + (int)(12 * 2.0);
			if (change < 0.37) return currentTimeStep + (int)(12 * 2.5);
			if (change < 0.392) return currentTimeStep + (int)(12 * 3.0);
			if (change < 0.41) return currentTimeStep + (int)(12 * 3.5);
			if (change < 0.42) return currentTimeStep + (int)(12 * 4.0);
			if (change < 0.445) return currentTimeStep + (int)(12 * 4.5);
			if (change < 0.47) return currentTimeStep + (int)(12 * 5.0);
			if (change < 0.49) return currentTimeStep + (int)(12 * 5.5);
			if (change < 0.5) return currentTimeStep + (int)(12 * 6.0);
			if (change < 0.52) return currentTimeStep + (int)(12 * 6.5);
			if (change < 0.53) return currentTimeStep + (int)(12 * 7.0);
			if (change < 0.55) return currentTimeStep + (int)(12 * 7.5);
			if (change < 0.58) return currentTimeStep + (int)(12 * 8.0);
			if (change < 0.6) return currentTimeStep + (int)(12 * 8.5);
			if (change < 0.7) return currentTimeStep + (int)(12 * 9.0);
			if (change < 0.77) return currentTimeStep + (int)(12 * 9.5);
			if (change < 0.79) return currentTimeStep + (int)(12 * 10.0);
			if (change < 0.83) return currentTimeStep + (int)(12 * 10.5);
			if (change < 0.88) return currentTimeStep + (int)(12 * 11.0);
			if (change < 0.92) return currentTimeStep + (int)(12 * 11.5);
			if (change < 0.945) return currentTimeStep + (int)(12 * 12.0);
			if (change < 0.965) return currentTimeStep + (int)(12 * 12.5);
			if (change < 0.98) return currentTimeStep + (int)(12 * 13.0);
			if (change < 0.99) return currentTimeStep + (int)(12 * 13.5);
			return currentTimeStep + (int)(12 * 14.0);
		case six_nine:
			if (change < 0.25) return currentTimeStep + (int)(12 * 0.5);
			if (change < 0.36) return currentTimeStep + (int)(12 * 1.0);
			if (change < 0.42) return currentTimeStep + (int)(12 * 1.5);
			if (change < 0.46) return currentTimeStep + (int)(12 * 2.0);
			if (change < 0.5) return currentTimeStep + (int)(12 * 2.5);
			if (change < 0.52) return currentTimeStep + (int)(12 * 3.0);
			if (change < 0.54) return currentTimeStep + (int)(12 * 3.5);
			if (change < 0.58) return currentTimeStep + (int)(12 * 4.0);
			if (change < 0.61) return currentTimeStep + (int)(12 * 4.5);
			if (change < 0.63) return currentTimeStep + (int)(12 * 5.0);
			if (change < 0.65) return currentTimeStep + (int)(12 * 5.5);
			if (change < 0.68) return currentTimeStep + (int)(12 * 6.0);
			if (change < 0.695) return currentTimeStep + (int)(12 * 6.5);
			if (change < 0.71) return currentTimeStep + (int)(12 * 7.0);
			if (change < 0.76) return currentTimeStep + (int)(12 * 7.5);
			if (change < 0.8) return currentTimeStep + (int)(12 * 8.0);
			if (change < 0.83) return currentTimeStep + (int)(12 * 8.5);
			if (change < 0.895) return currentTimeStep + (int)(12 * 9.0);
			if (change < 0.92) return currentTimeStep + (int)(12 * 9.5);
			if (change < 0.95) return currentTimeStep + (int)(12 * 10.0);
			if (change < 0.97) return currentTimeStep + (int)(12 * 10.5);
			if (change < 0.98) return currentTimeStep + (int)(12 * 11.0);
			if (change < 0.99) return currentTimeStep + (int)(12 * 11.5);
			if (change < 0.995) return currentTimeStep + (int)(12 * 12.0);
			return currentTimeStep + (int)(12 * 12.5);
		case nine_twelve:
			if (change < 0.400) return currentTimeStep + (int)(12 * 0.5);
			if (change < 0.485) return currentTimeStep + (int)(12 * 1.0);
			if (change < 0.580) return currentTimeStep + (int)(12 * 1.5);
			if (change < 0.625) return currentTimeStep + (int)(12 * 2.0);
			if (change < 0.670) return currentTimeStep + (int)(12 * 2.5);
			if (change < 0.700) return currentTimeStep + (int)(12 * 3.0);
			if (change < 0.740) return currentTimeStep + (int)(12 * 3.5);
			if (change < 0.765) return currentTimeStep + (int)(12 * 4.0);
			if (change < 0.790) return currentTimeStep + (int)(12 * 4.5);
			if (change < 0.805) return currentTimeStep + (int)(12 * 5.0);
			if (change < 0.815) return currentTimeStep + (int)(12 * 5.5);
			if (change < 0.830) return currentTimeStep + (int)(12 * 6.0);
			if (change < 0.850) return currentTimeStep + (int)(12 * 6.5);
			if (change < 0.860) return currentTimeStep + (int)(12 * 7.0);
			if (change < 0.880) return currentTimeStep + (int)(12 * 7.5);
			if (change < 0.910) return currentTimeStep + (int)(12 * 8.0);
			if (change < 0.930) return currentTimeStep + (int)(12 * 8.5);
			if (change < 0.940) return currentTimeStep + (int)(12 * 9.0);
			if (change < 0.970) return currentTimeStep + (int)(12 * 9.5);
			if (change < 0.990) return currentTimeStep + (int)(12 * 10.0);
			return currentTimeStep + (int)(12 * 10.5);
		case twelve_fifteen:
			if (change < 0.430) return currentTimeStep + (int)(12 * 0.5);
			if (change < 0.600) return currentTimeStep + (int)(12 * 1.0);
			if (change < 0.680) return currentTimeStep + (int)(12 * 1.5);
			if (change < 0.740) return currentTimeStep + (int)(12 * 2.0);
			if (change < 0.792) return currentTimeStep + (int)(12 * 2.5);
			if (change < 0.820) return currentTimeStep + (int)(12 * 3.0);
			if (change < 0.870) return currentTimeStep + (int)(12 * 3.5);
			if (change < 0.900) return currentTimeStep + (int)(12 * 4.0);
			if (change < 0.930) return currentTimeStep + (int)(12 * 4.5);
			if (change < 0.950) return currentTimeStep + (int)(12 * 5.0);
			if (change < 0.965) return currentTimeStep + (int)(12 * 5.5);
			if (change < 0.980) return currentTimeStep + (int)(12 * 6.0);
			if (change < 0.985) return currentTimeStep + (int)(12 * 6.5);
			if (change < 0.990) return currentTimeStep + (int)(12 * 7.0);
			if (change < 0.992) return currentTimeStep + (int)(12 * 7.5);
			if (change < 0.994) return currentTimeStep + (int)(12 * 8.0);
			if (change < 0.998) return currentTimeStep + (int)(12 * 8.5);
			return currentTimeStep + (int)(12 * 9.0);
		case fifteen_eighteen:
			if (change < 0.530) return currentTimeStep + (int)(12 * 0.5);
			if (change < 0.580) return currentTimeStep + (int)(12 * 1.0);
			if (change < 0.730) return currentTimeStep + (int)(12 * 1.5);
			if (change < 0.820) return currentTimeStep + (int)(12 * 2.0);
			if (change < 0.880) return currentTimeStep + (int)(12 * 2.5);
			if (change < 0.910) return currentTimeStep + (int)(12 * 3.0);
			if (change < 0.940) return currentTimeStep + (int)(12 * 3.5);
			if (change < 0.960) return currentTimeStep + (int)(12 * 4.0);
			if (change < 0.970) return currentTimeStep + (int)(12 * 4.5);
			if (change < 0.992) return currentTimeStep + (int)(12 * 5.0);
			if (change < 0.994) return currentTimeStep + (int)(12 * 5.5);
			if (change < 0.996) return currentTimeStep + (int)(12 * 6.0);
			if (change < 0.999) return currentTimeStep + (int)(12 * 6.5);
			return currentTimeStep + (int)(12 * 7.0);
		case eighteen_twentyone:
			if (change < 0.390) return currentTimeStep + (int)(12 * 0.5);
			if (change < 0.610) return currentTimeStep + (int)(12 * 1.0);
			if (change < 0.760) return currentTimeStep + (int)(12 * 1.5);
			if (change < 0.820) return currentTimeStep + (int)(12 * 2.0);
			if (change < 0.900) return currentTimeStep + (int)(12 * 2.5);
			if (change < 0.920) return currentTimeStep + (int)(12 * 3.0);
			if (change < 0.960) return currentTimeStep + (int)(12 * 3.5);
			if (change < 0.970) return currentTimeStep + (int)(12 * 4.0);
			if (change < 0.985) return currentTimeStep + (int)(12 * 4.5);
			if (change < 0.990) return currentTimeStep + (int)(12 * 5.0);
			if (change < 0.995) return currentTimeStep + (int)(12 * 5.5);
			if (change < 0.999) return currentTimeStep + (int)(12 * 6.0);
			return currentTimeStep + (int)(12 * 6.5);
		case twentyone_twentyfour:
			if (change < 0.690) return currentTimeStep + (int)(12 * 0.5);
			if (change < 0.800) return currentTimeStep + (int)(12 * 1.0);
			if (change < 0.860) return currentTimeStep + (int)(12 * 1.5);
			if (change < 0.880) return currentTimeStep + (int)(12 * 2.0);
			if (change < 0.900) return currentTimeStep + (int)(12 * 2.5);
			if (change < 0.920) return currentTimeStep + (int)(12 * 3.0);
			if (change < 0.945) return currentTimeStep + (int)(12 * 3.5);
			if (change < 0.960) return currentTimeStep + (int)(12 * 4.0);
			if (change < 0.970) return currentTimeStep + (int)(12 * 4.5);
			if (change < 0.980) return currentTimeStep + (int)(12 * 5.0);
			if (change < 0.985) return currentTimeStep + (int)(12 * 5.5);
			if (change < 0.995) return currentTimeStep + (int)(12 * 6.0);
			return currentTimeStep + (int)(12 * 6.5);
		default:
			return currentTimeStep + 1;
		}
	}
	
	private void reduceNumFutureTrips() {
		ArrayList<Trip> removeMe = new ArrayList<Trip>();
		for (Trip trip : futureTrips) {
			if (Math.random() < 0.25) {
				removeMe.add(trip);
			}
		}
		
		for (Trip trip : removeMe) {
			futureTrips.remove(trip);
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
	
	private void updateTotalDemandMap() {
		for (Trip trip : newTrips) {
			Point tripPos = trip.getOrigin();
			demandMap[tripPos.x-1][tripPos.y-1] += 1;
		}
	}
	
	public int[][] getTotalDemandMap() {
		return demandMap;
	}
	
	
}
