package org.olafneumann.trainer.ui;

@FunctionalInterface
public interface UndoListener<T> {
	void undo(T object);
}
