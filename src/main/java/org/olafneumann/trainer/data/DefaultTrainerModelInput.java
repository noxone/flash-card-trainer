package org.olafneumann.trainer.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.text.Document;

import org.olafneumann.trainer.action.TrainerModelInputAction;

public class DefaultTrainerModelInput implements TrainerModelInput {
	private static final String LINK_SEARCH = "$(search)";

	private final String name;
	private final Lettering titleFont;
	private final Lettering inputFont;
	private final Lettering printFont;
	private final Lettering listFont;
	private final Class<Document> documentClass;
	private final Locale locale;
	private final Pattern linkPattern;
	private final String searchLink;
	private final Collection<TrainerModelInputAction> actions;

	protected DefaultTrainerModelInput(final String name, final Lettering titleFont, final Lettering inputFont,
			final Lettering printFont, final Lettering listFont, final Class<Document> documentClass,
			final Locale locale, final Pattern linkPattern, final String searchLink,
			final Collection<TrainerModelInputAction> actions) {
		this.name = name;
		this.titleFont = titleFont;
		this.inputFont = inputFont;
		this.printFont = printFont;
		this.listFont = listFont;
		this.documentClass = documentClass;
		this.locale = locale;
		this.linkPattern = linkPattern;
		this.searchLink = searchLink;
		this.actions = actions;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Lettering getTitleLettering() {
		return titleFont;
	}

	@Override
	public Lettering getInputLettering() {
		return inputFont;
	}

	@Override
	public Lettering getPrintLettering() {
		return printFont;
	}

	@Override
	public Lettering getListLettering() {
		return listFont;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public Document getDocument() {
		if (documentClass != null) {
			try {
				return documentClass.newInstance();
			} catch (final Exception e) {
			}
		}
		return null;
	}

	@Override
	public Pattern getLinkPattern() {
		return linkPattern;
	}

	@Override
	public String getSearchUrl(String search) {
		try {
			search = URLEncoder.encode(search, "utf-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("Your system does not support UTF-8. How is it even able to run?", e);
		}
		if (searchLink != null && searchLink.trim().length() > 0) {
			return searchLink.replace(LINK_SEARCH, search);
		}
		return search;
	}

	@Override
	public TrainerModelInputAction[] getTrainerModelInputActions() {
		return actions.toArray(new TrainerModelInputAction[0]);
	}
}
