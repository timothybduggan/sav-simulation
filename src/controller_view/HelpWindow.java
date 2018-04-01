package controller_view;

import javafx.scene.control.Alert;

/**
 * This is an Alert for our HelpBox Dialogue. It gives information on how to use
 * the NetPaintFX Application.
 * 
 * @author duggan
 *
 */

public class HelpWindow extends Alert {
	public HelpWindow() {
		super(AlertType.INFORMATION);
		addHelpText();
		this.setTitle("Help");
	}

	// Sets this alert's text to our helpWindow text.
	private void addHelpText() {
		this.setHeaderText("How to Use SAV-Simulation Visualizer");
		this.setContentText(
				"Click on the window to start the simulation. The simulation is moved towards the next time step "
				+ "every 0.5 seconds, so you should quickly see things changing. The views allow you to see current vehicle positions, "
				+ "relative generation rates (darker colors have higher generation rates), zones with trips currently waiting to be serviced, "
				+ "and the total number of trips that have been generated in a zone. To restart a fresh simulation, use the 'default' scenario. "
				+ "Scenario R3 demonstrates how R3 relocation works, and Scenario R4 shows how R4 relocation works.");
	}
}
