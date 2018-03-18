package model;
import java.awt.Point;
import java.util.ArrayList;

public class VehicleRelocation {
	private double[][] estimatedDemandR1;
	private double[][] estimatedDemandR2;
	private ArrayList<Trip> currentDemand;
	private ArrayList<Vehicle> allVehicles; // all vehicles.
	private TripAssignment tripAssignment;
	private TripGeneration tripGeneration;
	private double totalGeneratedDemand;
	private int height = 40;
	private int width = 40;
	private Map map;
	
	public VehicleRelocation() {
		
	}
	
	private void calculateGeneratedDemand() {
		
	}
	
	public void estimateDemand() {
		this.resetEstimatedDemand();
		this.currentDemand = tripAssignment.getWaitList();
		
		// If a passenger is waiting to get picked up, they are 1 demand.
		for (Trip trip : currentDemand) {
			Point pos = trip.getOrigin();
			estimatedDemandR1[pos.x-1][pos.y-1] -= 1;
		}
		
		// Subtract generation rate for each zone (chance of spawning a trip this timeStep)
		for (int i = 1; i < width + 1; i++) {
			for (int j = 1; j < height + 1; j++) {
				Point pos = new Point(i,j);
				if (tripGeneration.isInnerCore(pos)) {
					estimatedDemandR1[i-1][j-1] -= tripGeneration.getInnerCoreGenerationRate();
				} else if (tripGeneration.isOuterCore(pos)) {
					estimatedDemandR1[i-1][j-1] -= tripGeneration.getOuterCoreGenerationRate();
				} else {
					estimatedDemandR1[i-1][j-1] -= tripGeneration.getOuterServiceGenerationRate();
				}
			}
		}
		
		// Add 1 for each vehicle in the area! (pos = more cars than needed)
		for (Vehicle vehicle : allVehicles) {
			Point pos = vehicle.getPosition();
			estimatedDemandR1[pos.x-1][pos.y-1] += 1;
		}
	}
	
	public double[][] getEstimatedDemandR1() {
		return this.estimatedDemandR1;
	}
	
	private void resetEstimatedDemand() {
		for (int i = 0; i < estimatedDemandR1.length; i++) {
			for (int j = 0; j < estimatedDemandR1[i].length; j++) {
				estimatedDemandR1[i][j] = 0;
			}
		}
	}
	
	private void relocation1() {
		
	}
	
	private void relocation2() {
		
	}
	
	private void relocation3() {
		
	}
	
	private void relocation4() {
		
	}
	
	public void update() {
		relocation1();
		relocation2();
		relocation3();
		relocation4();
	}
}
