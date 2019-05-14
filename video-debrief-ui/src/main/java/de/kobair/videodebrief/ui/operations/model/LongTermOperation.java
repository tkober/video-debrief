package de.kobair.videodebrief.ui.operations.model;

import de.kobair.videodebrief.core.operations.Operation;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LongTermOperation implements Operation {

	public StringProperty nameProperty;
	public StringProperty descriptionProperty;
	public DoubleProperty progressProperty;
	public IntegerProperty stepProperty;

	private final int totalSteps;

	public LongTermOperation(String name, String currentStep) {
		this(name, currentStep, -1.0, 1, 1);
	}

	public LongTermOperation(String name, String currentStep, double progress, int step, int totalSteps) {
		this.nameProperty = new SimpleStringProperty(name);
		this.descriptionProperty = new SimpleStringProperty(currentStep);
		this.progressProperty = new SimpleDoubleProperty(progress);
		this.stepProperty = new SimpleIntegerProperty(step);
		this.totalSteps = totalSteps;
	}

	public int getTotalSteps() {
		return totalSteps;
	}

	@Override
	public void updateDescription(final String desciption) {
		Platform.runLater(() -> {
			this.descriptionProperty.set(desciption);
		});
	}

	@Override
	public void updateProgress(final double progress) {
		Platform.runLater(() -> {
			this.progressProperty.set(progress);
		});
	}

	@Override
	public void updateStep(int step) {
		Platform.runLater(() -> {
			this.stepProperty.set(step);
		});
	}
}
