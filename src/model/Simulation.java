package model;
import java.util.Observable;
import java.util.ArrayList;

// The main simulation class. Contains the different modules, and glues them
// together to run the whole sim. Crazy stuff, huh?
public class Simulation extends Observable {
	private ArrayList<Vehicle> vehicles;
	private TripAssignment tripAssignment;
	private TripGeneration tripGeneration;
	private VehicleRelocation vehicleRelocation;
	private Map map;
	
	public Simulation() {
		initializeVehicles();
		initializeTripGeneration();
		
		int[][] numVehicles = map.getNumVehicles();
		
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				System.out.print(numVehicles[i][j] + " , ");
			}
			System.out.println();
		}
	}
	
	
	private void initializeVehicles() {
		this.vehicles = new ArrayList<Vehicle>(1601);
		
		for (int i = 1; i < 41; i++) {
			for (int j = 1; j < 41; j++) {
				Vehicle addMe = new Vehicle(i,j);
				this.vehicles.add(addMe);
			}
		}
	}
	
	private void initializeTripGeneration() {
		this.tripGeneration = new TripGeneration();
		this.map = new Map(this.vehicles, this.tripGeneration);
		this.tripGeneration.setMap(this.map);
	}
	
	public int[][] getVehicleCount() {
		return map.getNumVehicles();
	}
}
