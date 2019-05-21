package de.kobair.videodebrief.ui.generics;

public class LoadedController<UIClass, ControllerClass> {

	private final UIClass ui;
	private final ControllerClass controller;

	public LoadedController(final UIClass ui, final ControllerClass controller) {
		this.ui = ui;
		this.controller = controller;
	}

	public UIClass getUi() {
		return ui;
	}

	public ControllerClass getController() {
		return controller;
	}
}
