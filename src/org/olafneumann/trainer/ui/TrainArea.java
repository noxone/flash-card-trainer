package org.olafneumann.trainer.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private List<JButton> actionButtons = new ArrayList<JButton>();

	public TrainArea(boolean showCaptions, boolean showAdditionalInformation) {
		this.showAdditionalInformation = showAdditionalInformation;
		setLayout(new BorderLayout());
		if (showCaptions)
			add(lblTitle, BorderLayout.NORTH);
		add(txtItem, BorderLayout.CENTER);

		JPanel pnlActionHolder = new JPanel(new BorderLayout());
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

		for (JButton button : actionButtons) {
			button.getParent().remove(button);
		}
		actionButtons.clear();

		if (text.trim().length() > 0) {
			TrainerModelInputAction[] actions = input.getTrainerModelInputActions();
			((GridLayout) pnlActions.getLayout()).setRows(actions.length);

			for (final TrainerModelInputAction action : actions) {
				final JButton button = new JButton(action.getIcon());
				if (action.getIcon() == null)
					button.setText(action.toString());
				actionButtons.add(button);
				pnlActions.add(button);
				button.setToolTipText(action.getActionTooltip());
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						actionSelected(button, input, action, text);
					}
				});
			}
		}

		invalidate();
		repaint();
	}

	void addHyperlinkListener(HyperlinkListener listener) {
		txtItem.addHyperlinkListener(listener);
	}

	void removeHyperlinkListener(HyperlinkListener listener) {
		txtItem.removeHyperlinkListener(listener);
	}

	private String makeLink(TrainerModelInput input, String string, boolean important) {
		if (input.getLinkPattern() == null) {
			return string;
		} else {
			Matcher matcher = input.getLinkPattern().matcher(string);
			StringBuilder sb = new StringBuilder();
			sb.append("<html>").append( //$NON-NLS-1$
					ONUtils.getFontDiv(input.getInputLettering().getFont(), important, important ? 2.0 : 1.5));
			int last = 0;
			while (matcher.find()) {
				sb.append(string.substring(last, matcher.start()));
				String match = matcher.groupCount() > 0 ? matcher.group(1) : matcher.group();
				String link = input.getSearchUrl(match);
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
				sb.append(string.substring(last, string.length()));
				sb.append("</div>").append("</html>"); //$NON-NLS-1$ //$NON-NLS-2$
				return sb.toString();
			}
		}
	}

	public boolean isTextVisible() {
		return txtItem.isVisible();
	}

	public void setTextVisible(boolean visible) {
		txtItem.setVisible(visible);
	}

	private void actionSelected(JComponent component, final TrainerModelInput input, final TrainerModelInputAction action, String text) {
		List<String> texts = action.getTexts(text);
		if (texts == null || texts.isEmpty() || texts.size() == 1) {
			doPerformAction(input, action, texts == null || texts.isEmpty() ? text : texts.get(0));
		} else {
			JPopupMenu menu = new JPopupMenu();
			for (final String string : texts) {
				JMenuItem item = new JMenuItem(string);
				item.addActionListener(new AbstractAction() {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						doPerformAction(input, action, string);
					}
				});
				menu.add(item);
			}
			menu.show(component, 0, component.getSize().height);
		}
	}

	private void doPerformAction(final TrainerModelInput input, final TrainerModelInputAction action, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				action.performInputAction(input, text);
			}
		});
	}
}
