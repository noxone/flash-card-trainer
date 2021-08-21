package org.olafneumann.trainer.data;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.text.Document;

import org.olafneumann.trainer.action.TextToSpeech;
import org.olafneumann.trainer.action.TextToSpeechAction;
import org.olafneumann.trainer.action.TrainerModelInputAction;
import org.olafneumann.trainer.action.UrlAction;

public class TrainerModelInputProvider {
	private TrainerModelInputProvider() {
		throw new RuntimeException();
	}

	public static List<TrainerModelInput> readTrainerModelInputs(final InputStream in) throws IOException {
		final Properties properties = new Properties();
		properties.load(in);
		return readTrainerModelInputs(properties);
	}

	public static List<TrainerModelInput> readTrainerModelInputs(final Properties properties) {
		final List<TrainerModelInput> inputs = new ArrayList<>();
		for (int i = 1;; ++i) {
			final TrainerModelInput input = createInput(properties, "input" + i);
			if (input == null) {
				break;
			}
			inputs.add(input);
		}
		return inputs;
	}

	private static TrainerModelInput createInput(final Properties properties, final String name) {
		String inputName = properties.getProperty(name + ".name");
		if ((inputName == null) || (inputName.trim().length() <= 0)) {
			return null;
		}
		inputName = inputName.trim();
		final Lettering title = readLettering(name + ".font.title.", properties);
		final Lettering input = readLettering(name + ".font.input.", properties);
		final Lettering print = readLettering(name + ".font.print.", properties);
		final Lettering list = readLettering(name + ".font.list.", properties);
		@SuppressWarnings("unchecked")
		final Class<Document> documentClass = (Class<Document>) readClass(name + ".document", properties);
		Locale locale = Locale.getDefault();
		final String localeString = properties.getProperty(name + ".locale").trim();
		if (localeString != null && localeString.trim().length() > 0) {
			locale = findLocale(localeString);
		}
		final Pattern pattern = readPattern(properties, name + ".linkRegex");
		final String searchLink = properties.getProperty(name + ".searchLink").trim();
		final List<TrainerModelInputAction> actions = loadInputActions(name + ".actions.", properties);
		return new DefaultTrainerModelInput(inputName, title, input, print, list, documentClass, locale, pattern,
				searchLink, actions);
	}

	private static Locale findLocale(final String string) {
		if ((string == null) || (string.trim().length() <= 0)) {
			return Locale.getDefault();
		}
		Locale language = null;
		final String[] parts = string.split("_", 2);
		for (final Locale locale : Locale.getAvailableLocales()) {
			if (locale.getLanguage().equals(parts[0]) && locale.getCountry().equals(parts[1])) {
				return locale;
			} else if (locale.getLanguage().equals(parts[0])
					&& (language == null || locale.getCountry() == null || locale.getCountry().trim().length() == 0)) {
				language = locale;
			}
		}
		return language;
	}

	private static Lettering readLettering(final String prefix, final Properties properties) {
		final String nameString = properties.getProperty(prefix + "name").trim();
		final String sizeString = properties.getProperty(prefix + "size").trim();
		final String styleString = properties.getProperty(prefix + "style").trim();
		final String colorString = properties.getProperty(prefix + "color");
		final String[] styles = styleString.split("\\s*[|]\\s*");
		int style = Font.PLAIN;
		for (final String s : styles) {
			if ("bold".equalsIgnoreCase(s)) {
				style |= Font.BOLD;
			} else if ("italic".equalsIgnoreCase(s)) {
				style |= Font.ITALIC;
			}
		}
		return new Lettering(new Font(nameString, style, Integer.parseInt(sizeString)), getColor(colorString));
	}

	private static Color getColor(final String color) {
		return Color.black; // TODO Farbe parsen
	}

	private static List<TrainerModelInputAction> loadInputActions(final String prefix, final Properties properties) {
		final List<TrainerModelInputAction> actions = new ArrayList<>();
		// TTS
		final ActionParameters actTts = readActionParameters(properties, prefix + "tts", "type");
		if (actTts != null) {
			try {
				actions.add(new TextToSpeechAction(TextToSpeech.valueOf(actTts.getCommand()).getTextToSpeechProvider(),
						actTts.getPattern()));
			} catch (final Exception e) {
				System.err.println("Error loading TTS action for type: " + actTts.getCommand());
			}
		}
		actions.addAll(readUrlActions(prefix + "url", properties));
		return actions;
	}

	private static List<TrainerModelInputAction> readUrlActions(String prefix, final Properties properties) {
		if (!prefix.endsWith(".")) {
			prefix += ".";
		}
		final List<TrainerModelInputAction> actions = new ArrayList<>();

		for (int i = 1;; ++i) {
			final TrainerModelInputAction input = readUrlAction(prefix + i + ".", properties);
			if (input == null) {
				break;
			}
			actions.add(input);
		}

		return actions;
	}

	private static TrainerModelInputAction readUrlAction(final String prefix, final Properties properties) {
		final String name = properties.getProperty(prefix + "name");
		if ((name == null) || (name.trim().length() <= 0)) {
			return null;
		}
		final String iconName = properties.getProperty(prefix + "icon");
		final String url = properties.getProperty(prefix + "url");
		final List<Pattern> patterns = new ArrayList<>();
		Pattern pattern;
		for (int i = 1; (pattern = readPattern(properties, prefix + "regex." + i)) != null; ++i) {
			patterns.add(pattern);
		}
		return new UrlAction(name, patterns.toArray(new Pattern[0]), url, iconName);
	}

	private static ActionParameters readActionParameters(final Properties properties, String prefix,
			final String name) {
		if (!prefix.endsWith(".")) {
			prefix += ".";
		}
		final String command = properties.getProperty(prefix + name);
		if (command != null && !command.trim().isEmpty()) {
			return new ActionParameters(command, readPattern(properties, prefix + "regex"));
		}
		return null;
	}

	private static Pattern readPattern(final Properties properties, String prefix) {
		Pattern pattern = null;
		if (!prefix.endsWith(".")) {
			prefix += ".";
		}
		final String patternString = properties.getProperty(prefix + "text");
		if (patternString != null && patternString.trim().length() > 0) {
			pattern = Pattern.compile(patternString, getPatternFlags(properties.getProperty(prefix + "flags")));
		}
		return pattern;
	}

	private static int getPatternFlags(String flags) {
		int style = 0;
		if (flags != null) {
			flags = flags.trim();
			for (final String flag : flags.split("\\s*[|]\\s*")) {
				if (flag == null || flag.trim().length() == 0) {
					// nix
				} else if ("CANON_EQ".equalsIgnoreCase(flag)) {
					style |= Pattern.CANON_EQ;
				} else if ("CASE_INSENSITIVE".equalsIgnoreCase(flag)) {
					style |= Pattern.CASE_INSENSITIVE;
				} else if ("COMMENTS".equalsIgnoreCase(flag)) {
					style |= Pattern.COMMENTS;
				} else if ("DOTALL".equalsIgnoreCase(flag)) {
					style |= Pattern.DOTALL;
				} else if ("LITERAL".equalsIgnoreCase(flag)) {
					style |= Pattern.LITERAL;
				} else if ("MULTILINE".equalsIgnoreCase(flag)) {
					style |= Pattern.MULTILINE;
				} else if ("UNICODE_CASE".equalsIgnoreCase(flag)) {
					style |= Pattern.UNICODE_CASE;
				} else if ("UNIX_LINES".equalsIgnoreCase(flag)) {
					style |= Pattern.UNIX_LINES;
				} else {
					System.err.println("Unknown flag: " + flag);
				}
			}
		}
		return style;
	}

	private static Class<?> readClass(final String key, final Properties properties) {
		final String className = properties.getProperty(key);
		if (className == null) {
			return null;
		}
		try {
			return Class.forName(className);
		} catch (final ClassNotFoundException e) {
			return null;
		}
	}

	private static class ActionParameters {
		private final String command;
		private final Pattern pattern;

		public ActionParameters(final String command, final Pattern pattern) {
			this.command = command;
			this.pattern = pattern;
		}

		public String getCommand() {
			return command;
		}

		public Pattern getPattern() {
			return pattern;
		}
	}
}
