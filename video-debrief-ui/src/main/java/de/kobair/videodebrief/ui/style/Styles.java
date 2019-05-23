package de.kobair.videodebrief.ui.style;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.Scene;

public class Styles {

	private static final List<String> STYLE_RESOURCES = Arrays.asList(
			"components.css",
			"badges.css"
	);

	public enum ColorTheme {
		LIGHT("light.css"),
		DARK("dark.css");

		private final String resource;

		ColorTheme(String resource) {
			this.resource = resource;
		}

		String getResource() {
			return resource;
		}
	}

	public static void applyStyle(Scene scene, ColorTheme colorTheme) {
		List<String> resourceUrls = STYLE_RESOURCES.stream().map(Styles::externalResourceUrl).collect(Collectors.toList());
		resourceUrls.add(colorThemeUrl(colorTheme));

		scene.getStylesheets().addAll(resourceUrls);
	}

	private static String externalResourceUrl(String resource) {
		return Styles.class.getResource(resource).toExternalForm();
	}

	private static String colorThemeUrl(ColorTheme colorTheme) {
		String resource = colorTheme.getResource();
		return externalResourceUrl(resource);
	}
}
