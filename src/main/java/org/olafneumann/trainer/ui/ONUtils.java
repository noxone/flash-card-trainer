package org.olafneumann.trainer.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class ONUtils {
	private ONUtils() {
		throw new RuntimeException();
	}

	@Deprecated
	public static String fillString(final String text, final int length, final char fillchar, final boolean fillAtEnd) {
		if (text.length() >= length) {
			return text;
		}
		final char[] zeichen = new char[length - text.length()];
		Arrays.fill(zeichen, fillchar);
		if (fillAtEnd) {
			return text + new String(zeichen);
		} else {
			return new String(zeichen) + text;
		}
	}

	@Deprecated
	public static String fillString(final int number, final int length, final char fillchar, final boolean fillAtEnd) {
		return fillString(Integer.toString(number), length, fillchar, fillAtEnd);
	}

	@Deprecated
	public static boolean isNumber(final Class<?> clazz) {
		// return clazz == int.class || clazz == long.class || clazz ==
		// short.class || clazz == byte.class
		// || clazz == Integer.class || clazz == Long.class || clazz ==
		// Short.class || clazz == Byte.class
		// || clazz == float.class || clazz == Float.class || clazz ==
		// double.class || clazz == Double.class;
		return clazz == int.class || clazz == long.class || clazz == short.class || clazz == byte.class
				|| clazz == float.class || clazz == double.class || Number.class.isAssignableFrom(clazz);
	}

	@Deprecated
	public static boolean isNumber(final Object value) {
		return value == null || isNumber(value.getClass());
	}

	@Deprecated
	public static JPanel createMenuLabel(final String text) {
		return createMenuLabel(text, null);
	}

	@Deprecated
	public static JPanel createMenuLabel(final String text, final Color background) {
		JLabel label;
		label = new JLabel(text);
		// label.setAlignmentX(0.5f);
		label.setFont(label.getFont().deriveFont(Font.ITALIC | Font.BOLD));
		label.setPreferredSize(new Dimension(
				SwingUtilities.computeStringWidth(label.getFontMetrics(label.getFont()), label.getText()) + 5, 22));

		final JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.add(label);
		if (background != null) {
			panel.setBackground(background);
		}
		return panel;
	}

	public static boolean copyFile(final String source, final String dest) {
		if (source.equals(dest)) {
			return false;
		}
		InputStream in = null;
		OutputStream out = null;
		try {
			try {
				in = new BufferedInputStream(new FileInputStream(source));
				out = new BufferedOutputStream(new FileOutputStream(dest));
			} catch (final FileNotFoundException e) {
				return false;
			}

			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
		} catch (final IOException e) {
			return false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (final IOException e) {
				}
			}
		}
		return true;
	}

	public static void showMessage(final String message) {
		showMessage(message, 0);
	}

	public static void showMessage(final String message, final int type) {
		showMessage(null, message, type);
	}

	public static void showMessage(final String title, final String message, final int type) {
		if (SwingUtilities.isEventDispatchThread()) {
			showMessage_internal(title, message, type);
		} else {
			try {
				SwingUtilities.invokeAndWait(() -> showMessage_internal(title, message, type));
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void showError(final String errorMessage) {
		showMessage(Messages.getString("ONUtils.Error"), errorMessage, JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}

	public static void showError(final Exception exception) {
		showError(Messages.getString("ONUtils.AnErrorOccurred") + exception.getLocalizedMessage()); //$NON-NLS-1$
	}

	public static boolean showQuestion(final String question) {
		if (SwingUtilities.isEventDispatchThread()) {
			return showQuestion_internal(question);
		}
		try {
			final boolean[] answer = new boolean[1];
			SwingUtilities.invokeAndWait(() -> answer[0] = showQuestion_internal(question));
			return answer[0];
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void showMessage_internal(String title, final String message, final int type) {
		final Frame parent = getMessageParent();
		if (title == null && parent != null) {
			title = parent.getTitle() + " - " + Messages.getString("ONUtils.Message"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (title == null) {
			title = Messages.getString("ONUtils.Message"); //$NON-NLS-1$
		}
		JOptionPane.showMessageDialog(parent, message, title, type);
	}

	private static boolean showQuestion_internal(final String question) {
		final Frame parent = getMessageParent();
		String title = null;
		if (parent != null) {
			title = parent.getTitle() + " - " + Messages.getString("ONUtils.Question"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (title == null) {
			title = Messages.getString("ONUtils.Question"); //$NON-NLS-1$
		}
		return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(getMessageParent(), question, title,
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	}

	private static Frame getMessageParent() {
		return MainWindow.getWindow();
	}

	public static Thread runInDaemonThread(final Runnable runnable, final String threadName) {
		final Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		if (threadName != null) {
			thread.setName(threadName);
		}
		thread.start();
		return thread;
	}

	public static void saveSettings(final Object settings) {
		if (settings == null) {
			throw new NullPointerException();
		}

		final Class<?> clazz = settings.getClass();
		final Preferences prefs = Preferences.userNodeForPackage(clazz);
		for (final Field d : clazz.getFields()) {
			if (d.getType() == String.class) {
				try {
					prefs.put(d.getName(), d.get(settings).toString());
				} catch (final Exception e) {
				}
			}
		}
	}

	public static void loadSettings(final Object settings) {
		if (settings == null) {
			throw new NullPointerException();
		}

		final Class<?> clazz = settings.getClass();
		final Preferences prefs = Preferences.userNodeForPackage(clazz);
		for (final Field d : clazz.getFields()) {
			if (d.getType() == String.class) {
				try {
					d.set(settings, prefs.get(d.getName(), "")); //$NON-NLS-1$
				} catch (final Exception e) {
				}
			}
		}
	}

	public static String getFontDiv(final Font font, final boolean bold, final double factor) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div style=\""); //$NON-NLS-1$
		appendStyle(sb, "font-family", font.getName()); //$NON-NLS-1$
		appendStyle(sb, "font-size", (int) (font.getSize() * factor) + "pt"); //$NON-NLS-1$ //$NON-NLS-2$
		if (bold || (font.getStyle() & Font.BOLD) == Font.BOLD) {
			appendStyle(sb, "font-weight", "bold"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if ((font.getStyle() & Font.ITALIC) == Font.ITALIC) {
			appendStyle(sb, "font-style", "italic"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append("\">"); //$NON-NLS-1$
		return sb.toString();
	}

	private static void appendStyle(final StringBuilder sb, final String name, final String value) {
		sb.append(name).append(":").append(value).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
