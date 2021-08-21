package org.olafneumann.trainer.action;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.olafneumann.trainer.data.TrainerModelInput;
import org.olafneumann.trainer.ui.Icons;
import org.olafneumann.trainer.ui.LinkRunner;
import org.olafneumann.trainer.ui.Messages;

public class UrlAction implements TrainerModelInputAction {
	private final String websiteName;
	private final Pattern[] patterns;
	private final String url;
	private final ImageIcon icon;

	public UrlAction(final String name, final Pattern[] patterns, final String url, final String iconName) {
		this(name, patterns, url, Icons.valueOf(iconName));
	}

	public UrlAction(final String name, final Pattern[] patterns, final String url, final Icons icon) {
		this.websiteName = name;
		this.patterns = patterns != null ? patterns : new Pattern[0];
		this.url = url;
		this.icon = icon.getImageIcon();
	}

	@Override
	public ImageIcon getIcon() {
		return icon;
	}

	@Override
	public List<String> getTexts(final String inputText) {
		final Collection<String> texts = new LinkedHashSet<>();
		for (final Pattern pattern : patterns) {
			final Matcher matcher = pattern.matcher(inputText);
			while (matcher.find()) {
				for (int i = 0; i < matcher.groupCount(); ++i) {
					texts.add(matcher.group(i + 1).trim());
				}
			}
		}
		return new ArrayList<>(texts);
	}

	@Override
	public void performInputAction(final TrainerModelInput input, String text) {
		try {
			text = URLEncoder.encode(text, "utf-8"); //$NON-NLS-1$
			final URL url = new URL(this.url.replace("$(search)", text)); //$NON-NLS-1$
			LinkRunner.open(url.toURI());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getActionTooltip() {
		return String.format(Messages.getString("UrlAction.SearchVocab"), websiteName); //$NON-NLS-1$
	}
}
