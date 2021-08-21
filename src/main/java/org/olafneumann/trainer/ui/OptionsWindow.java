package org.olafneumann.trainer.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.olafneumann.trainer.Settings;
import org.olafneumann.trainer.data.DefaultTrainerModelInput;

public class OptionsWindow extends JDialog {
	private static final long serialVersionUID = -3015471649713756394L;
	private static OptionsWindow window = null;

	public static synchronized void showWindow(final JFrame owner, final Settings settings,
			final DefaultTrainerModelInput[] inputs) {
		if (window == null) {
			SwingUtilities.invokeLater(() -> {
				window = new OptionsWindow(owner, settings, inputs);
				window.setVisible(true);
			});
		} else {
			SwingUtilities.invokeLater(() -> window.requestFocus());
		}
	}

	public static synchronized void hideWindow() {
		if (window != null) {
			SwingUtilities.invokeLater(() -> {
				window.setVisible(false);
				window.dispose();
				window = null;
			});
		}
	}

	private static JPanel createInputPanel(final DefaultTrainerModelInput input) {
		return new JPanel();
	}

	public OptionsWindow(final JFrame owner, final Settings settings, final DefaultTrainerModelInput[] inputs) {
		super(owner);
		setTitle("Options");
		/* setAutoRequestFocus(true); */
		setLayout(new BorderLayout());

		final JTabbedPane tbpMain = new JTabbedPane();
		add(tbpMain, BorderLayout.CENTER);

		// Main
		final JPanel pnlMain = new JPanel();
		tbpMain.add("Main", pnlMain);

		// inputs
		for (final DefaultTrainerModelInput input : inputs) {
			tbpMain.add(input.getName(), createInputPanel(input));
		}

		// buttons
		final JButton btnOK = new JButton("OK");
		final JButton btnCancel = new JButton("Cancel");
		final JPanel pnlButtons = new JPanel(new GridLayout(1, 2));
		pnlButtons.add(btnCancel);
		pnlButtons.add(btnOK);
		final JPanel pnlBottom = new JPanel(new BorderLayout());
		pnlBottom.add(pnlButtons, BorderLayout.EAST);
		add(pnlBottom, BorderLayout.SOUTH);

		pack();
	}
}
