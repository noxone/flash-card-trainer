package org.olafneumann.trainer.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import javax.swing.JMenuItem;

class UndoManager<T> {
	private UndoListener<T> undoListener;
	private final Stack<T> stack = new Stack<>();

	UndoManager(final UndoListener<T> undoListener) {
		this.undoListener = undoListener;
	}

	public int push(final T object) {
		stack.push(object);
		return stack.size();
	}

	public T peek() {
		return stack.peek();
	}

	public T pop() {
		return stack.pop();
	}

	public void clear() {
		stack.clear();
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	Collection<JMenuItem> createMenuItems() {
		final List<JMenuItem> items = new ArrayList<>();
		if (!stack.isEmpty()) {
			for (final T object : stack) {
				final JMenuItem item = new JMenuItem(object.toString());
				item.addActionListener(event -> undo(object));
				items.add(item);
			}
		} else {
			final JMenuItem item = new JMenuItem(Messages.getString("UndoManager.NothingToUndo")); //$NON-NLS-1$
			item.setEnabled(false);
			items.add(item);
		}
		return items;
	}

	private void undo(final T object) {
		if (object != null) {
			stack.remove(object);
			undoListener.undo(object);
		}
	}

	public void undo() {
		undo(pop());
	}

	public UndoListener<T> getUndoListener() {
		return undoListener;
	}

	public void setUndoListener(final UndoListener<T> undoListener) {
		this.undoListener = undoListener;
	}
}
