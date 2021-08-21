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
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window = new OptionsWindow(owner, settings, inputs);
					window.setVisible(true);
				}
			});
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.requestFocus();
				}
			});
		}
	}

	public static synchronized void hideWindow() {
		if (window != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.setVisible(false);
					window.dispose();
					window = null;
				}
			});
		}
	}

	private static JPanel createInputPanel(DefaultTrainerModelInput input) {
		return new JPanel();
	}

	public OptionsWindow(JFrame owner, Settings settings, DefaultTrainerModelInput[] inputs) {
		super(owner);
		setTitle("Options");
		/*setAutoRequestFocus(true);*/
		setLayout(new BorderLayout());

		JTabbedPane tbpMain = new JTabbedPane();
		add(tbpMain, BorderLayout.CENTER);

		// Main
		JPanel pnlMain = new JPanel();
		tbpMain.add("Main", pnlMain);

		// inputs
		for (DefaultTrainerModelInput input : inputs)
			tbpMain.add(input.getName(), createInputPanel(input));

		// buttons
		JButton btnOK = new JButton("OK");
		JButton btnCancel = new JButton("Cancel");
		JPanel pnlButtons = new JPanel(new GridLayout(1, 2));
		pnlButtons.add(btnCancel);
		pnlButtons.add(btnOK);
		JPanel pnlBottom = new JPanel(new BorderLayout());
		pnlBottom.add(pnlButtons, BorderLayout.EAST);
		add(pnlBottom, BorderLayout.SOUTH);

		pack();
	}
}
