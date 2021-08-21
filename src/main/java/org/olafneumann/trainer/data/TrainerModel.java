package org.olafneumann.trainer.data;

import java.util.List;

public interface TrainerModel<I extends TrainerItem> extends QuestionItemProvider, Iterable<I> {
	int addItem(String[] items);

	int size();

	TrainerItem get(int index);

	TrainerItem remove(int index);

	void resetKnown();

	boolean hasUnknownItems();

	int countKnownItems();

	int countKnownItemInputs();

	boolean hasChanges();

	void setHasChanges(boolean changes);

	List<I> getItems();

	List<? extends TrainerItemGroup<I>> getGroups();
}
