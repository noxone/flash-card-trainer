package org.olafneumann.trainer.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import org.olafneumann.trainer.data.TrainerModelInput;

class InputArea extends JPanel implements UndoableEditListener {
	private static final long serialVersionUID = -8977002175890800568L;

	private final JToggleButton tglKnown = new JToggleButton(Icons.CHECK_SMALL.getImageIcon());
	private final JLabel lblTitle = new JLabel();
	private final JTextArea txtInput = new JTextArea();

	private final UndoManager undo = new UndoManager();

	private final TrainerModelInput input;

	public InputArea(final TrainerModelInput input) {
		this.input = input;
		setLayout(new BorderLayout());
		final JPanel pnlNorth = new JPanel(new BorderLayout());
		((BorderLayout) pnlNorth.getLayout()).setHgap(10);
		pnlNorth.add(tglKnown, BorderLayout.WEST);
		pnlNorth.add(lblTitle, BorderLayout.CENTER);
		add(pnlNorth, BorderLayout.NORTH);
		add(txtInput, BorderLayout.CENTER);
		setTitle(input.getName());
		lblTitle.setFont(input.getTitleLettering().getFont());
		txtInput.setFont(input.getInputLettering().getFont());
		txtInput.setLineWrap(true);
		txtInput.setWrapStyleWord(true);
		txtInput.enableInputMethods(true);
		txtInput.setLocale(input.getLocale());
		final Document document = input.getDocument();
		if (document != null) {
			txtInput.setDocument(document);
			// TODO Chinesische Eingabe aktivieren
		}

		txtInput.getDocument().addUndoableEditListener(this);
		txtInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent evt) {
				if ((evt.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
					switch (evt.getKeyCode()) {
					case KeyEvent.VK_Z:
						if (undo.canUndo()) {
							undo.undo();
						}
						break;
					case KeyEvent.VK_Y:
						if (undo.canRedo()) {
							undo.redo();
						}
						break;
					default:
						// nothing
					}
				}
			}
		});
	}

	public String getText() {
		return txtInput.getText();
	}

	public void setText(final String text) {
		undo.discardAllEdits();
		txtInput.setText(text);
	}

	public String getTitle() {
		return lblTitle.getText();
	}

	public void setTitle(final String title) {
		lblTitle.setText(title);
	}

	public boolean isChecked() {
		return tglKnown.isSelected();
	}

	public void setChecked(final boolean checked) {
		tglKnown.setSelected(checked);
	}

	@Override
	public void requestFocus() {
		txtInput.requestFocus();
	}

	void addDocumentListener(final DocumentListener listener) {
		txtInput.getDocument().addDocumentListener(listener);
	}

	void addActionListener(final ActionListener listener) {
		tglKnown.addActionListener(listener);
	}

	@Override
	public void undoableEditHappened(final UndoableEditEvent editEvent) {
		undo.addEdit(editEvent.getEdit());
	}

	void applyLocaleFromInput() {
		if (txtInput.getInputContext() != null) {
			txtInput.getInputContext().selectInputMethod(input.getLocale());
		} else {
			System.err.println(String.format("InputContext for input %s is null.", input.getName()));
		}
	}
}
