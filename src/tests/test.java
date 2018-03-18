package tests;
import static org.junit.Assert.*;

import java.awt.Point;

import org.junit.Test;

import model.Simulation;
import model.Trip;
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
		
		assertEquals(a.getNumTrips(), 0);
		assertEquals(a.getPosition().x, 38);
		assertEquals(a.getPosition().y, 37);
		assertEquals(a.getState(), Vehicle_State.on_trip);
		assertFalse(t.isPickedUp());
		assertFalse(t.isFinished());
		assertEquals(t.getWaitTime(), 0, .001);
		t.update();
		assertEquals(a.getNumTrips(), 1);
		assertEquals(a.getPosition().x, 34);
		assertEquals(a.getPosition().y, 40);
		assertEquals(a.getState(), Vehicle_State.on_trip);
		assertTrue(t.isPickedUp());
		assertFalse(t.isFinished());
		assertEquals(t.getWaitTime(), 2.273, .001);
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
	public void SimulationTest1() {
		Simulation sim = new Simulation();
	}
}
