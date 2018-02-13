import java.awt.Point;

import org.junit.Test;

public class test {

	@Test
	public void TripGenerationTest() {
		TripGeneration generator = new TripGeneration();
		Point start = new Point(15,15);
		Point dest  = generator.generateTrip(start);
	}
	
}
