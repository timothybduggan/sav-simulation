import java.awt.Point;

public class Trip {
	private Point origin;
	private Point destination;
	private int waitTime; // timeSteps Waited?
	private Vehicle assignedSAV;
	
	
	public Trip(Point origin, Point destination, Vehicle assignedSAV) {
		this.origin = origin;
		this.destination = destination;
		this.waitTime = 0;
		this.assignedSAV = assignedSAV;
	}
}
