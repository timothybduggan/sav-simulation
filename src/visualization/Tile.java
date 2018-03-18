package visualization;

import java.awt.Point;
import java.io.Serializable;
import java.awt.Color;

import javafx.scene.canvas.GraphicsContext;

@SuppressWarnings("serial")
public class Tile extends PaintObject implements Serializable {
	private Color color;
	private Point topLeft;
	private int sideLength;
	private String value;
	
	public Tile(Color color, Point a, int value, int sideLength) {
		super(a, new Point(a.x+sideLength, a.y+sideLength));
		this.color = color;
		this.value = "" + value;
		this.sideLength = sideLength;
	}

	public Tile (Color color, Point a, String value, int sideLength) {
		super(a, new Point(a.x+sideLength, a.y+sideLength));
		this.color = color;
		this.value = value;
		this.sideLength = sideLength;
	}
	
	public void draw(GraphicsContext gc) {
		if (sideLength <= 4) return;
		gc.setFill(ColorTypeConverter.Awt2Fx(Color.BLACK));
		gc.fillRect(p1.x, p1.y, sideLength, sideLength);
		gc.setFill(ColorTypeConverter.Awt2Fx(Color.WHITE));
		gc.fillRect(p1.x+2, p1.y+2, sideLength-4, sideLength-4);
		gc.setFill(ColorTypeConverter.Awt2Fx(color));;
		gc.fillRect(p1.x+2, p1.y+2, sideLength-4, sideLength-4);
		gc.setFill(ColorTypeConverter.Awt2Fx(Color.BLACK));
		gc.strokeText(value, p1.x+5, p1.y+sideLength*2/3);
	}
	
	public void setPoints(Point a) {
		this.p1 = a;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setValue(int value) {
		this.value = "" + value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void setSideLength(int sl) {
		this.sideLength = sl;
	}
}
