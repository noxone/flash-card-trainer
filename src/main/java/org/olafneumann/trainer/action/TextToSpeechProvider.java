package org.olafneumann.trainer.action;

import java.util.Locale;

public interface TextToSpeechProvider {
	boolean say(Locale locale, String text);
}
