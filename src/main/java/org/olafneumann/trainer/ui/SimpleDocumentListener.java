package org.olafneumann.trainer.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

abstract class SimpleDocumentListener implements DocumentListener {
	@Override
	public void removeUpdate(final DocumentEvent e) {
		textChanged(e);
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		removeUpdate(e);
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		removeUpdate(e);
	}

	protected abstract void textChanged(DocumentEvent e);
}
