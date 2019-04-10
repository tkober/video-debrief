package de.kobair.videodebrief.core.utils;

public class EnvironmentUtils {
	
	public enum Variable {
		FFMPEG_HOME("FFMPEG_HOME");
		
		private final String name;
		
		private Variable(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public static String getEnvironmentVariable(Variable variable) {
		return System.getenv(variable.getName());
	}
	
	public static boolean environmentVariableSetAndNotEmpty(Variable variable) {
		String value = getEnvironmentVariable(variable);
		return value != null && value.trim().length() > 0;
	}
}
