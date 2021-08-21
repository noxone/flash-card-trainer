package org.olafneumann.trainer.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

abstract class SimpleDocumentListener implements DocumentListener {
	@Override
	public void removeUpdate(DocumentEvent e) {
		textChanged(e);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		removeUpdate(e);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		removeUpdate(e);
	}

	protected abstract void textChanged(DocumentEvent e);
}
