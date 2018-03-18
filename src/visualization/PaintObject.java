package visualization;

import java.awt.Point;
import java.io.Serializable;

import javafx.scene.canvas.GraphicsContext;

/**
 * PaintObject
 * 
 * <p>
 * A PaintObject is an abstract class defining a shape that can be drawn with
 * two points
 * <p>
 * 
 * @author duggan
 *
 */
@SuppressWarnings("serial")
public abstract class PaintObject implements Serializable {
	protected Point p1;
	protected Point p2;

	public PaintObject(Point a, Point b) {
		p1 = a;
		p2 = b;
	}

	public void setPoints(Point a, Point b) {
		this.p1 = a;
		this.p2 = b;
	}
	
	protected double getWidth() {
		return Math.abs(p1.x - p2.x);
	}

	protected double getHeight() {
		return Math.abs(p1.y - p2.y);
	}
	
	public abstract void draw(GraphicsContext gc);
}