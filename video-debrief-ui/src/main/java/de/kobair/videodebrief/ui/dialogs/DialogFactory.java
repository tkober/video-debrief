package de.kobair.videodebrief.ui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.video.VideoInformation;
import de.kobair.videodebrief.ui.playback.model.AttributedPerspective;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class DialogFactory {

	private static abstract class SummaryItem {
		abstract void renderInGrid(GridPane gridPane, int rowIndex);
	}

	private static class KeyValuePair extends SummaryItem {

		private static final Insets INSETS = new Insets(0, 0, 0, 8);

		private final String key;
		private final Object value;

		private KeyValuePair(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		void renderInGrid(GridPane gridPane, int rowIndex) {
			Label keyLabel = new Label(key+":");
			gridPane.add(keyLabel, 0, rowIndex);

			Label valueLabel = new Label(value.toString());
			GridPane.setMargin(valueLabel, INSETS);
			gridPane.add(valueLabel, 1, rowIndex);
		}
	}

	private static class SectionHeader extends SummaryItem {
		private static final Insets INSETS = new Insets(10, 0, 0, 0);

		private final String title;

		private SectionHeader(String title) {
			this.title = title;
		}

		@Override
		void renderInGrid(GridPane gridPane, int rowIndex) {
			Label titleLabel = new Label(title);
			GridPane.setMargin(titleLabel, INSETS);
			gridPane.add(titleLabel, 0, rowIndex, 2, 1);
		}
	}

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
	public static DialogConfig CHOOSE_PERSPECTIVES_CONFIG  = new DialogConfig(StageStyle.UTILITY, null, "Additional Perspectives", "Choose additional perspectives to export:", null);
	public static DialogConfig VIDEO_INFORMATION_CONFIG = new DialogConfig();

	private static String durationString(long durationMillis) {
		return DurationFormatUtils.formatDuration(durationMillis, "HH:mm:ss.SSS");
	}

	public static TextInputDialog namingDialog(String title, String placeholder) {
		return textInputDialog(title, placeholder, NAMING_CONFIG);
	}

	public static  TextInputDialog importMediaDialog(Event event, File file, String perspectiveName) {
		String placeholder = perspectiveName != null && !perspectiveName.trim().isEmpty() ? perspectiveName : "New Event";
		return textInputDialog("Create Perspective", perspectiveName, IMPORT_MEDIA_CONFIG);
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

	public static Dialog<Map<AttributedPerspective, Boolean>> chooseAdditionalPerspectivesDialog(List<AttributedPerspective> additionalPerspectives) {
		return multiSelectDialog(additionalPerspectives, CHOOSE_PERSPECTIVES_CONFIG);
	}

	public static <T extends Object> Dialog<Map<T, Boolean>> multiSelectDialog(List<T> options, DialogConfig config) {
		Dialog<Map<T, Boolean>> dialog = new Dialog<>();
		config.apply(dialog);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);

		Map<T, CheckBox> optionsToCheckboxes = new HashMap<>(options.size());
		for (int i = 0; i < options.size(); i++) {
			final T option = options.get(i);
			CheckBox checkBox = new CheckBox(option.toString());
			optionsToCheckboxes.put(option, checkBox);
			grid.add(checkBox, 0, i);
		}
		dialog.getDialogPane().setContent(grid);

		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
				Map<T, Boolean> result = new HashMap<>(optionsToCheckboxes.size());
				for (T option : options) {
					CheckBox checkBox = optionsToCheckboxes.get(option);
					result.put(option, checkBox.isSelected());
				}
				return result;
			}
			return null;
		});

		return dialog;
	}

	public static Dialog<ButtonType> videoInformationDialog(VideoInformation videoInformation, Perspective perspective) {
		String title = perspective.getName() + " - Details";
		String detailsText = videoInformation.getRawInformationAsString();

		List<SummaryItem> summaryItems = new ArrayList<>();

		summaryItems.add(new SectionHeader("General"));
		summaryItems.add(new KeyValuePair("Format", videoInformation.getFormatName()));
		String duration = durationString(videoInformation.getDurationMillis());
		summaryItems.add(new KeyValuePair("Duration", duration));
		String fileSize = LocalUtils.smartFileSizeString(videoInformation.getSizeInBytes());
		summaryItems.add(new KeyValuePair("Size", fileSize));

		String overallBitrate = String.format("%s kBit/s", videoInformation.getBitRate() / 1000);
		summaryItems.add(new KeyValuePair("Bitrate", overallBitrate));

		if (videoInformation.getVideoInformation().size() > 0) {
			VideoInformation.VideoTrack videoTrack = videoInformation.getVideoInformation().get(0);
			summaryItems.add(new SectionHeader("Video"));

			summaryItems.add(new KeyValuePair("Codec", videoTrack.getCodecName()));

			String resolution = String.format("%s x %s", videoTrack.getWidth(), videoTrack.getHeight());
			summaryItems.add(new KeyValuePair("Resolution", resolution));

			summaryItems.add(new KeyValuePair("Aspect Ratio", videoTrack.getDisplayAspectRatio()));

			String fps = String.format("%.2f fps", videoTrack.getFramesPerSecond());
			summaryItems.add(new KeyValuePair("Framerate", fps));

			String bitrate = String.format("%s kBit/s", videoTrack.getBitRate() / 1000);
			summaryItems.add(new KeyValuePair("Bitrate", bitrate));
		}

		if (videoInformation.getAudioInformation().size() > 0) {
			VideoInformation.AudioTrack audioTrack = videoInformation.getAudioInformation().get(0);
			summaryItems.add(new SectionHeader("Audio"));

			summaryItems.add(new KeyValuePair("Codec", audioTrack.getCodecName()));

			String bitrate = String.format("%s kBit/s", audioTrack.getBitRate() / 1000);
			summaryItems.add(new KeyValuePair("Bitrate", bitrate));

			String channels = String.format("%s (%s)", audioTrack.getNumberOfChannels(), audioTrack.getChannelLayout());
			summaryItems.add(new KeyValuePair("Channels", channels));

			String sampleRate = String.format("%s Hz", audioTrack.getSampleRate());
			summaryItems.add(new KeyValuePair("Sample Rate", sampleRate));
		}

		return expandableDetailsDialog(title, summaryItems, detailsText, VIDEO_INFORMATION_CONFIG);
	}

	public static Dialog<ButtonType> expandableDetailsDialog(String title, List<SummaryItem> summaryItems, String detailsText, DialogConfig config) {
		Dialog<ButtonType> dialog = new Dialog<>();
		config.apply(dialog);
		dialog.setTitle(title);

		GridPane summaryContent = new GridPane();
		summaryContent.setMaxWidth(Double.MAX_VALUE);
		int i = 0;
		for (SummaryItem item : summaryItems) {
			item.renderInGrid(summaryContent, i);
			i++;
		}

		Label label = new Label("Details:");

		TextArea textArea = new TextArea(detailsText);
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
		dialog.getDialogPane().setContent(summaryContent);
		dialog.getDialogPane().setExpandableContent(expContent);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

		return dialog;
	}
}
