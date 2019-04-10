package de.kobair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class ResourceLoader {
	
	public static File loadResourceAsFile(String resourceName) {
		URL url = ResourceLoader.class.getClassLoader().getResource(resourceName);
		File result = new File(url.getPath());
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	public static <T> T loadJsonResource(String resourceName, Class type) throws FileNotFoundException {
		File jsonFile = loadResourceAsFile(resourceName);
		JsonReader reader = new JsonReader(new FileReader(jsonFile));
		return new Gson().fromJson(reader, type);
	}
	
	public static JsonObject loadJsonResourceAsObject(String resourceName) throws FileNotFoundException {
		return loadJsonResource(resourceName, JsonObject.class);
	}
	
	public static JsonArray loadJsonResourceAsArray(String resourceName) throws FileNotFoundException {
		return loadJsonResource(resourceName, JsonObject.class);
	}

}
