package controller_view;

import java.awt.Point;
import java.util.Vector;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import visualization.ColorTypeConverter;
import visualization.Line;
import visualization.PaintObject;
import visualization.Tile;
import model.Simulation;

/**
 * A JPanel GUI for Netpaint that has all paint objects drawn on it. This file
 * also represents the controller as it controls how paint objects are drawn and
 * sends new paint objects to the server. All Client objects also listen to the
 * server to read the Vector of PaintObjects and repaint every time any client
 * adds a new one.
 * 
 * @author duggan
 * 
 */
public class Client extends Application {

	// Main Window Instance Variables
	private BorderPane window;
	private BorderPane drawingView;
	public static final int width = 800;
	public static final int height = 800;
	private MenuBar menuBar;
	// DrawingView Instance Variables
	private Vector<PaintObject> allPaintObjects;
	private PaintObject currentPaintObject;
	private Canvas canvas;
	private GraphicsContext gc;
	private int sl = 20;
	private Simulation sim;
	private int[][] grid;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		window = new BorderPane();
		primaryStage.setTitle("SAV-simulation");
		Scene scene = new Scene(window, width, height);
		setupMenus();
		window.setTop(menuBar);

		// New Initialize Drawing View

		drawingView = new BorderPane();
		initializeCanvas();
		//initializeControlBar();
		window.setCenter(drawingView);
		
		// End Initialize Drawing View
		
		initializeSimulation();
		initializeGrid();
		updateGrid();
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	// Sets up the menu at the top of the screen
	private void setupMenus() {
		MenuItem help = new MenuItem("Help");
		Menu options = new Menu("Options");
		help.setOnAction(event -> {
			Alert helpWindow = new HelpWindow();
			helpWindow.showAndWait();
		});

		options.getItems().addAll(help);

		menuBar = new MenuBar();
		menuBar.getMenus().addAll(options);
	}

	// Initializes the Canvas (for drawing purposes)
	private void initializeCanvas() {
		canvas = new Canvas(width, height);
		gc = canvas.getGraphicsContext2D();
		resetCanvas();
		drawingView.setCenter(canvas);
		allPaintObjects = new Vector<PaintObject>();
		currentPaintObject = new Line(ColorTypeConverter.Fx2Awt(Color.BLACK), new Point(0, 0), new Point(0, 0));
	}

	// Initializes the Control Bar (for changing drawing object, color, etc)
	
	private void initializeSimulation() {
		sim = new Simulation();
		grid = sim.getVehicleCount();
	}

	private void initializeGrid() {
		for (int i = 0; i < 40; i++) {
			for (int j = 0; j < 40; j++) {
				int value = i*j;
				allPaintObjects.add(new Tile(ColorTypeConverter.Fx2Awt(Color.rgb(255,0,0,value/1600.0)), new Point(i*sl,j*sl), value, sl));
			}
		}
		drawAllPaintObjects();
	}
	
	private void updateGrid() {
		int i = 0;
		for (PaintObject po : allPaintObjects) {
			((Tile) po).setValue(grid[i%40][i/40]);
			System.out.println("("+i%40+","+i/40+") = "+grid[i%40][i/40]);
			((Tile) po).setColor(ColorTypeConverter.Fx2Awt(Color.rgb(255,0,0,grid[i%40][i/40]/2.0)));
			i++;
		}
		
		this.resetCanvas();
		this.drawAllPaintObjects();
	}
	
	// Draws all paint objects (in vector), including currentPaintObject
	private void drawAllPaintObjects() {
		resetCanvas();
		for (PaintObject po : allPaintObjects) {
			po.draw(gc);
		}
		currentPaintObject.draw(gc);
	}

	// Resets the canvas (white screen)
	private void resetCanvas() {
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, width, height);
	}
}