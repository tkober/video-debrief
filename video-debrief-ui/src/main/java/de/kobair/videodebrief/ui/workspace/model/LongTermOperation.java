package de.kobair.videodebrief.ui.workspace.model;

import de.kobair.videodebrief.core.operations.Operation;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LongTermOperation implements Operation {

	public StringProperty nameProperty;
	public StringProperty currentStepProperty;
	public IntegerProperty progressProperty;

	public LongTermOperation(String name, String currentStep) {
		this(name, currentStep, -1);
	}

	public LongTermOperation(String name, String currentStep, int progress) {
		this.nameProperty = new SimpleStringProperty(name);
		this.currentStepProperty = new SimpleStringProperty(currentStep);
		this.progressProperty = new SimpleIntegerProperty(progress);
	}

	@Override
	public void updateCurrentStep(final String currentStep) {
		this.currentStepProperty.set(currentStep);
	}

	@Override
	public void updateProgress(final int progress) {
		this.progressProperty.set(progress);
	}
}
