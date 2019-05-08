package de.kobair.videodebrief.ui.badges;

import de.kobair.videodebrief.core.video.VideoInformation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TrackBadgeFactory {

	public static Node badgeForVideoTrack(VideoInformation.VideoTrack track) {
		final Long fps = Math.round(track.getFramesPerSecond());
		final String codec = track.getCodecName();
		final Integer hResolution = track.getHeight();

		VBox result = new VBox();
		result.getChildren().add(badgeNameView(codec));
		result.getChildren().add(badgeParanetersView(hResolution.toString(), fps.toString()));

		return result;
	}

	public static  Node badgeForAudioTrack(VideoInformation.AudioTrack track) {
		final String codec = track.getCodecName();
		final String channelLayout = track.getChannelLayout();

		VBox result = new VBox();
		result.getChildren().add(badgeNameView(codec));
		result.getChildren().add(badgeParanetersView(channelLayout));

		return result;
	}

	private static Node badgeNameView(String name) {
		Label nameLabel = new Label(name);
		HBox.setMargin(nameLabel, new Insets(0,3,0, 3));

		HBox result = new HBox();
		result.setAlignment(Pos.CENTER);
		result.getChildren().add(nameLabel);

		return result;
	}

	private static Node badgeParanetersView(String... parameters) {
		HBox result = new HBox();

		for (String parameter : parameters) {
			Node parameterView = parameterView(parameter);
			HBox.setMargin(parameterView, new Insets(0, 3, 0, 3));
			result.getChildren().add(parameterView);
		}

		return result;
	}

	private static Node parameterView(String parameter) {
		Label result = new Label(parameter);
		return result;
	}

}
