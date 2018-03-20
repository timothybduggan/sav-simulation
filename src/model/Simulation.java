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
	
	public Simulation() {
		this(true);
	}
	
	public Simulation(boolean random) {
		this.timeStep = 0;
		initializeVehicles(random);
		initializeTripGeneration();
		initializeTripAssignment();
		initializeVehicleRelocation();
		
//		printVehicleMap();
//		printGenerationRates();
	}
	
	public void updateSimulation() {
		tripAssignment.update();
		map.updateVehicleStates();
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
	
	private void update() {
		// this is one step of the simulation.
		tripAssignment.update(); // generates trips, assigns vehicles, and moves them.
		vehicleRelocation.update(); // handles relocation
		updateVehicleStatus();
		timeStep++;
	}
	
	private void updateVehicleStatus() {
		
		for (Vehicle car : vehicles) {
			switch(car.getState()) {
			case available: // these states remain through time steps
			case on_trip:
				break;
			case end_trip:	// these vehicles become available next step
			case on_relocation:
				car.setState(Vehicle_State.available);
				break;
			default:
				throw new IllegalArgumentException("Vehicle State not recognized");
			}
		}
	}
	
	public int[][] getWaitListMap() {
		return tripAssignment.getWaitListMap();
	}
	
	public int[][] getTotalTripRequests() {
		return tripAssignment.getNewDemandMap();
	}
}
