import java.awt.Point;
import java.util.ArrayList;

public class Map {
	// This is the map space
	
	private ArrayList<ArrayList<ArrayList<Vehicle>>> vehicleMap; // each space has a list of what cars are in it.
	private ArrayList<Vehicle> vehicles;
	static int width = 40;	// number of east/west blocks
	static int height = 40;  // number of north/south blocks
	private TripGeneration tripGeneration;
	private double[][] zoneGenerationRates;
	static int maxDistanceFromInnerCore = 10;	// max distance from outer core to inner core
	static int maxDistanceFromOuterCore = 5; 	// max distance from outer service to outer core
	// Bottom left corner is (1,1)
	
	public Map(ArrayList<Vehicle> cars, TripGeneration tripGeneration) {
		// Create a link to the simulation's trip Generation module
		this.tripGeneration = tripGeneration;
		// Initialize the Zone Generation Rate array
		this.zoneGenerationRates = new double[40][40];
		// Create a local list of all cars in the simulation
		this.vehicles = cars;
		// Initialize the Vehicle Map
		this.vehicleMap = new ArrayList<ArrayList<ArrayList<Vehicle>>>(width); // double array of vehicle lists
		for (ArrayList<ArrayList<Vehicle>> column : vehicleMap) {
			column = new ArrayList<ArrayList<Vehicle>>(height);
			for (@SuppressWarnings("unused") ArrayList<Vehicle> vehicles : column) {
				vehicles = new ArrayList<Vehicle>();
			}
		}
		// Add all vehicles from the simulation to the Vehicle Map
		for (Vehicle car : vehicles) {
			Point pos = car.getPosition();
			addVehicle(pos, car);
		}
	}

	private boolean addVehicle(Point pos, Vehicle car) {
		if (car == null) return false;
		// If this vehicle is not already found in that zone's list...
		if (!vehicleMap.get(pos.x-1).get(pos.y-1).contains(car)) {
			vehicleMap.get(pos.x-1).get(pos.y-1).add(car);	// add it to that zone's list
			return true;
		}
		// otherwise, return false to say we couldn't add to list 
		return false;
	}
	
	// Update the map w/ current vehicle position data
	private void updateVehicles() {
		// For each car in the simulation
		for (Vehicle car : this.vehicles) {
			// update that car's position in the map.
			Point prev = car.getPreviousPosition();
			Point curr = car.getPosition();
			// remove the car from its current position
			vehicleMap.get(prev.x-1).get(prev.y-1).remove(car);
			// add the car to its new position
			vehicleMap.get(curr.x-1).get(curr.y-1).add(car);
		}
	}
	
	// calculates generation rate for each zone, saves to a double array.
	public void calculateZoneGenerationRates() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				// find distance from point to next zone.
				Point pos = new Point(i+1,j+1);
				if (tripGeneration.isInnerCore(pos)) {
					zoneGenerationRates[i][j] = tripGeneration.getInnerCoreGenerationRate();
				} else if (tripGeneration.isOuterCore(pos)) {
					zoneGenerationRates[i][j] = (tripGeneration.getInnerCoreGenerationRate() + tripGeneration.getOuterCoreGenerationRate()) 
												* (1.0 - (double) distanceFromInnerCore(pos) / maxDistanceFromInnerCore);
				} else {
					zoneGenerationRates[i][j] = (tripGeneration.getOuterCoreGenerationRate() + tripGeneration.getOuterServiceGenerationRate())
												* (1.0 - (double) distanceFromOuterCore(pos) / maxDistanceFromOuterCore);
				}
			}
		}
	}
	
	// get generation rate at position
	public double getGenerationRate(Point pos) {
		return zoneGenerationRates[pos.x-1][pos.y-1];
	}
	
	// get generation rate using array indexing (0-39 instead of 1-40)
	public double getGenerationRate(int i, int j) {
		return zoneGenerationRates[i][j];
	}
	
	public double[][] getGenerationRates() {
		return this.zoneGenerationRates;
	}
	
	public int distanceFrom(Point start, Point end) {
		int distance = 0;
		distance += Math.abs(start.x-end.x);
		distance += Math.abs(start.y-end.y);
		return distance;
	}
	
	// returns -1 if inside the core (inner or outer), or distance to core (left/right/diagonal are each distance 1)
	public int distanceFromOuterCore(Point pos) {
		int distance = 0;
		
		if (!tripGeneration.isOuterServiceArea(pos)) { // if we are in the core...
			return -1;
		}
		
		if (pos.x < 11) {
			distance += Math.abs(pos.x - 11);
		} else {
			distance += Math.abs(pos.x - 30);
		}
		
		if (pos.y < 11) {
			distance += Math.abs(pos.y - 11);
		} else {
			distance += Math.abs(pos.y - 30);
		}
		
		return distance;
	}
	
	public int distanceFromInnerCore(Point pos) {
		int distance = 0;
		
		if (tripGeneration.isInnerCore(pos)) { // if we are in the inner core...
			return -1;
		}
		
		if (pos.x < 16) {	// if we are west of the inner core...
			distance += Math.abs(pos.x - 16);
		} else {			// if we are east of the inner core...
			distance += Math.abs(pos.x - 25);
		}
		
		if (pos.y < 16) {	// if we are south of the inner core...
			distance += Math.abs(pos.y - 16);
		} else {			// if we are north of the inner core...
			distance += Math.abs(pos.y - 25);
		}
		
		return distance;
	}
	
	public void update() {
		this.updateVehicles();
	}
}
