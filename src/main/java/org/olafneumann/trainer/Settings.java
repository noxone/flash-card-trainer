package org.olafneumann.trainer;

import java.util.Properties;

public class Settings {
	private static final String KEY_APPLICATION_TRAINER_SHOW_CAPTIONS = "application.trainer.showCaptions";
	private static final String KEY_APPLICATION_TITLE = "application.title";

	private static final String DEFAULT_APPLICATION_TITLE = "Trainer";

	private String title = DEFAULT_APPLICATION_TITLE;
	private boolean showCaptions = true;

	Settings() {
	}

	void load(final Properties properties) {
		title = properties.getProperty(KEY_APPLICATION_TITLE, title);
		try {
			showCaptions = Boolean.parseBoolean(properties.getProperty(KEY_APPLICATION_TRAINER_SHOW_CAPTIONS));
		} catch (final Exception e) {
		}
	}

	public String getTitle() {
		return title;
	}

	public boolean isShowCaptions() {
		return showCaptions;
	}
}
