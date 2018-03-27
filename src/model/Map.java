package model;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class Map {
	// This is the map space
	
	private ArrayList<ArrayList<ArrayList<Vehicle>>> vehicleMap; // each space has a list of what cars are in it.
	private int[][] numVehicles;
	private int[][] numFreeVehicles;
	private int[][] numAvailable;
	private ArrayList<Vehicle> vehicles;
	static int width = 40;	// number of east/west blocks
	static int height = 40;  // number of north/south blocks
	private TripGeneration tripGeneration;
	private double[][] zoneGenerationRates;
	static int maxDistanceFromInnerCore = 10;	// max distance from outer core to inner core
	static int maxDistanceFromOuterCore = 20; 	// max distance from outer service to outer core
	// Bottom left corner is (1,1)
	private int currentTimeStep;
	
	public Map(ArrayList<Vehicle> cars, TripGeneration tripGeneration) {
		// Create a link to the simulation's trip Generation module
		this.tripGeneration = tripGeneration;
		// Initialize the Zone Generation Rate array
		this.zoneGenerationRates = new double[40][40];
		// Create a local list of all cars in the simulation
		this.vehicles = cars;
		// Initialize the Vehicle Map
		this.vehicleMap = new ArrayList<ArrayList<ArrayList<Vehicle>>>(width); // double array of vehicle lists
		for (int i = 0; i < 40; i++) {
			vehicleMap.add(new ArrayList<ArrayList<Vehicle>>());
		}
		for (ArrayList<ArrayList<Vehicle>> column : vehicleMap) {
			for (int i = 0; i < 40; i++) {
				column.add(new ArrayList<Vehicle>());
			}
		}
		// Add all vehicles from the simulation to the Vehicle Map
		if (vehicles == null) return;
		
		for (Vehicle car : vehicles) {
			addVehicle(car);
		}
		
		this.numVehicles = new int[40][40];
		this.numFreeVehicles = new int[40][40];
		this.numAvailable = new int[40][40];
		
		countVehicles();
		countFreeVehicles();
	}

	private void countVehicles() {
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				numVehicles[i][j] = vehicleMap.get(i).get(j).size();
			}
		}
	}
	
	private void countFreeVehicles() {
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				numFreeVehicles[i][j] = 0;
				for (Vehicle car : vehicleMap.get(i).get(j)) {
					if (car.getState() != Vehicle_State.on_trip) {
						numFreeVehicles[i][j] += 1; 
					}
				}
			}
		}
	}
	
	private void countAvailableVehicles() {
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				numFreeVehicles[i][j] = 0;
				for (Vehicle car : vehicleMap.get(i).get(j)) {
					if (car.getState() == Vehicle_State.available) {
						numAvailable[i][j] += 1; 
					}
				}
			}
		}
	}
	
	public int[][] getNumVehicles() {
		this.countVehicles();
		return this.numVehicles;
	}
	
	public int[][] getNumFreeVehicles() {
		this.countFreeVehicles();
		return this.numFreeVehicles;
	}
	
	public int[][] getAvailableVehicles() {
		this.countAvailableVehicles();
		return this.numAvailable;
	}
	
	public boolean addVehicle(Vehicle car) {
		if (car == null) return false;
		Point pos = car.getPosition();
		// If this vehicle is not already found in that zone's list...
		if (vehicleMap.get(pos.x-1).get(pos.y-1).size() > 0) {
			if (!vehicleMap.get(pos.x-1).get(pos.y-1).contains(car)) {
				vehicleMap.get(pos.x-1).get(pos.y-1).add(car);	// add it to that zone's list
				return true;
			}
			return false;
		} else {
			vehicleMap.get(pos.x-1).get(pos.y-1).add(car);
			return true;
		}
		
	}
	
	// Update the map w/ current vehicle position data
	public void updateVehicles() {
		// For each car in the simulation
		resetVehicleMap();
		
		for (Vehicle car : this.vehicles) {
			// update that car's position in the map.
			Point curr = car.getPosition();
			vehicleMap.get(curr.x-1).get(curr.y-1).add(car);
		}
	}
	
	public void moveVehicle(Vehicle vehicle, Point dest) {
		Point pos = vehicle.getPosition();
		
		vehicleMap.get(pos.x-1).get(pos.y-1).remove(vehicle);
		vehicleMap.get(dest.x-1).get(dest.y-1).add(vehicle);
	}
	
	private void resetVehicleMap() {
		for (ArrayList<ArrayList<Vehicle>> column : vehicleMap) {
			for (ArrayList<Vehicle> point : column) {
				point.clear();
			}
		}
	}
	
	// calculates generation rate for each zone, saves to a double array.
	public void calculateZoneGenerationRates() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				// find distance from point to next zone. [These equations are wrong]
				Point pos = new Point(i+1,j+1);
				if (tripGeneration.isInnerCore(pos)) {
					zoneGenerationRates[i][j] = tripGeneration.getInnerCoreGenerationRate();
				} else if (tripGeneration.isOuterCore(pos)) {
					zoneGenerationRates[i][j] = (tripGeneration.getInnerCoreGenerationRate() - tripGeneration.getOuterCoreGenerationRate()) 
												* (1.0 - (double) distanceFromInnerCore(pos) / maxDistanceFromInnerCore)
												+ tripGeneration.getOuterCoreGenerationRate();
//					zoneGenerationRates[i][j] = tripGeneration.getOuterCoreGenerationRate();
				} else {
					zoneGenerationRates[i][j] = (tripGeneration.getOuterCoreGenerationRate() - tripGeneration.getOuterServiceGenerationRate())
												* (1.0 - (double) distanceFromOuterCore(pos) / maxDistanceFromOuterCore)
												+ tripGeneration.getOuterServiceGenerationRate();
//					zoneGenerationRates[i][j] = tripGeneration.getOuterServiceGenerationRate();
				}
//				System.out.println(zoneGenerationRates[i][j]);
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
		} else if (pos.x > 30) {
			distance += Math.abs(pos.x - 30);
		}
		
		if (pos.y < 11) {
			distance += Math.abs(pos.y - 11);
		} else if (pos.y > 30) {
			distance += Math.abs(pos.y - 30);
		}
		
//		System.out.println(pos + " is " + distance + " from outer core");
		
		return distance;
	}
	
	public int distanceFromInnerCore(Point pos) {
		int distance = 0;
		
		if (tripGeneration.isInnerCore(pos)) { // if we are in the inner core...
			return -1;
		}
		
		if (pos.x < 16) {	// if we are west of the inner core...
			distance += Math.abs(pos.x - 16);
		} else if (pos.x > 25) {			// if we are east of the inner core...
			distance += Math.abs(pos.x - 25);
		}
		
		if (pos.y < 16) {	// if we are south of the inner core...
			distance += Math.abs(pos.y - 16);
		} else if (pos.y > 25) {			// if we are north of the inner core...
			distance += Math.abs(pos.y - 25);
		}
		
//		System.out.println(pos + " is " + distance + " from inner core");
		
		return distance;
	}
	
	public int getMaxSpeed() {
		if (this.isPeakHours()) {
			return 7;
		} else {
			return 11;
		}	
	}
	
	private boolean isPeakHours() {
		int currentTime = currentTimeStep % 288; // step today. 0-11 : 0000-0100 , 12-23 : 0100-0200 , erc.
		if (currentTime >= 84 && currentTime < 96)
			return true;
		if (currentTime >= 192 && currentTime < 222)
			return true;
		
		return false;
	}
	
	public void update() {
		this.updateVehicles();
		this.currentTimeStep++;
	}
	
	public int getCurrentTimeStep() {
		return this.currentTimeStep;
	}
	
	public ArrayList<Vehicle> getVehicleList(Point pos) {
		return vehicleMap.get(pos.x-1).get(pos.y-1);
	}
	
	public int freeVehiclesAt(Point pos) {
		ArrayList<Vehicle> list = this.getVehicleList(pos);
		int numFreeVehicles = 0;
		
		for (Vehicle vehicle : list) {
			if (vehicle.getState() != Vehicle_State.on_trip)
				numFreeVehicles++;
		}
		
		return numFreeVehicles;
	}
	
	private Vehicle getVehicle(Point pos) {
		for (Vehicle car : this.getVehicleList(pos)) {
			if (car.getState() == Vehicle_State.available)
				return car;
		}
		return null;
	}
	
	// Finds a vehicle within 5 minutes distance from origin
	public Vehicle findFreeVehicle(Point origin) {
		int radius = this.getMaxSpeed();
		Vehicle freeSAV = null;
		LinkedList<Point> queue = new LinkedList<Point>();
		ArrayList<Point> visited = new ArrayList<Point>();
		Point currPoint = origin;
		do {
			visited.add(currPoint);
			freeSAV = getVehicle(currPoint);
			if (freeSAV != null) {
				return freeSAV;
			}
			
			if (distanceFrom(origin, currPoint) < radius) {
				if (currPoint.x > 1 ) {
					Point newPoint = new Point(currPoint.x-1, currPoint.y);
					if (!queue.contains(newPoint) && !visited.contains(newPoint)) {
						queue.add(newPoint);
					}
				}
				if (currPoint.x < 40) {
					Point newPoint = new Point(currPoint.x+1, currPoint.y);
					if (!queue.contains(newPoint) && !visited.contains(newPoint)) {
						queue.add(newPoint);
					}
				}
				if (currPoint.y > 1) {
					Point newPoint = new Point(currPoint.x, currPoint.y-1);
					if (!queue.contains(newPoint) && !visited.contains(newPoint)) {
						queue.add(newPoint);
					}
				}
				if (currPoint.y < 40) {
					Point newPoint = new Point(currPoint.x, currPoint.y+1);
					if (!queue.contains(newPoint) && !visited.contains(newPoint)) {
						queue.add(newPoint);
					}
				}
			}
			
			currPoint = queue.remove();
			
		} while (!queue.isEmpty());
		
		return null; // if we get here, there is no vehicle in range
			
	}
	
	public void updateVehicleStates() {
		this.updateVehicles();
		
		for (Vehicle car : vehicles) {
			if (car.getState() != Vehicle_State.on_trip) {
				if (car.getState() == Vehicle_State.available) {
					car.update(0);
				}
				car.setState(Vehicle_State.available);
			}
		}
		
		
	}
	
}
