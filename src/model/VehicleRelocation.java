package model;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

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
	private int numVehicles;
	
	public VehicleRelocation(TripAssignment tripAssignment, TripGeneration tripGeneration, Map map, ArrayList<Vehicle> vehicles) {
		this.tripAssignment = tripAssignment;
		this.tripGeneration = tripGeneration;
		this.map = map;
		this.allVehicles = vehicles;
		this.numVehicles = vehicles.size();
		
		calculateGeneratedDemand();
	}
	
	private void calculateGeneratedDemand() {
		double[][] generationRates = map.getGenerationRates();
		totalGeneratedDemand = 0;
		
		for (int i = 0; i < generationRates.length; i++) {
			for (int j = 0; j < generationRates[i].length; j++) {
				totalGeneratedDemand += generationRates[i][j];
			}
		}
		
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
	
	private void applyR1() {
		
		double[][] blocks = calculateBlocksR1();
		double maxBlock = maxBlockValue(blocks);
		double minBlock = minBlockValue(blocks);
		
		while (maxBlock > 10 || minBlock < 10) {
			Point nextBlock;
			double blockVal;
			
			if (maxBlock > -1*minBlock) {
				nextBlock = maxBlockPosition(blocks);
				blockVal = maxBlock;
			} else {
				nextBlock = minBlockPosition(blocks);
				blockVal = minBlock;
			}
			
			balanceBlockR1(nextBlock, blocks);
			
			
			blocks = calculateBlocksR1();
			maxBlock = maxBlockValue(blocks);
			minBlock = minBlockValue(blocks);
		}
	}
	
	private double[][] calculateBlocksR1() {
		double totalDemand = calculateTotalDemand();
		double[][] blocks = new double[5][5];
		int totalFreeVehicles = totalFreeVehicles();
		
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				blocks[i][j] = totalFreeVehicles * (((double) vehiclesInBlockR1(i,j) / (double) totalFreeVehicles)
											        - (demandInBlockR1(i,j) / totalDemand));
			}
		}
		
		return blocks;
	}
	
	private double[][] calculateBlocksR2() {
		double totalDemand = calculateTotalDemand();
		double[][] blocks = new double[10][10];
		int totalFreeVehicles = totalFreeVehicles();
		
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				blocks[i][j] = totalFreeVehicles * (((double) vehiclesInBlockR2(i,j) / (double) totalFreeVehicles)
													- (demandInBlockR2(i,j) / totalDemand));
			}
		}
		
		return blocks;
	}
	
	private double demandInBlockR1(int i, int j) {
		double[][] generatedDemand = map.getGenerationRates();
		int[][] waitingDemand = tripAssignment.getWaitListMap();
		double demandInBlock = 0;
		
		for (int k = 0; k < 64; k++) {
			demandInBlock += generatedDemand[8*i + k%8][8*j + k/8];
			demandInBlock += waitingDemand[8*i + k%8][8*j + k/8];
		}
		
		return demandInBlock;
	}

	private double demandInBlockR2(int i, int j) {
		double[][] generatedDemand = map.getGenerationRates();
		int[][] waitingDemand = tripAssignment.getWaitListMap();
		double demandInBlock = 0;
		
		for (int k = 0; k < 16; k++) {
			demandInBlock += generatedDemand[4*i + k%4][4*j + k/4];
			demandInBlock += waitingDemand[4*i + k%4][4*j + k/4];
		}
		
		return demandInBlock;
	}
	
	private int totalFreeVehicles() {
		int[][] numVehicles = map.getNumFreeVehicles();
		int totalFreeVehicles = 0;
		
		for (int i = 0; i < numVehicles.length; i++) {
			for (int j = 0; j < numVehicles[i].length; j++) {
				totalFreeVehicles += numVehicles[i][j];
			}
		}
		
		return totalFreeVehicles;
	}
	
	// returns num vehicles in a given block
	private int vehiclesInBlockR1(int i, int j) {
		int[][] numVehicles = map.getNumFreeVehicles();
		int vehiclesInBlock = 0;
		
		for (int k = 0; k < 64; k++) {
			vehiclesInBlock += numVehicles[8*i + k%8][8*j + k/8];
		}
		
		return vehiclesInBlock;
	}
	
	private int vehiclesInBlockR2(int i, int j) {
		int[][] numVehicles = map.getNumFreeVehicles();
		int vehiclesInBlock = 0;
		
		for (int k = 0; k < 16; k++) {
			vehiclesInBlock += numVehicles[4*i + k%4][4*j + k/4];
		}
		
		return vehiclesInBlock;	
	}
	
	private double maxBlockValue(double[][] blocks) {
		if (blocks == null) return -1;
		double maxBlock = blocks[0][0];
		for (int i = 0; i < blocks.length; i++) {
			for (int j = 0; j < blocks[i].length; j++) {
				if (blocks[i][j] > maxBlock) {
					maxBlock = blocks[i][j];
				}
			}
		}
		
		return maxBlock;
	}
	
	private double minBlockValue(double[][] blocks) {
		if (blocks == null) return +1;
		double minBlock = blocks[0][0];
		for (int i = 0; i < blocks.length; i++) {
			for (int j = 0; j < blocks[i].length; j++) {
				if (blocks[i][j] < minBlock) {
					minBlock = blocks[i][j];
				}
			}
		}
		
		return minBlock;
	}
	
	private Point maxBlockPosition(double[][] blocks) {
		double maxValue = blocks[0][0];
		Point maxPosition = new Point(0,0);
		
		for (int i = 0; i < blocks.length; i++) {
			for (int j = 0; j < blocks[i].length; j++) {
				if (blocks[i][j] > maxValue) {
					maxValue = blocks[i][j];
					maxPosition.x = i;
					maxPosition.y = j;
				}
			}
		}
		
		return maxPosition;
	}
	
	private Point minBlockPosition(double[][] blocks) {
		double minValue = blocks[0][0];
		Point minPosition = new Point(0,0);
		
		for (int i = 0; i < blocks.length; i++) {
			for (int j = 0; j < blocks[i].length; j++) {
				if (blocks[i][j] < minValue) {
					minValue = blocks[i][j];
					minPosition.x = i;
					minPosition.y = j;
				}
			}
		}
		
		return minPosition;
	}
	
	private double calculateTotalDemand() {
		double totalDemand = totalGeneratedDemand;
		totalDemand += tripAssignment.getWaitList().size();
		
		return totalDemand;
	}
	
	private void balanceBlockR1(Point block, double[][] blocks) {
		int numFreeSAV = 0;
		double blockVal = blocks[block.x][block.y];
		boolean hasNorth, hasSouth, hasEast, hasWest;
		boolean[] hasDirection = new boolean[4]; // 0-north, 1-east, 2-south, 3-west
		double[] adjacentBlockVals = new double[4]; // 0-north, 1-east, 2-south, 3-west
		int[] adjacentBlockVehicles = new int[4]; // 0-north, 1-east, 2-south, 3-west
		double diffBlocks = 5;
		
		if (block.x > 0) hasWest = true;
		else hasWest = false;
		if (block.x < 4) hasEast = true;
		else hasEast = false;
		if (block.y > 0) hasSouth = true;
		else hasSouth = false;
		if (block.y < 4) hasNorth = true;
		else hasNorth = false;
		
		// while there are still savs, 10% threshold exceeded, AND TODO: sav relocation will improve balances by >= 1
		while ((blockVal > 10 || blockVal < 10) && (numFreeSAV > 0)) {
		
			if (blockVal > 0) {
				numFreeSAV = vehiclesInBlockR1(block.x, block.y);
				if (hasWest) adjacentBlockVals[3] = blocks[block.x-1][block.y];
				if (hasEast) adjacentBlockVals[1] = blocks[block.x+1][block.y];
				if (hasSouth) adjacentBlockVals[2] = blocks[block.x][block.y-1];
				if (hasNorth) adjacentBlockVals[0] = blocks[block.x][block.y-1];
				
				
			} else {
				if (hasWest) {
					numFreeSAV += vehiclesInBlockR1(block.x - 1, block.y);
					adjacentBlockVals[3] = blocks[block.x-1][block.y];
				}
				if (hasEast) {
					numFreeSAV += vehiclesInBlockR1(block.x + 1, block.y);
					adjacentBlockVals[1] = blocks[block.x+1][block.y];
				}
				if (hasSouth) {
					numFreeSAV += vehiclesInBlockR1(block.x, block.y - 1);
					adjacentBlockVals[2] = blocks[block.x][block.y-1];
				}
				if (hasNorth) {
					numFreeSAV += vehiclesInBlockR1(block.x, block.y + 1);
					adjacentBlockVals[0] = blocks[block.x][block.y+1];
				}
			}
		}
	}
	
	private void applyR2() {
		
	}
	
	public void applyR3() {
		
		for (Vehicle vehicle : allVehicles) {
			
			if (vehicle.getState() != Vehicle_State.available) continue;
			
			Point moveTo = searchR3(vehicle.getPosition());
			
			if (moveTo == null) continue;
			
			map.moveVehicle(vehicle, moveTo);
			vehicle.setDestination(moveTo);
			vehicle.update(0);
			vehicle.setState(Vehicle_State.on_relocation);
		}
	}
	
	private Point searchR3(Point pos) {
		if (map.freeVehiclesAt(pos) <= 1) return null;
		
		LinkedList<Point> queue = new LinkedList<Point>();
		ArrayList<Point> list = new ArrayList<Point>();
		ArrayList<Point> visited = new ArrayList<Point>();
		int radius = 2;
		Point currPoint = pos;
		
		
		
		do {
			visited.add(currPoint);
			
			if (map.distanceFrom(pos, currPoint) < radius) {
				if (currPoint.x > 1) {
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
			
			if (map.freeVehiclesAt(currPoint) == 0) list.add(currPoint);
			
			currPoint = queue.remove();
			
		} while (!queue.isEmpty());
		
		double maxRate = 0;
		Point bestPos = null;
		
		for (Point point : list) {
			if (map.getGenerationRate(point) > maxRate) {
				maxRate = map.getGenerationRate(point);
				bestPos = point;
			} else if (map.getGenerationRate(point) == maxRate) {
				if (Math.random() < 0.50) bestPos = point;
			}
		}
		
		return bestPos;
	}
	
	public void applyR4() {
		for (Vehicle vehicle : allVehicles) {
			
			if (vehicle.getState() != Vehicle_State.available) continue;
			
			Point moveTo = searchR4(vehicle.getPosition());
			
			if (moveTo == null) continue;
			
			map.moveVehicle(vehicle, moveTo);
			vehicle.setDestination(moveTo);
			vehicle.update(0);
			vehicle.setState(Vehicle_State.on_relocation);
		}
	}
	
	private Point searchR4(Point pos) {
		if (map.freeVehiclesAt(pos) < 3) return null;
		
		int vehiclesAtPos = map.freeVehiclesAt(pos);
		
		ArrayList<Point> list = new ArrayList<Point>();
		
		if (pos.x > 1 && map.freeVehiclesAt(new Point(pos.x-1, pos.y)) <= vehiclesAtPos - 3) {
			list.add(new Point(pos.x-1, pos.y));
		}
		if (pos.x < 40 && map.freeVehiclesAt(new Point(pos.x+1, pos.y)) <= vehiclesAtPos - 3) {
			list.add(new Point(pos.x+1, pos.y));
		}
		if (pos.y > 1 && map.freeVehiclesAt(new Point(pos.x, pos.y-1)) <= vehiclesAtPos - 3) {
			list.add(new Point(pos.x, pos.y-1));
		}
		if (pos.y < 40 && map.freeVehiclesAt(new Point(pos.x, pos.y+1)) <= vehiclesAtPos - 3) {
			list.add(new Point(pos.x, pos.y+1));
		}
		
		double maxRate = 0;
		Point bestPos = null;
		
		for (Point point : list) {
			if (map.getGenerationRate(point) > maxRate) {
				maxRate = map.getGenerationRate(point);
				bestPos = point;
			} else if (map.getGenerationRate(point) == maxRate) {
				if (Math.random() < 0.50) bestPos = point;
			}
		}
		
		return bestPos;
		
	}
	
	public void update() {
//		applyR1();
//		applyR2();
		applyR3();
		applyR4();
	}
}
