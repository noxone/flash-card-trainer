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

	public UrlAction(String name, Pattern[] patterns, String url, String iconName) {
		this(name, patterns, url, Icons.valueOf(iconName));
	}

	public UrlAction(String name, Pattern[] patterns, String url, Icons icon) {
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
	public List<String> getTexts(String inputText) {
		Collection<String> texts = new LinkedHashSet<String>();
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(inputText);
			while (matcher.find()) {
				for (int i = 0; i < matcher.groupCount(); ++i) {
					texts.add(matcher.group(i + 1).trim());
				}
			}
		}
		return new ArrayList<String>(texts);
	}

	@Override
	public void performInputAction(TrainerModelInput input, String text) {
		try {
			text = URLEncoder.encode(text, "utf-8"); //$NON-NLS-1$
			URL url = new URL(this.url.replace("$(search)", text)); //$NON-NLS-1$
			LinkRunner.open(url.toURI());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getActionTooltip() {
		return String.format(Messages.getString("UrlAction.SearchVocab"), websiteName); //$NON-NLS-1$
	}
}
