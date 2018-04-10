package model;
import java.util.Observable;
import java.util.Random;
import java.util.ArrayList;

// The main simulation class. Contains the different modules, and glues them
// together to run the whole sim. Crazy stuff, huh?
public class Simulation extends Observable {
	private ArrayList<Vehicle> vehicles;
	private TripAssignment tripAssignment;
	private TripGeneration tripGeneration;
	private VehicleRelocation vehicleRelocation;
	private Map map;
	private int timeStep;
	private boolean inScenario;
	private int scenarioNumber;
	
	public Simulation() {
		this(true);
	}
	
	public Simulation(boolean random) {
		this.timeStep = 0;
		initializeDefault(random);
		
		
//		printVehicleMap();
//		printGenerationRates();
	}
	
	public void updateSimulation() {
		// this is one step of the simulation.
		if (!inScenario) {
			tripAssignment.update(); // generates trips, assigns vehicles, and moves them.
			vehicleRelocation.update(); // handles relocation
			map.updateVehicleStates();
		} else {
			if (scenarioNumber == 3) {
				vehicleRelocation.applyR3();
			} else if (scenarioNumber == 4) {
				vehicleRelocation.applyR4();
			}
			map.updateVehicleStates();
		}
		timeStep++;
	}

	public String getTime() {
		String time = "";
		int day = timeStep / 288;
		int hour = (timeStep % 288) / 12;
		int minute = 5 * ((timeStep % 288) % 12);
		String ampm = "AM";
		
		if (hour >= 12) ampm = "PM";
		if (hour >= 13) hour -= 12;
		
		time = String.format("Day: %d\t %02d:%02d %s", day, hour, minute, ampm);
		return time;
	}

	public double getDistance() {
		double dist = 0;
		for (Vehicle car : vehicles) {
			dist += car.getMilesDriven();
		}
		
		return dist;
	}
	
	public double getAverageDistance() {
		double dist = this.getDistance();
		return dist / vehicles.size();
	}
	
	public double getInducedTravel() {
		double totalDist = this.getDistance();
		double unoccDist = 0;
		for (Vehicle car : vehicles) {
			unoccDist += car.getUnoccupiedMiles();
		}
		
		if (totalDist == 0) return 0;
		
		return unoccDist / totalDist * 100;
	}

	public double getAverageWaitTime() {
		double totalWait = 0;
		int totalTrips = tripAssignment.getCompletedTrips().size();
		if (totalTrips == 0) return 0;
		
		for (Trip trip : tripAssignment.getCompletedTrips()) {
			totalWait += trip.getWaitTime();
		}
		
		return totalWait / totalTrips;
	}
	
	public int getNumVehicles() {
		return vehicles.size();
	}
	
	public void initializeScenario(int scenario) {
		inScenario = true;
		timeStep = 0;
		scenarioNumber = scenario;
		initializeVehicles(scenario);
		initializeTripGeneration();
		initializeTripAssignment();
		initializeVehicleRelocation();
		
	}
	
	public void initializeDefault(boolean random) {
		timeStep = 0;
		inScenario = false;
		initializeVehicles(random);
		initializeTripGeneration();
		initializeTripAssignment();
		initializeVehicleRelocation();
	}
	
	private void initializeVehicles(boolean random) {
		this.vehicles = new ArrayList<Vehicle>(1601);
//		Random gen;
//		if (random) {
//			gen = new Random();
//		} else {
//			gen = new Random(10);
//		}
		
		for (int i = 1; i < 41; i++) {
			for (int j = 1; j < 41; j++) {
//				if (gen.nextDouble() > 0.1) {
					Vehicle addMe = new Vehicle(i,j);
					this.vehicles.add(addMe);
//				}
			}
		}
	}
	
	private void initializeVehicles(int scenario) {
		switch(scenario) {
		
		case 1:	// R1 Demonstration
			break;
		case 2: // R2 Demonstration
			break;
		case 3: // R3 Demonstration'
			vehicles.clear();
			for (int i = 0; i < 10; i++) {
				vehicles.add(new Vehicle(10,10));
			}
			for (int i = 0; i < 10; i++) {
				vehicles.add(new Vehicle(30,10));
			}
			for (int i = 0; i < 10; i++) {
				vehicles.add(new Vehicle(10,30));
			}
			for (int i = 0; i < 10; i++) {
				vehicles.add(new Vehicle(30,30));
			}
			break;
		case 4: // R4 Demonstration
			vehicles.clear();
			for (int i = 0; i < 25; i++) {
				vehicles.add(new Vehicle(20,20));
			}
			break;
		default: 
			break;
		
		}
	}
	
	private void initializeTripGeneration() {
		this.tripGeneration = new TripGeneration();
		this.map = new Map(this.vehicles, this.tripGeneration);
		map.calculateZoneGenerationRates();
		this.tripGeneration.setMap(this.map);
	}
	
	
	
	private void initializeTripAssignment() {
		tripAssignment = new TripAssignment(tripGeneration, map);
	}
	
	private void initializeVehicleRelocation() {
		vehicleRelocation = new VehicleRelocation(tripAssignment, tripGeneration, map, vehicles);
	}
	
	public TripAssignment getTripAssignment() {
		return this.tripAssignment;
	}
	
	public TripGeneration getTripGeneration() {
		return this.tripGeneration;
	}
	
	public VehicleRelocation getVehicleRelocation() {
		return this.vehicleRelocation;
	}
	
	public int[][] getVehicleCount() {
		return map.getNumVehicles();
	}
	
	// return generation rates * 1000
	public int[][] getGenerationRates() {
		double[][] gr = map.getGenerationRates();
		int[][] retval = new int[gr.length][gr[0].length];
		
		for (int i = 0; i < gr.length; i++) {
			for (int j = 0; j < gr[i].length; j++) {
				retval[i][j] = (int) (gr[i][j] * 1000);
			}
		}
		
		return retval;
	}
	
	public int[][] getEstimatedDemand() {
		double[][] dm = vehicleRelocation.getEstimatedDemandR1();
		int[][] retval = new int[dm.length][dm[0].length];
		
		for (int i = 0; i < dm.length; i++) {
			
		}
		
		return retval;
	}
	
	private void printVehicleMap() {

		int[][] numVehicles = map.getNumVehicles();
		
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				System.out.print(numVehicles[i][j] + " , ");
			}
			System.out.println();
		}
	}
	
	private void printGenerationRates() {
		
		double[][] genRates = map.getGenerationRates();
		double total = 0;
		
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				System.out.printf("%4.2f,", genRates[i][j]);
				total += genRates[i][j];
			}
			System.out.println();
		}
		System.out.println("Total: " + total);
	}

	public int[][] getWaitListMap() {
		return tripAssignment.getWaitListMap();
	}
	
	public int[][] getTotalTripRequests() {
		return tripAssignment.getTotalDemandMap();
	}

	public boolean isPeak() {
		return Vehicle.isPeakHours(timeStep);
	}
}
