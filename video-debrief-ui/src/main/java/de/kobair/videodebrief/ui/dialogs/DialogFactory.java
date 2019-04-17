package de.kobair.videodebrief.ui.dialogs;

import java.io.File;

import de.kobair.videodebrief.core.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.StageStyle;

public class DialogFactory {

	public static class DialogConfig {

		protected final StageStyle initStyle;
		protected final Node graphic;
		protected final String titleText;
		protected final String headerText;
		protected final String contentText;

		public DialogConfig(StageStyle initStyle, Node graphic, String titleText, String headerText, String contentText) {
			this.initStyle = initStyle;
			this.graphic = graphic;
			this.titleText = titleText;
			this.headerText = headerText;
			this.contentText = contentText;
		}

		public DialogConfig() {
			this(StageStyle.UTILITY, null, null, null, null);
		}

		protected void apply(Dialog dialog) {
			dialog.setGraphic(this.graphic);
			dialog.initStyle(this.initStyle);
			dialog.setTitle(this.titleText);
			dialog.setHeaderText(this.headerText);
			dialog.setContentText(this.contentText);
		}
	}


	public static DialogConfig NAMING_CONFIG  = new DialogConfig(StageStyle.UTILITY, null, null, null, "Name:");
	public static DialogConfig IMPORT_MEDIA_CONFIG  = new DialogConfig(StageStyle.UTILITY, null, null, null, "Perspective:");
	public static DialogConfig DELETION_CONFIG  = new DialogConfig();

	public static TextInputDialog namingDialog(String title, String placeholder) {
		return textInputDialog(title, placeholder, NAMING_CONFIG);
	}

	public static  TextInputDialog importMediaDialog(Event event, File file, String perspectiveName) {
		String placeholder = perspectiveName != null && !perspectiveName.trim().isEmpty() ? perspectiveName : "New Event";
		return textInputDialog("Create Perspective", perspectiveName, NAMING_CONFIG);
	}

	public static TextInputDialog textInputDialog(String title, String placeholder, DialogConfig config) {
		TextInputDialog dialog = new TextInputDialog(placeholder);
		config.apply(dialog);
		dialog.setTitle(title);

		return dialog;
	}

	public static Dialog<ButtonType> confirmDeleteDialog(String title, String question) {
		Alert result = confirmationDialog(title, question, DELETION_CONFIG);

		ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
		ButtonType delete = new ButtonType("Delete", ButtonBar.ButtonData.YES);
		ButtonType deleteKeepFiles = new ButtonType("Delete, keep files", ButtonBar.ButtonData.OTHER);

		result.getButtonTypes().setAll(delete, deleteKeepFiles, cancel);
		return result;
	}

	public static Alert confirmationDialog(String title, String question, DialogConfig config) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		config.apply(alert);
		alert.setTitle(title);
		alert.setContentText(question);

		return alert;
	}
}
