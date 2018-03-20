package tests;
import static org.junit.Assert.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import model.Map;
import model.Simulation;
import model.Trip;
import model.TripAssignment;
import model.TripGeneration;
import model.Vehicle;
import model.Vehicle_State;

public class test {
	
	@Test
	public void MovementTest1() {
		Vehicle a = new Vehicle(32,27); 		// (row, col)
		a.setDestination(new Point(34,1));		// (row, col)
		assertEquals(a.getPosition().x, 32); 	// row
		assertEquals(a.getPosition().y, 27); 	// column
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 34);	// row
		assertEquals(a.getPosition().y, 18);	// column
		assertEquals(a.getUnoccupiedMiles(), 2.75, .001);
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 34);	// row
		assertEquals(a.getPosition().y, 7);		// column
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 34);	// row
		assertEquals(a.getPosition().y, 1);		// column
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 34);	// row
		assertEquals(a.getPosition().y, 1);		// column
	}
	
	@Test
	public void MovementTest2() {
		Vehicle a = new Vehicle();
		a.setDestination(new Point(25,37));
		assertEquals(a.getPosition().x, 1);
		assertEquals(a.getPosition().y, 1);
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 12);
		assertEquals(a.getPosition().y, 1);
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 23);
		assertEquals(a.getPosition().y, 1);
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 25);
		assertEquals(a.getPosition().y, 10);
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 25);
		assertEquals(a.getPosition().y, 21);
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 25);
		assertEquals(a.getPosition().y, 32);
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 25);
		assertEquals(a.getPosition().y, 37);
		a.moveTowardsDestination();
		assertEquals(a.getPosition().x, 25);
		assertEquals(a.getPosition().y, 37);
	}
	
	@Test
	public void TripTest1() {
		Vehicle a = new Vehicle(3,4);
		
		assertFalse(a.inUse());
		assertEquals(a.getState(), Vehicle_State.available);
		
		Trip t = new Trip(new Point(1,1), new Point(10,10), a);	// trip from 1,1 to 10,10, using vehicle a.
		assertTrue(a.inUse());
		assertEquals(t.getOrigin().x, 1);
		assertEquals(t.getOrigin().y, 1);
		// a should now have two destinations: primary destination is 1,1 to pick up user.
		// secondary destination should be 10,10 to drop off user. 
		// on first 'moveTowardsDestination', a should go from 3,4 to 1,1, then to 6,1 in one step.
		
		assertEquals(a.getPosition().x, 3);
		assertEquals(a.getPosition().y, 4);
		assertEquals(a.getMilesDriven(), 0, 0.001);
		assertEquals(a.getState(), Vehicle_State.on_trip);
		assertFalse(t.isPickedUp());
		assertFalse(t.isFinished());
		assertEquals(t.getWaitTime(), 0, .001);
		t.update();
		assertEquals(a.getPreviousPosition().x, 3);
		assertEquals(a.getPreviousPosition().y, 4);
		assertEquals(a.getPosition().x, 7);
		assertEquals(a.getPosition().y, 1);
		assertEquals(a.getState(), Vehicle_State.on_trip);
		assertTrue(t.isPickedUp());
		assertFalse(t.isFinished());
		assertEquals(t.getWaitTime(), 2.273, .001);
		assertEquals(a.getMilesDriven(), 2.75, 0.001);
		assertEquals(a.getUnoccupiedMiles(), 1.25, .001);
		t.update();
		assertEquals(a.getPosition().x, 10);
		assertEquals(a.getPosition().y, 9);
		assertEquals(a.getState(), Vehicle_State.on_trip);
		assertFalse(t.isFinished());
		assertEquals(a.getMilesDriven(), 5.50, 0.001);
		t.update();
		System.out.println(a.getPosition() + " - " + t.getDestination() + " - " + a.getPosition().equals(t.getDestination()));
		assertEquals(a.getPosition().x, 10);
		assertEquals(a.getPosition().y, 10);
		assertEquals(a.getState(), Vehicle_State.end_trip); // end trip will be in "Vehicle State Enum"
		assertTrue(t.isFinished());
		assertEquals(a.getMilesDriven(), 5.75, 0.001);
		assertFalse(a.inUse());
		
	}
	
	@Test
	public void TripTest2() {
		Vehicle a = new Vehicle(3,4);
		Trip t = new Trip(new Point(1,1), new Point(10,10), null); // trip from 1,1 to 10,10. No vehicle yet.
		assertFalse(t.isPickedUp());
		assertFalse(t.isFinished());
		assertFalse(t.hasAssignedSAV());
		assertEquals(t.getCurrentPosition(), null);
		assertEquals(t.getWaitTime(), 0, .001);
		t.update();
		t.assignSAV(a);
		assertFalse(t.isPickedUp());
		assertFalse(t.isFinished());
		assertTrue(t.hasAssignedSAV());
		assertEquals(t.getWaitTime(), 5, .001);
		t.update();
		assertTrue(t.isPickedUp());
		assertFalse(t.isFinished());
		assertTrue(t.hasAssignedSAV());
		assertEquals(t.getWaitTime(), 7.273, .001);
		t.update();
		assertTrue(t.isPickedUp());
		assertFalse(t.isFinished());
		assertTrue(t.hasAssignedSAV());
		t.update();
		assertTrue(t.isPickedUp());
		assertTrue(t.isFinished());
		assertTrue(t.hasAssignedSAV());
	}

	@Test
	public void TripTest3() {
		Vehicle a = new Vehicle(38,37);
		
		assertEquals(a.getState(), Vehicle_State.available);
		
		Trip t = new Trip(new Point(40,40), new Point(31,31), a);	// trip from 1,1 to 10,10, using vehicle a.
		
		// a should now have two destinations: primary destination is 1,1 to pick up user.
		// secondary destination should be 10,10 to drop off user. 
		// on first 'moveTowardsDestination', a should go from 3,4 to 1,1, then to 6,1 in one step.
		
		assertEquals(a.getColdStarts(), 0);
		assertEquals(a.getNumTrips(), 0);
		assertEquals(a.getPosition().x, 38);
		assertEquals(a.getPosition().y, 37);
		assertEquals(a.getState(), Vehicle_State.on_trip);
		assertFalse(t.isPickedUp());
		assertFalse(t.isFinished());
		assertEquals(t.getWaitTime(), 0, .001);
		assertFalse(a.isOccupied());
		t.update();
		assertEquals(a.getNumTrips(), 1);
		assertEquals(a.getPosition().x, 34);
		assertEquals(a.getPosition().y, 40);
		assertEquals(a.getState(), Vehicle_State.on_trip);
		assertTrue(t.isPickedUp());
		assertFalse(t.isFinished());
		assertEquals(t.getWaitTime(), 2.273, .001);
		assertTrue(a.isOccupied());
		t.update();
		assertEquals(a.getPosition().x, 31);
		assertEquals(a.getPosition().y, 32);
		assertEquals(a.getState(), Vehicle_State.on_trip);
		assertFalse(t.isFinished());
		t.update();
		System.out.println(a.getPosition() + " - " + t.getDestination() + " - " + a.getPosition().equals(t.getDestination()));
		assertEquals(a.getPosition().x, 31);
		assertEquals(a.getPosition().y, 31);
		assertEquals(a.getState(), Vehicle_State.end_trip); // end trip will be in "Vehicle State Enum"
		assertTrue(t.isFinished());
		
	}	

	@Test
	public void TripTest4() {
		// Rush Hour Trip Tests (crosses into non-rush)
		Vehicle a = new Vehicle(1,1);
		Vehicle b = new Vehicle(1,1);
		Trip t = new Trip(new Point(1,1), new Point(40,40), a);
		assertFalse(t.assignSAV(b));
		
		assertEquals(t.getStartTime(), 0);
		assertEquals(a.getPosition(), new Point(1,1));
		t.update(90);
		assertEquals(a.getPosition(), new Point(8,1));
		t.update(91);
		assertEquals(a.getPosition(), new Point(15,1));
		t.update(92);
		assertEquals(a.getPosition(), new Point(22,1));
		t.update(93);
		assertEquals(a.getPosition(), new Point(29,1));
		t.update(94);
		assertEquals(a.getPosition(), new Point(36,1));
		t.update(95);
		assertEquals(a.getPosition(), new Point(40,4));
		t.update(96);
		assertEquals(a.getPosition(), new Point(40,15));
		t.update(97);
		assertEquals(t.getCurrentPosition(), new Point(40,26));
		t.update(98);
		assertEquals(a.getPosition(), new Point(40,37));
		t.update(99);
		assertEquals(a.getPosition(), new Point(40,40));
	}
	
	@Test
	public void TripTest5() {
		// Rush Hour Trip Tests (crosses into non-rush)
		Vehicle a = new Vehicle(1,1);
		Vehicle b = new Vehicle(1,1);
		Trip t = new Trip(new Point(1,1), new Point(40,40), a);
		assertFalse(t.assignSAV(b));
		
		assertEquals(t.getStartTime(), 0);
		assertEquals(a.getPosition(), new Point(1,1));
		t.update(216);
		assertEquals(a.getPosition(), new Point(8,1));
		t.update(217);
		assertEquals(a.getPosition(), new Point(15,1));
		t.update(218);
		assertEquals(a.getPosition(), new Point(22,1));
		t.update(219);
		assertEquals(a.getPosition(), new Point(29,1));
		t.update(220);
		assertEquals(a.getPosition(), new Point(36,1));
		t.update(221);
		assertEquals(a.getPosition(), new Point(40,4));
		t.update(222);
		assertEquals(a.getPosition(), new Point(40,15));
		t.update(223);
		assertEquals(t.getCurrentPosition(), new Point(40,26));
		t.update(224);
		assertEquals(a.getPosition(), new Point(40,37));
		t.update(225);
		assertEquals(a.getPosition(), new Point(40,40));
		
	}
	
	@Test
	public void SimulationTest1() {
		Simulation sim = new Simulation(false);
		
	}

	// to help me understand how .contains() works
	@Test
	public void ArrayListTest() {
		ArrayList<Point> list = new ArrayList<Point>();
		Point a = new Point(1,1);
		Point b = new Point(1,1);
		
		list.add(a);
		assertTrue(list.contains(b));
		list.remove(a);
		assertFalse(list.contains(a));
		
		ArrayList<Vehicle> cars = new ArrayList<Vehicle>();
		Vehicle c = new Vehicle(1,1);
		cars.add(c);
		assertTrue(cars.contains(c));
		assertEquals(cars.size(), 1);
		cars.remove(c);
		assertEquals(cars.size(), 0);
		cars.add(c);
		Vehicle d = new Vehicle(1,1);
		assertFalse(cars.contains(d));
	}

	@Test
	public void MapTest1() {
		Vehicle a = new Vehicle(2,2);
		ArrayList<Vehicle> list = new ArrayList<Vehicle>();
		list.add(a);
		TripGeneration tg = new TripGeneration();
		Map map = new Map(list, tg);
		tg.setMap(map);
		
		int[][] vehicleLocations = map.getNumVehicles();
		assertEquals(vehicleLocations[0][0], 0);
		assertEquals(vehicleLocations[1][1], 1);
		vehicleLocations = map.getNumFreeVehicles();
		assertEquals(vehicleLocations[1][1], 1);
		a.setState(Vehicle_State.on_trip);
		vehicleLocations = map.getNumFreeVehicles();
		assertEquals(vehicleLocations[0][0], 0);
		assertEquals(vehicleLocations[1][1], 0);
		a.setDestination(new Point(3,3));
		a.moveTowardsDestination();
		map.update();
		a.setState(Vehicle_State.available);
		vehicleLocations = map.getNumVehicles();
		assertEquals(vehicleLocations[1][1], 0);
		assertEquals(vehicleLocations[2][2], 1);
		assertEquals(map.getCurrentTimeStep(), 1);
		assertEquals(map.getMaxSpeed(), 11);
		for (int i = 0; i < 90; i++) {
			map.update();
		}
		assertEquals(map.getMaxSpeed(), 7);
		for (int i = 0; i < 60; i++) {
			map.update();
		}
		assertEquals(map.getCurrentTimeStep(), 151);
		assertEquals(map.getMaxSpeed(), 11);
		for (int i = 0; i < 60; i++) {
			map.update();
		}
		assertEquals(map.getMaxSpeed(), 7);
		for (int i = 0; i < 99; i++) {
			map.update();
		}
		assertEquals(map.getMaxSpeed(), 11);
		
		ArrayList<Vehicle> listB = map.getVehicleList(new Point(1,1));
		assertEquals(listB.size(), 0);
		listB = map.getVehicleList(new Point(3,3));
		assertEquals(map.getCurrentTimeStep(), 310);
		System.out.println(listB);
		assertEquals(listB.size(), 1);
		assertTrue(listB.contains(a));
		assertEquals(map.getGenerationRate(new Point(1,1)), .03125, .000001);
		assertEquals(map.getGenerationRate(0,0), .03125, .000001);
		assertEquals(map.distanceFrom(new Point(1,1), new Point(40,40)), 78);
		assertEquals(map.distanceFrom(new Point(40,40), new Point(1,1)), 78);
		assertEquals(map.distanceFromOuterCore(new Point(25,25)), -1);
		assertEquals(map.distanceFromOuterCore(new Point(10,1)), 11);
		assertEquals(map.distanceFromInnerCore(new Point(20,20)), -1);
		
		
		assertFalse(map.addVehicle(null));
		assertFalse(map.addVehicle(a));
		Vehicle b = new Vehicle(1,1);
		assertTrue(map.addVehicle(b));
		Vehicle c = new Vehicle(1,1);
		assertTrue(map.addVehicle(c));
		listB = map.getVehicleList(new Point(1,1));
		assertEquals(listB.size(), 2);
		
		assertEquals(map.findFreeVehicle(new Point(40,40)), null);
		assertEquals(map.findFreeVehicle(new Point(1,1)), b);
		b.setState(Vehicle_State.on_trip);
		assertEquals(map.findFreeVehicle(new Point(1,1)), c);
//		System.out.println(a.getState() + " : " + a.getPosition());
		assertEquals(map.findFreeVehicle(new Point(1,4)), a);
		assertEquals(map.findFreeVehicle(new Point(3,1)), c);
		assertEquals(map.findFreeVehicle(new Point(2,2)), c);
		assertEquals(map.findFreeVehicle(new Point(1,3)), a);
		assertEquals(map.findFreeVehicle(new Point(5,5)), a);
	}

	@Test
	public void TripAssignmentTest() {
		ArrayList<Vehicle> cars = initializeVehicles();
		
		TripGeneration tg = new TripGeneration();
		Map map = new Map(cars, tg);
		tg.setMap(map);
		TripAssignment ta = new TripAssignment(tg, map);
		
		assertEquals(ta.getFutureTrips().size(), 0);
		Trip t = new Trip(new Point(25,25), new Point(30,30), null, 3);
		ta.addFutureTrip(t);
		assertEquals(ta.getFutureTrips().size(), 1);
		
//		System.out.println(a.getState());
		ta.update();
		map.updateVehicleStates();
		assertEquals(ta.getFutureTrips().size(), 1);
//		System.out.println(ta.getWaitList().size());
//		System.out.println(a.getPosition());
//		System.out.println(a.getState());
		ta.update();
		map.updateVehicleStates();
		assertEquals(ta.getFutureTrips().size(), 1);
//		System.out.println(ta.getWaitList().size());
//		System.out.println(a.getPosition());
//		System.out.println(a.getState());
		ta.update();
		map.updateVehicleStates();
		assertEquals(ta.getFutureTrips().size(), 1);
//		System.out.println(ta.getWaitList().size());
//		System.out.println(a.getPosition());
//		System.out.println(a.getState());
		ta.update();
		map.updateVehicleStates();
		assertEquals(ta.getFutureTrips().size(), 0);
//		System.out.println(ta.getWaitList().size());
//		System.out.println(a.getPosition());
//		System.out.println(a.getState());
		ta.update();
		map.updateVehicleStates();
//		System.out.println(ta.getWaitList().size());
//		System.out.println(a.getPosition());
//		System.out.println(a.getState());
		for (int i = 0; i < 100; i++) {
			ta.update();
			map.updateVehicleStates();
		}
		
		ta.getWaitListMap();
		ta.getWaitList();
		
	}

	private ArrayList<Vehicle> initializeVehicles() {
		ArrayList<Vehicle> cars = new ArrayList<Vehicle>(1600);

		for (int i = 1; i < 41; i++) {
			for (int j = 1; j < 41; j++) {
				Vehicle addMe = new Vehicle(i,j);
				cars.add(addMe);
			}
		}
		
		return cars;
	}

	private void printVehicleMap(Map map) {

		int[][] numVehicles = map.getNumVehicles();
		
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				System.out.printf("%2d,", numVehicles[i][j]);
			}
			System.out.println();
		}
		System.out.println();
	}
}
