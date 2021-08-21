package org.olafneumann.settings;

public class SettingsException extends Exception {
	private static final long serialVersionUID = -5301857866477349283L;

	public SettingsException(String message) {
		super(message);
	}

	public SettingsException(String message, Throwable cause) {
		super(message, cause);
	}
}
