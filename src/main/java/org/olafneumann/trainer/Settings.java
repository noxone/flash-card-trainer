package org.olafneumann.trainer;

import java.util.Properties;

public class Settings {
	private static final String DEFAULT_APPLICATION_TITLE = "Trainer";

	private String title = "";
	private boolean showCaptions = true;

	Settings() {
	}

	void load(final Properties properties) {
		title = properties.getProperty("application.title", DEFAULT_APPLICATION_TITLE);
		try {
			showCaptions = Boolean.parseBoolean(properties.getProperty("application.trainer.showCaptions"));
		} catch (final Exception e) {
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public boolean isShowCaptions() {
		return showCaptions;
	}

	public void setShowCaptions(final boolean showCaptions) {
		this.showCaptions = showCaptions;
	}
}
