package visualization;

import java.awt.Point;
import java.io.Serializable;
import java.awt.Color;

import javafx.scene.canvas.GraphicsContext;
import visualization.ColorTypeConverter;
import visualization.PaintObject;

/**
 * Line
 * 
 * <p>
 * A Line is a concrete class extending PaintObject defining a shape (line) that
 * can be drawn with two points, and a given color.
 * <p>
 * 
 * @author duggan
 *
 */

@SuppressWarnings("serial")
public class Line extends PaintObject implements Serializable {
	private Color color;

	public Line(Color color, Point a, Point b) {
		super(a, b);
		this.color = color;
	}

	public void draw(GraphicsContext gc) {
		gc.setStroke(ColorTypeConverter.Awt2Fx(color));
		gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
}
