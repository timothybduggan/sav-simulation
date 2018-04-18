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
//	private int numVehicles;
	private int timeStep;
	private double threshold = 0.01;
	
	public VehicleRelocation(TripAssignment tripAssignment, TripGeneration tripGeneration, Map map, ArrayList<Vehicle> vehicles) {
		this.tripAssignment = tripAssignment;
		this.tripGeneration = tripGeneration;
		this.map = map;
		this.allVehicles = vehicles;
//		this.numVehicles = vehicles.size();
		
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
	
	/*
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
	*/
	
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

	/*
	private int availableVehiclesInBlockR2(int i, int j) {
		int[][] numVehicles = map.getAvailableVehicles();
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
	*/	
	public void applyR3() {
		
		for (Vehicle vehicle : allVehicles) {
			
			if (vehicle.getState() != Vehicle_State.available) continue;
			
			Point moveTo = searchR3(vehicle.getPosition());
			
			if (moveTo == null) continue;
			
			map.moveVehicle(vehicle, moveTo);
			vehicle.setDestination(moveTo);
			vehicle.setState(Vehicle_State.on_relocation_r3);
			vehicle.update(timeStep);
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
				//if (Math.random() < 0.50) bestPos = point;
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
			vehicle.setState(Vehicle_State.on_relocation_r4);
			vehicle.update(0);
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
				//if (Math.random() < 0.50) bestPos = point;
			}
		}
		
		return bestPos;
		
	}
	
	public void update(int timeStep) {
		this.timeStep = timeStep;
		applyR1();
		applyR2();
		applyR3();
//		applyR4();
	}

	private void applyR1() {
		// Divide into 25 2x2 blocks (8x8)
		int iteration = 0;
		double[][] balances = calculateBlocksR1();
		boolean[][] serviced = new boolean[5][5];
		do {
			iteration++;
			Point maxBlock = findMaxBlock(balances, serviced);
			balanceBlockR1(maxBlock, balances);
			balances = calculateBlocksR1();
			serviced[maxBlock.x][maxBlock.y] = true;
		} while (!isCompleteR1(balances, serviced) && iteration < 25);
	}

	private void applyR2() {
		// Divide into 100 1x1 blocks (4x4)
		int iteration = 0;
		double[][] balances = calculateBlocksR2();
		boolean[][] serviced = new boolean[10][10];
		do {
			iteration++;
			Point maxBlock = findMaxBlock(balances, serviced);
			balanceBlockR2(maxBlock, balances);
			balances = calculateBlocksR2();
			serviced[maxBlock.x][maxBlock.y] = true;
		} while (!isCompleteR2(balances, serviced) && iteration < 100);
	}
	
	private double[][] calculateBlocksR1() {
		double totalDemand = calculateTotalDemand();
		double[][] blocks = new double[5][5];
		int totalFreeVehicles = totalFreeVehicles();
		
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				blocks[i][j] = totalFreeVehicles * (((double) vehiclesInBlockR1(i,j) / (double) totalFreeVehicles)
											        - (demandInBlockR1(i,j) / totalDemand));
				//System.out.print(blocks[i][j] + "\t");
			}
			//System.out.println();
		}
		
		//System.out.println("~~~~~~~~~~~~~~~~~~~~");
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
	
	private double calculateTotalDemand() {
		double totalDemand = totalGeneratedDemand;
		totalDemand += tripAssignment.getWaitList().size();
		
		return totalDemand;
	}
	
	private Point findMaxBlock(double[][] blockBalances) {
		double maxVal = 0;
		Point retPoint = new Point(-1,-1);
		for (int i = 0; i < blockBalances.length; i++) {
			for (int j = 0; j < blockBalances[i].length; j++) {
				if (Math.abs(blockBalances[i][j]) > maxVal) {
					maxVal = Math.abs(blockBalances[i][j]);
					retPoint.x = i;
					retPoint.y = j;
				}
			}
		}
		return retPoint;
	}

	private Point findMaxBlock(double[][] blockBalances, boolean[][] serviced) {
		double maxVal = 0;
		Point retPoint = new Point(-1,-1);
		for (int i = 0; i < blockBalances.length; i++) {
			for (int j = 0; j < blockBalances[i].length; j++) {
				if (Math.abs(blockBalances[i][j]) > maxVal) {
					if (serviced[i][j]) continue;
					else {
						maxVal = Math.abs(blockBalances[i][j]);
						retPoint.x = i; 
						retPoint.y = j;
					}
				}
			}
		}
		return retPoint;
	}
	
	private void balanceBlockR1(Point block, double[][] balances) {
		boolean[] hasDirection = checkDirectionsR1(block); // 0 - north, 1 - east, 2 - south, 3 - west;
		int[] freeVehiclesInBlock = calculateAvailableVehiclesR1(block, hasDirection);
		
		if (balances[block.x][block.y] > 0) { // positive == too many vehicles
			pushVehiclesFromR1(block, hasDirection, freeVehiclesInBlock, balances);
		} else {
			pullVehiclesToR1(block, hasDirection, freeVehiclesInBlock, balances);
		}
	}
	
	private void balanceBlockR2(Point block, double[][] balances) {
		boolean[] hasDirection = checkDirectionsR2(block); // 0 - north, 1 - east, 2 - south, 3 - west;
		int[] freeVehiclesInBlock = calculateAvailableVehiclesR2(block, hasDirection);
		
		if (balances[block.x][block.y] > 0) { // positive == too many vehicles
			pushVehiclesFromR2(block, hasDirection, freeVehiclesInBlock, balances);
		} else {
			pullVehiclesToR2(block, hasDirection, freeVehiclesInBlock, balances);
		}
	}
	
	private boolean[] checkDirectionsR1(Point block) {
		boolean[] hasDirection = new boolean[4]; // 0 - north, 1 - east, 2 - south, 3 - west;
		if (block.x > 0) hasDirection[3] = true;
		if (block.x < 4) hasDirection[1] = true;
		if (block.y > 0) hasDirection[2] = true;
		if (block.y < 4) hasDirection[0] = true;
		
		return hasDirection;
	}
	
	private boolean[] checkDirectionsR2(Point block) {
		boolean[] hasDirection = new boolean[4]; // 0-north, 1-east, 2-south, 3-west
		if (block.x > 0) hasDirection[3] = true;
		if (block.x < 9) hasDirection[1] = true;
		if (block.y > 0) hasDirection[2] = true;
		if (block.y < 9) hasDirection[0] = true;
		
		return hasDirection;
	}
	
	private int[] calculateAvailableVehiclesR1(Point block, boolean[] hasDirection) {
		int[] freeVehiclesInBlock = new int[5]; // 0-north, 1-east, 2-south, 3-west, 4-local
		if (hasDirection[0]) freeVehiclesInBlock[0] = availableVehiclesInBlockR1(block.x, block.y+1);
		if (hasDirection[1]) freeVehiclesInBlock[1] = availableVehiclesInBlockR1(block.x+1, block.y);
		if (hasDirection[2]) freeVehiclesInBlock[2] = availableVehiclesInBlockR1(block.x, block.y-1);
		if (hasDirection[3]) freeVehiclesInBlock[3] = availableVehiclesInBlockR1(block.x-1, block.y);
		freeVehiclesInBlock[4] = availableVehiclesInBlockR1(block.x, block.y);
		
		return freeVehiclesInBlock;
	}
	
	private int[] calculateAvailableVehiclesR2(Point block, boolean[] hasDirection) {
		int[] freeVehiclesInBlock = new int[5]; // 0-north, 1-east, 2-south, 3-west, 4-local
		if (hasDirection[0]) freeVehiclesInBlock[0] = availableVehiclesInBlockR2(block.x, block.y+1);
		if (hasDirection[1]) freeVehiclesInBlock[1] = availableVehiclesInBlockR2(block.x+1, block.y);
		if (hasDirection[2]) freeVehiclesInBlock[2] = availableVehiclesInBlockR2(block.x, block.y-1);
		if (hasDirection[3]) freeVehiclesInBlock[3] = availableVehiclesInBlockR2(block.x-1, block.y);
		freeVehiclesInBlock[4] = availableVehiclesInBlockR2(block.x, block.y);
		
		return freeVehiclesInBlock;
	}
	
	private int availableVehiclesInBlockR1(int i, int j) {
		int[][] numVehicles = map.getAvailableVehicles();
		int vehiclesInBlock = 0;
		
		for (int k = 0; k < 64; k++) {
			vehiclesInBlock += numVehicles[8*i+k%8][8*j+k/8];
		}
		
		return vehiclesInBlock;
	}

	private int availableVehiclesInBlockR2(int i, int j) {
		int[][] numVehicles = map.getAvailableVehicles();
		int vehiclesInBlock = 0;
		
		for (int k = 0; k < 16; k++) {
			vehiclesInBlock += numVehicles[4*i+k%4][4*j+k/4];
		}
		
		return vehiclesInBlock;
	}
	
	private void pushVehiclesFromR1(Point block, boolean[] hasDirection, int[] availableVehicles, double[][] balances) {
		int[] vehiclesToDirection = assignPushDirections(block, hasDirection, availableVehicles, balances);
		double dir = Math.random();
		Corner start = null;
		if (dir < 0.25) start = Corner.top_left;
		else if (dir < 0.50) start = Corner.top_right;
		else if (dir < 0.75) start = Corner.bottom_right;
		else start = Corner.bottom_left;
		
		pushVehiclesR1(block, vehiclesToDirection, hasDirection, start);
		
	}
	
	private void pushVehiclesFromR2(Point block, boolean[] hasDirection, int[] availableVehicles, double[][] balances) {
		int[] vehiclesToDirection = assignPushDirections(block, hasDirection, availableVehicles, balances);
		double dir = Math.random();
		Corner start = null;
		if (dir < 0.25) start = Corner.top_left;
		else if (dir < 0.50) start = Corner.top_right;
		else if (dir < 0.75) start = Corner.bottom_right;
		else start = Corner.bottom_left;
		
		pushVehiclesR2(block, vehiclesToDirection, hasDirection, start);
	}
	
	private void pullVehiclesToR1(Point block, boolean[] hasDirection, int[] availableVehicles, double[][] balances) {
		int[] vehiclesFromDirection = assignPullDirections(block, hasDirection, availableVehicles, balances);
		double dir = Math.random();
		Corner start = null;
		if (dir < 0.25) start = Corner.top_left;
		else if (dir < 0.50) start = Corner.top_right;
		else if (dir < 0.75) start = Corner.bottom_right;
		else start = Corner.bottom_left;
		
		pullVehiclesR1(block, vehiclesFromDirection, hasDirection, start);
		
	}
	
	private void pullVehiclesToR2(Point block, boolean[] hasDirection, int[] availableVehicles, double[][] balances) {
		int[] vehiclesFromDirection = assignPullDirections(block, hasDirection, availableVehicles, balances);
		double dir = Math.random();
		Corner start = null;
		if (dir < 0.25) start = Corner.top_left;
		else if (dir < 0.50) start = Corner.top_right;
		else if (dir < 0.75) start = Corner.bottom_right;
		else start = Corner.bottom_left;
		
		pullVehiclesR2(block, vehiclesFromDirection, hasDirection, start);
	}
	
	private void pushVehiclesR1(Point block, int[] vehiclesToDirection, boolean[] hasDirection, Corner start) {
		int vehiclesRemaining = 0;
		for (int i = 0; i < vehiclesToDirection.length; i++) vehiclesRemaining += vehiclesToDirection[i];
		int radius = 0;
		while (vehiclesRemaining > 0 && radius < 8) {
			switch (start) {
			case top_left:
				if (hasDirection[0]) vehiclesRemaining -= pushNorthR1(block, vehiclesToDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pushEastR1(block, vehiclesToDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pushSouthR1(block, vehiclesToDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pushWestR1(block, vehiclesToDirection, radius);
				break;
			case top_right:
				if (hasDirection[1]) vehiclesRemaining -= pushEastR1(block, vehiclesToDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pushSouthR1(block, vehiclesToDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pushWestR1(block, vehiclesToDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pushNorthR1(block, vehiclesToDirection, radius);
				break;
			case bottom_right:
				if (hasDirection[2]) vehiclesRemaining -= pushSouthR1(block, vehiclesToDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pushWestR1(block, vehiclesToDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pushNorthR1(block, vehiclesToDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pushEastR1(block, vehiclesToDirection, radius);
				break;
			case bottom_left:
				if (hasDirection[3]) vehiclesRemaining -= pushWestR1(block, vehiclesToDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pushNorthR1(block, vehiclesToDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pushEastR1(block, vehiclesToDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pushSouthR1(block, vehiclesToDirection, radius);
				break;
			}
			radius++;
		}
		
	}
	
	private void pushVehiclesR2(Point block, int[] vehiclesToDirection, boolean[] hasDirection, Corner start) {
		int vehiclesRemaining = 0;
		for (int i = 0; i < vehiclesToDirection.length; i++) vehiclesRemaining += vehiclesToDirection[i];
		int radius = 0;
		while (vehiclesRemaining > 0 && radius < 4) {
			switch(start) {
			case top_left:
				if (hasDirection[0]) vehiclesRemaining -= pushNorthR2(block, vehiclesToDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pushEastR2(block, vehiclesToDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pushSouthR2(block, vehiclesToDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pushWestR2(block, vehiclesToDirection, radius);
				break;
			case top_right:
				if (hasDirection[1]) vehiclesRemaining -= pushEastR2(block, vehiclesToDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pushSouthR2(block, vehiclesToDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pushWestR2(block, vehiclesToDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pushNorthR2(block, vehiclesToDirection, radius);
				break;
			case bottom_right:
				if (hasDirection[2]) vehiclesRemaining -= pushSouthR2(block, vehiclesToDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pushWestR2(block, vehiclesToDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pushNorthR2(block, vehiclesToDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pushEastR2(block, vehiclesToDirection, radius);
				break;
			case bottom_left:
				if (hasDirection[3]) vehiclesRemaining -= pushWestR2(block, vehiclesToDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pushNorthR2(block, vehiclesToDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pushEastR2(block, vehiclesToDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pushSouthR2(block, vehiclesToDirection, radius);
				break;
			}
			radius++;
		}
	}
	
	private void pullVehiclesR1(Point block, int[] vehiclesFromDirection, boolean[] hasDirection, Corner start) {
		int vehiclesRemaining = 0;
		for (int i = 0; i < vehiclesFromDirection.length; i++) vehiclesRemaining += vehiclesFromDirection[i];
		int radius = 0;
		while (vehiclesRemaining > 0 && radius < 8) {
			switch (start) {
			case top_left:
				if (hasDirection[0]) vehiclesRemaining -= pullNorthR1(block, vehiclesFromDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pullEastR1(block, vehiclesFromDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pullSouthR1(block, vehiclesFromDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pullWestR1(block, vehiclesFromDirection, radius);
				break;
			case top_right:
				if (hasDirection[1]) vehiclesRemaining -= pullEastR1(block, vehiclesFromDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pullSouthR1(block, vehiclesFromDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pullWestR1(block, vehiclesFromDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pullNorthR1(block, vehiclesFromDirection, radius);
				break;
			case bottom_right:
				if (hasDirection[2]) vehiclesRemaining -= pullSouthR1(block, vehiclesFromDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pullWestR1(block, vehiclesFromDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pullNorthR1(block, vehiclesFromDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pullEastR1(block, vehiclesFromDirection, radius);
				break;
			case bottom_left:
				if (hasDirection[3]) vehiclesRemaining -= pullWestR1(block, vehiclesFromDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pullNorthR1(block, vehiclesFromDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pullEastR1(block, vehiclesFromDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pullSouthR1(block, vehiclesFromDirection, radius);
				break;
			}
			radius++;
		}
	}
	
	private void pullVehiclesR2(Point block, int[] vehiclesFromDirection, boolean[] hasDirection, Corner start) {
		int vehiclesRemaining = 0;
		for (int i = 0; i < vehiclesFromDirection.length; i++) vehiclesRemaining += vehiclesFromDirection[i];
		int radius = 0;
		while (vehiclesRemaining > 0 && radius < 4) {
			switch (start) {
			case top_left:
				if (hasDirection[0]) vehiclesRemaining -= pullNorthR2(block, vehiclesFromDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pullEastR2(block, vehiclesFromDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pullSouthR2(block, vehiclesFromDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pullWestR2(block, vehiclesFromDirection, radius);
				break;
			case top_right:
				if (hasDirection[1]) vehiclesRemaining -= pullEastR2(block, vehiclesFromDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pullSouthR2(block, vehiclesFromDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pullWestR2(block, vehiclesFromDirection, radius);				
				if (hasDirection[0]) vehiclesRemaining -= pullNorthR2(block, vehiclesFromDirection, radius);
				break;
			case bottom_right:
				if (hasDirection[2]) vehiclesRemaining -= pullSouthR2(block, vehiclesFromDirection, radius);
				if (hasDirection[3]) vehiclesRemaining -= pullWestR2(block, vehiclesFromDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pullNorthR2(block, vehiclesFromDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pullEastR2(block, vehiclesFromDirection, radius);
				break;
			case bottom_left:
				if (hasDirection[3]) vehiclesRemaining -= pullWestR2(block, vehiclesFromDirection, radius);
				if (hasDirection[0]) vehiclesRemaining -= pullNorthR2(block, vehiclesFromDirection, radius);
				if (hasDirection[1]) vehiclesRemaining -= pullEastR2(block, vehiclesFromDirection, radius);
				if (hasDirection[2]) vehiclesRemaining -= pullSouthR2(block, vehiclesFromDirection, radius);
				break;
			}
			radius++;
		}
	}
	
	private int pushNorthR1(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 0; i < 8 && vehiclesToMove[0] > 0; i++) {
			if (freeVehicles[8 * block.x + i][8 * (block.y + 1) - 1 - radius] >= 1) {
				while (freeVehicles[8 * block.x + i][8 * (block.y + 1) - 1 - radius] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(8 * block.x + i + 1, 8 * (block.y + 1) - 1 - radius + 1));
					Point moveTo = new Point(8 * block.x + i + 1, 8 * (block.y + 1) + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r1);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[0]--;
					freeVehicles[8 * block.x + i][8 * (block.y + 1) - 1 - radius]--;
				}
			}
		}
		return movedVehicles;
	}
	
	private int pushNorthR2(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 0; i < 4 && vehiclesToMove[0] > 0; i++) {
			if (freeVehicles[4 * block.x + i][4 * (block.y + 1) - 1 - radius] >= 1) {
				Vehicle moveMe = map.getFreeVehicleAt(new Point(4 * block.x + i + 1, 4 * (block.y + 1) - 1 - radius + 1));
				Point moveTo = new Point(4 * block.x + i + 1, 4 * (block.y + 1) + 1);
				
				map.moveVehicle(moveMe, moveTo);
				moveMe.setDestination(moveTo);
				moveMe.setState(Vehicle_State.on_relocation_r2);
				moveMe.update(timeStep);
				
				movedVehicles++;
				vehiclesToMove[0]--;
				freeVehicles[4 * block.x + i][4 * (block.y + 1) - 1 - radius]--;
			}
		}
		return movedVehicles;
	}
	
	private int pullNorthR1(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 0; i < 8 && vehiclesToMove[0] > 0; i++) {
			if (freeVehicles[8 * block.x + i][8 * (block.y + 1) + radius] >= 1) {
				while (freeVehicles[8 * block.x + i][8 * (block.y + 1) + radius] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(8 * block.x + i + 1, 8 * (block.y + 1) + radius + 1));
					Point moveTo = new Point(8 * block.x + i + 1, 8 * (block.y + 1) - 1 + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r1);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[0]--;
					freeVehicles[8 * block.x + i][8 * (block.y + 1) + radius]--;
				}
			}
		}
		return movedVehicles;
	}
	
	private int pullNorthR2(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 0; i < 4 && vehiclesToMove[0] > 0; i++) {
			if (freeVehicles[4 * block.x + i][4 * (block.y + 1) + radius] >= 1) {
				while (freeVehicles[4 * block.x + i][4 * (block.y + 1) + radius] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(4 * block.x + i + 1, 4 * (block.y + 1) + radius + 1));
					Point moveTo = new Point(4 * block.x + i + 1, 4 * (block.y + 1) - 1 + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r2);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[0]--;
					freeVehicles[4 * block.x + i][4 * (block.y + 1) + radius]--;
				}
			}
		}
		return movedVehicles;
	}
	
	private int pushSouthR1(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 7; i >= 0 && vehiclesToMove[2] > 0; i--) {
			if (freeVehicles[8 * block.x + i][8 * block.y + radius] >= 1) {
				while (freeVehicles[8 * block.x + i][8 * block.y + radius] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(8 * block.x + i + 1, 8 * block.y + radius + 1));
					Point moveTo = new Point(8 * block.x + i + 1, 8 * block.y - 1 + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r1);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[2]--;
					freeVehicles[8 * block.x + i][8 * block.y + radius]--;
				}
			}
		}
		return movedVehicles;
	}

	private int pushSouthR2(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 3; i >= 0 && vehiclesToMove[2] > 0; i--) {
			if (freeVehicles[4 * block.x + i][4 * block.y + radius] >= 1) {
				while (freeVehicles[4 * block.x + i][4 * block.y + radius] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(4 * block.x + i + 1, 4 * block.y + radius + 1));
					Point moveTo = new Point(4 * block.x + i + 1, 4 * block.y - 1 + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r2);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[2]--;
					freeVehicles[4 * block.x + i][4 * block.y + radius]--;
				}
			}
		}
		return movedVehicles;
	}
	
	private int pullSouthR1(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 7; i >= 0 && vehiclesToMove[2] > 0; i--) {
			if (freeVehicles[8 * block.x + i][8 * block.y - 1 - radius] >= 1) {
				while (freeVehicles[8 * block.x + i][8 * block.y - 1 - radius] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(8 * block.x + i + 1, 8 * block.y - 1 - radius + 1));
					Point moveTo = new Point(8 * block.x + i + 1, 8 * block.y + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r1);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[2]--;
					freeVehicles[8 * block.x + i][8 * block.y - 1 - radius]--;
				}
			}
		}
		return movedVehicles;
	}

	private int pullSouthR2(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 3; i >= 0 && vehiclesToMove[2] > 0; i--) {
			if (freeVehicles[4 * block.x + i][4 * block.y - 1 - radius] >= 1) {
				while (freeVehicles[4 * block.x + i][4 * block.y - 1 - radius] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(4 * block.x + i + 1, 4 * block.y - 1 - radius + 1));
					Point moveTo = new Point(4 * block.x + i + 1, 4 * block.y + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r2);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[2]--;
					freeVehicles[4 * block.x + i][4 * block.y - 1 - radius]--;
				}
			}
		}
		return movedVehicles;
	}
	
	private int pushEastR1(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 7; i >= 0 && vehiclesToMove[1] > 0; i--) {
			if (freeVehicles[8 * (block.x + 1) - 1 - radius][8 * block.y + i] >= 1) {
				while (freeVehicles[8 * (block.x + 1) - 1 - radius][8 * block.y + i] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(8 * (block.x + 1) - 1 - radius + 1, 8 * block.y + i + 1));
					Point moveTo = new Point(8 * (block.x + 1) + 1, 8 * block.y + i + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r1);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[1]--;
					freeVehicles[8 * (block.x + 1) - 1 - radius][8 * block.y + i]--;
				}
			}
		}
		return movedVehicles;
	}

	private int pushEastR2(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 3; i >= 0 && vehiclesToMove[1] > 0; i--) {
			if (freeVehicles[4 * (block.x + 1) - 1 - radius][4 * block.y + i] >= 1) {
				while (freeVehicles[4 * (block.x + 1) - 1 - radius][4 * block.y + i] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(4 * (block.x + 1) - 1 - radius + 1, 4 * block.y + i + 1));
					Point moveTo = new Point(4 * (block.x + 1) + 1, 4 * block.y + i + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r2);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[1]--;
					freeVehicles[4 * (block.x + 1) - 1 - radius][4 * block.y + i]--;
				}
			}
		}
		return movedVehicles;
	}

	private int pullEastR1(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 7; i >= 0 && vehiclesToMove[1] > 0; i--) {
			if (freeVehicles[8 * (block.x + 1) + radius][8 * block.y + i] >= 1) {
				while (freeVehicles[8 * (block.x + 1) + radius][8 * block.y + i] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(8 * (block.x + 1) + radius + 1, 8 * block.y + i + 1));
					Point moveTo = new Point(8 * (block.x + 1) - 1 + 1, 8 * block.y + i + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r1);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[1]--;
					freeVehicles[8 * (block.x + 1) + radius][8 * block.y + i]--;
				}
			}
		}
		return movedVehicles;
	}

	private int pullEastR2(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 3; i >= 0 && vehiclesToMove[1] > 0; i--) {
			if (freeVehicles[4 * (block.x + 1) + radius][4 * block.y + i] >= 1) {
				while (freeVehicles[4 * (block.x + 1) + radius][4 * block.y + i] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(4 * (block.x + 1) + radius + 1, 4 * block.y + i + 1));
					Point moveTo = new Point(4 * (block.x + 1) - 1 + 1, 4 * block.y + i + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r2);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[1]--;
					freeVehicles[4 * (block.x + 1) + radius][4 * block.y + i]--;
				}
			}
		}
		return movedVehicles;
	}

	private int pushWestR1(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 0; i < 8 && vehiclesToMove[3] > 0; i++) {
			if (freeVehicles[8 * block.x + radius][8 * block.y + i] >= 1) {
				while (freeVehicles[8 * block.x + radius][8 * block.y + i] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(8 * block.x + radius + 1, 8 * block.y + i + 1));
					Point moveTo = new Point(8 * block.x - 1 + 1, 8 * block.y + i + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r1);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[3]--;
					freeVehicles[8 * block.x + radius][8 * block.y + i]--;
				}
			}
		}
		return movedVehicles;
	}

	private int pushWestR2(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 0; i < 4 && vehiclesToMove[3] > 0; i++) {
			if (freeVehicles[4 * block.x + radius][4 * block.y + i] >= 1) {
				while (freeVehicles[4 * block.x + radius][4 * block.y + i] >= 1) {
					Vehicle moveMe = map.getFreeVehicleAt(new Point(4 * block.x + radius + 1, 4 * block.y + i + 1));
					Point moveTo = new Point(4 * block.x - 1 + 1, 4 * block.y + i + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r2);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[3]--;
					freeVehicles[4 * block.x + radius][4 * block.y + i]--;
				}
			}
		}
		return movedVehicles;
	}

	private int pullWestR1(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 0; i < 8 && vehiclesToMove[3] > 0; i++) {
			if (freeVehicles[8 * block.x - 1 - radius][8 * block.y + i] >= 1) {
				while (freeVehicles[8 * block.x - 1 - radius][8 * block.y + i] >= 1) {
					Vehicle moveMe = map.findFreeVehicle(new Point(8 * block.x - 1 - radius + 1, 8 * block.y + i + 1));
					Point moveTo = new Point(8 * block.x + 1, 8 * block.y + i + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r1);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[3]--;
					freeVehicles[8 * block.x - 1 - radius][8 * block.y + i]--;
				}
			}
		}
		return movedVehicles;
	}

	private int pullWestR2(Point block, int[] vehiclesToMove, int radius) {
		int[][] freeVehicles = map.getAvailableVehicles();
		int movedVehicles = 0;
		for (int i = 0; i < 4 && vehiclesToMove[3] > 0; i++) {
			if (freeVehicles[4 * block.x - 1 - radius][4 * block.y + i] >= 1) {
				while (freeVehicles[4 * block.x - 1 - radius][4 * block.y + i] >= 1) {
					Vehicle moveMe = map.findFreeVehicle(new Point(4 * block.x - 1 - radius + 1, 4 * block.y + i + 1));
					Point moveTo = new Point(4 * block.x + 1, 4 * block.y + i + 1);
					
					map.moveVehicle(moveMe, moveTo);
					moveMe.setDestination(moveTo);
					moveMe.setState(Vehicle_State.on_relocation_r2);
					moveMe.update(timeStep);
					
					movedVehicles++;
					vehiclesToMove[3]--;
					freeVehicles[4 * block.x - 1 - radius][4 * block.y + i]--;
				}
			}
		}
		return movedVehicles;
	}

	private int[] assignPushDirections(Point block, boolean[] hasDirection, int[] availableVehicles, double[][] balances) {
		double[] adjacentBalances = findAdjacentBalances(block, hasDirection, balances);
		int[] vehiclesToDirection = new int[4];
		int vehiclesRemaining = availableVehicles[4];
		double maxDifference = 0;
		int totalFreeVehicles = countFreeVehicles();
		do {
			int minDirection = findMinDirection(hasDirection, adjacentBalances);
			vehiclesToDirection[minDirection]++;
			adjacentBalances[minDirection]++;
			adjacentBalances[4]--;
			maxDifference = calculateMaxDifference(hasDirection, adjacentBalances);
			vehiclesRemaining--;
		} while (maxDifference >= 2 && vehiclesRemaining > 0 && (adjacentBalances[4] >= totalFreeVehicles * threshold));
		
		return vehiclesToDirection;
	}
	
	private int[] assignPullDirections(Point block, boolean[] hasDirection, int[] availableVehicles, double[][] balances) {
		double[] adjacentBalances = findAdjacentBalances(block, hasDirection, balances);
		int[] vehiclesFromDirection = new int[4];
		int vehiclesRemaining = 0;
		for (int i = 0; i < 4; i++) {
			if (hasDirection[i]) vehiclesRemaining += availableVehicles[i];
		}
		double maxDifference = 0;
		int totalFreeVehicles = countFreeVehicles();
		do {
			int maxDirection = findMaxDirection(hasDirection, adjacentBalances);
			vehiclesFromDirection[maxDirection]++;
			adjacentBalances[maxDirection]--;
			adjacentBalances[4]++;
			maxDifference = calculateMaxDifference(hasDirection, adjacentBalances);
			vehiclesRemaining--;
		} while (maxDifference >= 2 && vehiclesRemaining > 0 && (adjacentBalances[4] <= totalFreeVehicles * (-1 * threshold)));
		
		return vehiclesFromDirection;
	}
	
	private double[] findAdjacentBalances(Point block, boolean[] directions, double[][] balances) {
		double[] adjBalances = new double[5]; //NESW local -- 0123 4
		if (directions[0]) adjBalances[0] = balances[block.x][block.y+1];
		if (directions[1]) adjBalances[1] = balances[block.x+1][block.y];
		if (directions[2]) adjBalances[2] = balances[block.x][block.y-1];
		if (directions[3]) adjBalances[3] = balances[block.x-1][block.y];
		adjBalances[4] = balances[block.x][block.y];
		
		return adjBalances;
	}
	
	private int findMinDirection(boolean[] hasDirection, double[] adjacentBalances) {
		int minDirection = -1;
		double minBalance = -1;
		for (int i = 0; i < hasDirection.length; i++) {
			if (!hasDirection[i]) continue;
			if (minDirection == -1) {
				minDirection = i;
				minBalance = adjacentBalances[i];
			} else if (adjacentBalances[i] < minBalance) {
				minBalance = adjacentBalances[i];
				minDirection = i;
			}
		}
		
		return minDirection;
	}
	
	private int findMaxDirection(boolean[] hasDirection, double[] adjacentBalances) {
		int maxDirection = -1;
		double maxBalance = -1;
		for (int i = 0; i < hasDirection.length; i++) {
			if (!hasDirection[i]) continue;
			if (maxDirection == -1) {
				maxDirection = i;
				maxBalance = adjacentBalances[i];
			} else if (adjacentBalances[i] > maxBalance) {
				maxBalance = adjacentBalances[i];
				maxDirection = i;
			}
		}
		
		return maxDirection;
	}
	
	private double calculateMaxDifference(boolean[] hasDirection, double[] adjBalances) {
		double maxDifference = -1;
		for (int i = 0; i < hasDirection.length; i++) {
			if (!hasDirection[i]) continue;
			if (maxDifference == -1) {
				maxDifference = Math.abs(adjBalances[4] - adjBalances[i]);
			} else if (Math.abs(adjBalances[4] - adjBalances[i]) > maxDifference) {
				maxDifference = Math.abs(adjBalances[4] - adjBalances[i]);
			}
		}
		
		return maxDifference;
	}
	
	private boolean isCompleteR1(double[][] balances, boolean[][] serviced) {
		int totalFreeVehicles = countFreeVehicles();
		
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (!serviced[i][j] && Math.abs(balances[i][j]) >= totalFreeVehicles * threshold) 
					return false;
			}
		}
		
		return true;
	}
	
	private boolean isCompleteR2(double[][] balances, boolean[][] serviced) {
		int totalFreeVehicles = countFreeVehicles();
		
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				if (!serviced[i][j] && Math.abs(balances[i][j]) >= totalFreeVehicles * threshold)
						return false;
			}
		}
		
		return true;
	}
	
	private int countFreeVehicles() {
		int[][] freeVehiclesAt = map.getNumFreeVehicles();
		int totalFreeVehicles = 0;
		
		for (int i = 0; i < freeVehiclesAt.length; i++) {
			for (int j = 0; j < freeVehiclesAt[i].length; j++) {
				totalFreeVehicles += freeVehiclesAt[i][j];
			}
		}
		
		return totalFreeVehicles;
	}
}
