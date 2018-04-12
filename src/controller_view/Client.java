package controller_view;

import java.awt.Point;
import java.util.Vector;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
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
	public static final int height = 850;
	private MenuBar menuBar;
	// DrawingView Instance Variables
	private Vector<PaintObject> allPaintObjects;
	private PaintObject currentPaintObject;
	private Canvas canvas;
	private GraphicsContext gc;
	private int sl = 20;
	private Simulation sim;
	private int[][] grid;
	private View view = View.VehicleCount;
	private Menu time = new Menu("null");
	private Menu dist = new Menu("null");
	
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
		updateGridVehicles();
		
//		refresher.setCycleCount(Timeline.INDEFINITE);
//		refresher.play();
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	// Sets up the menu at the top of the screen
	private void setupMenus() {
		MenuItem help = new MenuItem("Help");
		Menu views = new Menu("Views");
		MenuItem vc = new MenuItem("Vehicle Count");
		MenuItem gr = new MenuItem("Generation Rates");
		MenuItem wl = new MenuItem("Wait List Map");
		MenuItem cd = new MenuItem("Total Trip Requests");
		Menu scenarios = new Menu("Scenarios");
		MenuItem def = new MenuItem("Default");
		MenuItem r3 = new MenuItem("R3 Demo");
		MenuItem r4 = new MenuItem("R4 Demo");
		Menu options = new Menu("Options");
		
		help.setOnAction(event -> {
			Alert helpWindow = new HelpWindow();
			helpWindow.showAndWait();
		});
		vc.setOnAction(event -> {
			view = View.VehicleCount;
			updateGridVehicles();
		});
		gr.setOnAction(event -> {
			view = View.GenerationRates;
			updateGridGeneration();
		});
		wl.setOnAction(event -> {
			view = View.WaitListMap;
			updateGridWaitList();
		});
		cd.setOnAction(event -> {
			view = View.CurrentDemand;
			updateGridNewTrips();
		});
		def.setOnAction(event -> {
			view = View.VehicleCount;
			sim.initializeDefault(true);
			updateGridVehicles();
		});
		r3.setOnAction(event -> {
			view = View.VehicleCount;
			sim.initializeScenario(3);
			updateGridVehicles();
		});
		r4.setOnAction(event -> {
			view = View.VehicleCount;
			sim.initializeScenario(4);
			updateGridVehicles();
					
		});

		
		views.getItems().addAll(vc, gr, wl, cd);
		scenarios.getItems().addAll(def, r3, r4);
		options.getItems().addAll(views, help);

		menuBar = new MenuBar();
		menuBar.getMenus().addAll(options, scenarios, time, dist);
	}

	// Initializes the Canvas (for drawing purposes)
	private void initializeCanvas() {
		canvas = new Canvas(width, height);
		MouseListener mouseListener = new MouseListener();
		canvas.setOnMouseClicked(mouseListener);
		gc = canvas.getGraphicsContext2D();
		resetCanvas();
		drawingView.setCenter(canvas);
		allPaintObjects = new Vector<PaintObject>();
		currentPaintObject = new Line(ColorTypeConverter.Fx2Awt(Color.BLACK), new Point(0, 0), new Point(0, 0));
	}

	// Initializes the Control Bar (for changing drawing object, color, etc)
	
	private void initializeSimulation() {
		sim = new Simulation(false);
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
	
	private void updateGridVehicles() {
		int i = 0;
		grid = sim.getVehicleCount();
		
		for (PaintObject po : allPaintObjects) {
			((Tile) po).setValue(grid[i%40][i/40]);
//			System.out.println("("+i%40+","+i/40+") = "+grid[i%40][i/40]);
			((Tile) po).setColor(ColorTypeConverter.Fx2Awt(Color.rgb(255,0,0,Math.min(grid[i%40][i/40]/(sim.getNumVehicles() / 80.0), 1))));
			((Tile) po).setSideLength(20);
			
			i++;
		}
		
		this.resetCanvas();
		this.drawAllPaintObjects();
	}
	
	private void updateGridGeneration() {
		int i = 0;
		
		grid = sim.getGenerationRates();
		
		for (PaintObject po : allPaintObjects) {
			((Tile) po).setValue("");
			((Tile) po).setColor(ColorTypeConverter.Fx2Awt(Color.rgb(0, 0, 255,grid[i%40][i/40]/105.0)));
			((Tile) po).setSideLength(20);
			
			i++;
		}
		
		this.resetCanvas();
		this.drawAllPaintObjects();
	}
	
	private void updateGridWaitList() {
		int i = 0;
		
		grid = sim.getWaitListMap();
		
		for (PaintObject po : allPaintObjects) {
			((Tile) po).setValue(grid[i%40][i/40]);
			((Tile) po).setColor(ColorTypeConverter.Fx2Awt(Color.rgb(0, 0, 255, Math.min(1, grid[i%40][i/40]/5.0))));
			((Tile) po).setSideLength(20);
			
			i++;
		}
		
		this.resetCanvas();
		this.drawAllPaintObjects();
	}
	
	private void updateGridNewTrips() {
		int i = 0;
		
		grid = sim.getTotalTripRequests();
		
		for (PaintObject po : allPaintObjects) {
			((Tile) po).setValue(grid[i%40][i/40]);
			((Tile) po).setColor(ColorTypeConverter.Fx2Awt(Color.rgb(0, 255, 0, Math.min(1, grid[i%40][i/40]/100.0))));
			((Tile) po).setSideLength(20);
			
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
	
	
	/**
	 * This is used for regularly updating the shown Queue.
	 */
//	private Timeline refresher = new Timeline(new KeyFrame(Duration.seconds(.15), new EventHandler<ActionEvent>() {
//		@Override
//		public void handle(ActionEvent event) {
////			updateSimulation();
////			updateTime();
////			updateDistance();
//		}
//	}));
	
	private void updateTime() {
		String time = sim.getTime();
		if (sim.isPeak()) time += " PEAK. ";
		else time += " NOT PEAK. ";
		time += "Avg wait (sec): ";
		time += String.format("%.2f", sim.getAverageWaitTime() * 60);
		this.time.setText(time);
	}
	
	private void updateDistance() {
		dist.setText(String.format("%.2f miles travelled. %.2f %% induced.", sim.getDistance(), sim.getInducedTravel()));
	}
	
	private void updateSimulation() {
		if (!automate) return;
		
//		for (int i = 0; i < 288; i++) {
			sim.updateSimulation();
//		}
		
		switch(view) {
		case VehicleCount:
			updateGridVehicles();
			break;
		case GenerationRates:
			updateGridGeneration();
			break;
		case WaitListMap:
			updateGridWaitList();
			break;
		case CurrentDemand:
			updateGridNewTrips();
			break;
		}
	}
	
	private boolean automate = false;
	
	private class MouseListener implements EventHandler<MouseEvent> {
		
		
		
		@Override
		public void handle(MouseEvent event) {
			if (event.getEventType() == MouseEvent.MOUSE_CLICKED)
				handleClick();
		}
		
		void handleClick() {
//			automate = !automate;
			automate = true;
			updateSimulation();
			updateTime();
			updateDistance();
		}
	}
}