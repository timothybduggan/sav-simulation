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
		this.setHeaderText("How to Use NetPaintFX");
		this.setContentText(
				"Left click to start drawing an object. " + "Left click again to upload that object to the server. "
						+ "While drawing, you can right click to cancel the current object.");
	}
}
