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

	public static List<TrainerModelInput> readTrainerModelInputs(InputStream in) throws IOException {
		Properties properties = new Properties();
		properties.load(in);
		return readTrainerModelInputs(properties);
	}

	public static List<TrainerModelInput> readTrainerModelInputs(Properties properties) {
		List<TrainerModelInput> inputs = new ArrayList<TrainerModelInput>();
		for (int i = 1;; ++i) {
			TrainerModelInput input = createInput(properties, "input" + i);
			if (input != null)
				inputs.add(input);
			else
				break;
		}
		return inputs;
	}

	private static TrainerModelInput createInput(Properties properties, String name) {
		String inputName = properties.getProperty(name + ".name");
		if (inputName != null && inputName.trim().length() > 0) {
			inputName = inputName.trim();
			Lettering title = readLettering(name + ".font.title.", properties);
			Lettering input = readLettering(name + ".font.input.", properties);
			Lettering print = readLettering(name + ".font.print.", properties);
			Lettering list = readLettering(name + ".font.list.", properties);
			@SuppressWarnings("unchecked")
			Class<Document> documentClass = (Class<Document>) readClass(name + ".document", properties);
			Locale locale = Locale.getDefault();
			String localeString = properties.getProperty(name + ".locale").trim();
			if (localeString != null && localeString.trim().length() > 0)
				locale = findLocale(localeString);
			Pattern pattern = readPattern(properties, name + ".linkRegex");
			String searchLink = properties.getProperty(name + ".searchLink").trim();
			List<TrainerModelInputAction> actions = loadInputActions(name + ".actions.", properties);
			return new DefaultTrainerModelInput(inputName, title, input, print, list, documentClass, locale, pattern, searchLink, actions);
		} else {
			return null;
		}
	}

	private static Locale findLocale(String string) {
		if (string != null && string.trim().length() > 0) {
			Locale language = null;
			String[] parts = string.split("_", 2);
			for (Locale locale : Locale.getAvailableLocales()) {
				if (locale.getLanguage().equals(parts[0]) && locale.getCountry().equals(parts[1])) {
					return locale;
				} else if (locale.getLanguage().equals(parts[0])
						&& (language == null || locale.getCountry() == null || locale.getCountry().trim().length() == 0)) {
					language = locale;
				}
			}
			return language;
		} else {
			return Locale.getDefault();
		}
	}

	private static Lettering readLettering(String prefix, Properties properties) {
		String nameString = properties.getProperty(prefix + "name").trim();
		String sizeString = properties.getProperty(prefix + "size").trim();
		String styleString = properties.getProperty(prefix + "style").trim();
		String colorString = properties.getProperty(prefix + "color");
		String[] styles = styleString.split("\\s*[|]\\s*");
		int style = Font.PLAIN;
		for (String s : styles) {
			if ("bold".equalsIgnoreCase(s))
				style |= Font.BOLD;
			else if ("italic".equalsIgnoreCase(s))
				style |= Font.ITALIC;
		}
		return new Lettering(new Font(nameString, style, Integer.parseInt(sizeString)), getColor(colorString));
	}

	private static Color getColor(String color) {
		return Color.black; // TODO Farbe parsen
	}

	private static List<TrainerModelInputAction> loadInputActions(String prefix, Properties properties) {
		List<TrainerModelInputAction> actions = new ArrayList<TrainerModelInputAction>();
		// TTS
		ActionParameters actTts = readActionParameters(properties, prefix + "tts", "type");
		if (actTts != null) {
			try {
				actions.add(new TextToSpeechAction(TextToSpeech.valueOf(actTts.getCommand()).getTextToSpeechProvider(), actTts.getPattern()));
			}
			catch (Exception e) {
				System.err.println("Error loading TTS action for type: " + actTts.getCommand());
			}
		}
		actions.addAll(readUrlActions(prefix + "url", properties));
		return actions;
	}

	private static List<TrainerModelInputAction> readUrlActions(String prefix, Properties properties) {
		if (!prefix.endsWith("."))
			prefix += ".";
		List<TrainerModelInputAction> actions = new ArrayList<TrainerModelInputAction>();

		for (int i = 1;; ++i) {
			TrainerModelInputAction input = readUrlAction(prefix + i + ".", properties);
			if (input != null)
				actions.add(input);
			else
				break;
		}

		return actions;
	}

	private static TrainerModelInputAction readUrlAction(String prefix, Properties properties) {
		String name = properties.getProperty(prefix + "name");
		if (name != null && name.trim().length() > 0) {
			String iconName = properties.getProperty(prefix + "icon");
			String url = properties.getProperty(prefix + "url");
			List<Pattern> patterns = new ArrayList<Pattern>();
			Pattern pattern;
			for (int i = 1; (pattern = readPattern(properties, prefix + "regex." + i)) != null; ++i) {
				patterns.add(pattern);
			}
			return new UrlAction(name, patterns.toArray(new Pattern[0]), url, iconName);
		} else {
			return null;
		}
	}

	private static ActionParameters readActionParameters(Properties properties, String prefix, String name) {
		if (!prefix.endsWith("."))
			prefix += ".";
		String command = properties.getProperty(prefix + name);
		if (command != null && !command.trim().isEmpty())
			return new ActionParameters(command, readPattern(properties, prefix + "regex"));
		else
			return null;
	}

	private static Pattern readPattern(Properties properties, String prefix) {
		Pattern pattern = null;
		if (!prefix.endsWith("."))
			prefix += ".";
		String patternString = properties.getProperty(prefix + "text");
		if (patternString != null && patternString.trim().length() > 0) {
			pattern = Pattern.compile(patternString, getPatternFlags(properties.getProperty(prefix + "flags")));
		}
		return pattern;
	}

	private static int getPatternFlags(String flags) {
		int style = 0;
		if (flags != null) {
			flags = flags.trim();
			for (String flag : flags.split("\\s*[|]\\s*")) {
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

	private static Class<?> readClass(String key, Properties properties) {
		String className = properties.getProperty(key);
		if (className != null) {
			try {
				return Class.forName(className);
			}
			catch (ClassNotFoundException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	private static class ActionParameters {
		private final String command;
		private final Pattern pattern;

		public ActionParameters(String command, Pattern pattern) {
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
