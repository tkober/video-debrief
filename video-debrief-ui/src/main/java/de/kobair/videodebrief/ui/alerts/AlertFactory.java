package de.kobair.videodebrief.ui.alerts;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.kobair.videodebrief.ui.errors.ApplicationError;
import de.kobair.videodebrief.ui.errors.ApplicationException;
import de.kobair.videodebrief.ui.errors.ApplicationFatal;
import de.kobair.videodebrief.ui.errors.ApplicationWarning;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class AlertFactory {

	private static Alert stacktraceAlert(Alert.AlertType type, String title, Throwable cause) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setContentText(cause.getMessage());

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		cause.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("Details:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		return alert;
	}

	public static Alert alertForApplicationException(ApplicationException applicationException) {
		if (applicationException instanceof ApplicationWarning) {
			return warningDialog((ApplicationWarning) applicationException);
		}

		if (applicationException instanceof ApplicationError) {
			return errorDialog((ApplicationError) applicationException);
		}

		if (applicationException instanceof ApplicationFatal) {
			return fatalDialog((ApplicationFatal) applicationException);
		}

		return null;
	}

	public static Alert warningDialog(ApplicationWarning warning) {
		Throwable cause = warning.getCause();

		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Warning");
		alert.setContentText(warning.getCause().getMessage());

		return alert;
	}

	public static Alert errorDialog(ApplicationError error) {
		return stacktraceAlert(Alert.AlertType.ERROR, "Error", error.getCause());
	}

	public static Alert fatalDialog(ApplicationFatal fatal) {
		return stacktraceAlert(Alert.AlertType.ERROR, "Fatal Error", fatal.getCause());
	}

}
