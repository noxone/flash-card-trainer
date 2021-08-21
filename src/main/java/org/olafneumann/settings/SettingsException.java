package org.olafneumann.settings;

public class SettingsException extends Exception {
	private static final long serialVersionUID = -5301857866477349283L;

	public SettingsException(final String message) {
		super(message);
	}

	public SettingsException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
