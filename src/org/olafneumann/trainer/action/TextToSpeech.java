package org.olafneumann.trainer.action;

import java.io.InputStream;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public enum TextToSpeech {
	Google(new GoogleTextToSpeechProvider()), //
	;

	private final TextToSpeechProvider textToSpeech;

	private TextToSpeech(TextToSpeechProvider textToSpeech) {
		this.textToSpeech = textToSpeech;
	}

	public TextToSpeechProvider getTextToSpeechProvider() {
		return textToSpeech;
	}

	static void playMp3(InputStream in) {
		Player player = null;
		try {
			player = new Player(in);
			player.play();
		} catch (JavaLayerException e1) {
			e1.printStackTrace();
		} finally {
			if (player != null) {
				player.close();
			}
		}
	}
}
