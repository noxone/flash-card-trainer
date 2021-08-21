package org.olafneumann.trainer.data;

import java.awt.Font;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.text.Document;

import org.olafneumann.trainer.action.TrainerModelInputAction;

public interface TrainerModelInput {
	/**
	 * Returns the name of the current input. This name will be shown when entering
	 * data into the database or when displaying content for training.
	 *
	 * @return the name of the current input
	 */
	String getName();

	/**
	 * Returns the {@link Font} to be used be the title component when entering
	 * data.
	 *
	 * @return the font for the title
	 */
	Lettering getTitleLettering();

	/**
	 * Returns the {@link Font} to be used by the text control when entering data
	 * into the database.
	 *
	 * @return input control {@link Font}
	 */
	Lettering getInputLettering();

	/**
	 * Returns the font to be used when data from this input should be printed
	 *
	 * @return print {@link Font}
	 */
	Lettering getPrintLettering();

	/**
	 * Returns the font to be used in the list
	 *
	 * @return list {@link Font}
	 */
	Lettering getListLettering();

	/**
	 * Returns the locale of the current input
	 *
	 * @return the locale of the current input
	 */
	Locale getLocale();

	/**
	 * Returns a document to be used in the training input
	 *
	 * @return a document to be used in the training input
	 */
	Document getDocument();

	/**
	 * Returns a pattern that can be used to determine, which part of the entered
	 * text can be used as a link
	 *
	 * @return the pattern
	 */
	Pattern getLinkPattern();

	/**
	 * Returns a link to be started when the user clicked on a pattern part
	 *
	 * @param search the pattern part the user clicked at
	 * @return the URL to be opened in a browser
	 */
	String getSearchUrl(String search);

	TrainerModelInputAction[] getTrainerModelInputActions();
}
