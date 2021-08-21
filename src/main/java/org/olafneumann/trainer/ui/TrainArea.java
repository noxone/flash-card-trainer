package org.olafneumann.trainer.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkListener;

import org.olafneumann.trainer.action.TrainerModelInputAction;
import org.olafneumann.trainer.data.TrainerModelInput;

class TrainArea extends JPanel {
	private static final long serialVersionUID = 2997150357577525274L;

	private final JLabel lblTitle = new JLabel();
	private final JEditorPane txtItem = new JEditorPane("text/html", ""); //$NON-NLS-1$ //$NON-NLS-2$
	private final boolean showAdditionalInformation;

	private final JPanel pnlActions = new JPanel(new GridLayout(1, 1));
	private final List<JButton> actionButtons = new ArrayList<>();

	public TrainArea(final boolean showCaptions, final boolean showAdditionalInformation) {
		this.showAdditionalInformation = showAdditionalInformation;
		setLayout(new BorderLayout());
		if (showCaptions) {
			add(lblTitle, BorderLayout.NORTH);
		}
		add(txtItem, BorderLayout.CENTER);

		final JPanel pnlActionHolder = new JPanel(new BorderLayout());
		pnlActionHolder.add(pnlActions, BorderLayout.NORTH);
		add(pnlActionHolder, BorderLayout.EAST);

		txtItem.setEditable(false);
		txtItem.setOpaque(false);
	}

	private boolean isImportant() {
		return !showAdditionalInformation;
	}

	void setContent(final TrainerModelInput input, final String text) {
		lblTitle.setText(input.getName());
		txtItem.setText(makeLink(input, text, isImportant()));

		for (final JButton button : actionButtons) {
			button.getParent().remove(button);
		}
		actionButtons.clear();

		if (text.trim().length() > 0) {
			final TrainerModelInputAction[] actions = input.getTrainerModelInputActions();
			((GridLayout) pnlActions.getLayout()).setRows(actions.length);

			for (final TrainerModelInputAction action : actions) {
				final JButton button = new JButton(action.getIcon());
				if (action.getIcon() == null) {
					button.setText(action.toString());
				}
				actionButtons.add(button);
				pnlActions.add(button);
				button.setToolTipText(action.getActionTooltip());
				button.addActionListener(event -> actionSelected(button, input, action, text));
			}
		}

		invalidate();
		repaint();
	}

	void addHyperlinkListener(final HyperlinkListener listener) {
		txtItem.addHyperlinkListener(listener);
	}

	void removeHyperlinkListener(final HyperlinkListener listener) {
		txtItem.removeHyperlinkListener(listener);
	}

	private String makeLink(final TrainerModelInput input, final String string, final boolean important) {
		if (input.getLinkPattern() == null) {
			return string;
		}
		final Matcher matcher = input.getLinkPattern().matcher(string);
		final StringBuilder sb = new StringBuilder();
		sb.append("<html>").append( //$NON-NLS-1$
				ONUtils.getFontDiv(input.getInputLettering().getFont(), important, important ? 2.0 : 1.5));
		int last = 0;
		while (matcher.find()) {
			sb.append(string.substring(last, matcher.start()));
			final String match = matcher.groupCount() > 0 ? matcher.group(1) : matcher.group();
			final String link = input.getSearchUrl(match);
			if (link != null) {
				sb.append("<a href=\"").append(link).append("\" style=\"color:#00F;text-decoration:none;\">") //$NON-NLS-1$ //$NON-NLS-2$
						.append(match).append("</a>"); //$NON-NLS-1$
			} else {
				sb.append(match);
			}
			last = matcher.end();
		}
		// did we find anything?
		if (sb.length() == 0) {
			return string;
		} else {
			sb.append(string.substring(last));
			sb.append("</div>").append("</html>"); //$NON-NLS-1$ //$NON-NLS-2$
			return sb.toString();
		}
	}

	public boolean isTextVisible() {
		return txtItem.isVisible();
	}

	public void setTextVisible(final boolean visible) {
		txtItem.setVisible(visible);
	}

	private void actionSelected(final JComponent component, final TrainerModelInput input,
			final TrainerModelInputAction action, final String text) {
		final List<String> texts = action.getTexts(text);
		if (texts == null || texts.isEmpty() || texts.size() == 1) {
			doPerformAction(input, action, texts == null || texts.isEmpty() ? text : texts.get(0));
		} else {
			final JPopupMenu menu = new JPopupMenu();
			for (final String string : texts) {
				final JMenuItem item = new JMenuItem(string);
				item.addActionListener(new AbstractAction() {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(final ActionEvent e) {
						doPerformAction(input, action, string);
					}
				});
				menu.add(item);
			}
			menu.show(component, 0, component.getSize().height);
		}
	}

	private void doPerformAction(final TrainerModelInput input, final TrainerModelInputAction action,
			final String text) {
		SwingUtilities.invokeLater(() -> action.performInputAction(input, text));
	}
}
