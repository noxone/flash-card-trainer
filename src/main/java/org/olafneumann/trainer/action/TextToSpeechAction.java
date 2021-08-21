package org.olafneumann.trainer.action;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.olafneumann.trainer.data.TrainerModelInput;
import org.olafneumann.trainer.ui.Icons;
import org.olafneumann.trainer.ui.Messages;

public class TextToSpeechAction implements TrainerModelInputAction {
	private final TextToSpeechProvider textToSpeech;
	private final Pattern pattern;

	public TextToSpeechAction(TextToSpeechProvider textToSpeech, Pattern pattern) {
		this.textToSpeech = textToSpeech;
		this.pattern = pattern;
	}

	@Override
	public void performInputAction(final TrainerModelInput input, final String text) {
		new Thread("TextToSpeechTalker") { //$NON-NLS-1$
			@Override
			public void run() {
				textToSpeech.say(input.getLocale(), text);
			}
		}.start();
	}

	@Override
	public List<String> getTexts(String inputText) {
		if (pattern != null) {
			List<String> texts = new ArrayList<String>();
			Matcher matcher = pattern.matcher(inputText);
			while (matcher.find()) {
				for (int i = 0; i < matcher.groupCount(); ++i) {
					texts.add(matcher.group(i + 1).trim());
				}
			}
			return texts;
		} else {
			return null;
		}
	}

	@Override
	public ImageIcon getIcon() {
		return Icons.PLAY.getImageIcon();
	}

	@Override
	public String getActionTooltip() {
		return Messages.getString("TextToSpeechAction.SpeekCurrentText"); //$NON-NLS-1$
	}
}
