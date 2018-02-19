import java.awt.Point;

import org.junit.Test;

public class test {

//	@Test
//	public void TripGenerationTest() {
//		TripGeneration generator = new TripGeneration();
//		Point start = new Point(15,15);
//		Point dest  = generator.generateTrip(start);
//	}
	
	@Test
	public void MovementTest() {
		Vehicle a = new Vehicle(32,27);
		a.setDestination(new Point(34,17));
		System.out.println(a.getPosition());
		a.moveTowardsDestination();
		System.out.println(a.getPosition());
		a.moveTowardsDestination();
		System.out.println(a.getPosition());
		a.moveTowardsDestination();
		System.out.println(a.getPosition());
		a.moveTowardsDestination();
		System.out.println(a.getPosition());
	}
}
