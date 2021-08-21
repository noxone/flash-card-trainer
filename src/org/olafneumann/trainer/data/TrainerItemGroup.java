package org.olafneumann.trainer.data;

public interface TrainerItemGroup<I extends TrainerItem> extends QuestionItemProvider, Iterable<I> {
	TrainerModel<I> getModel();

	void add(I item);

	void remove(I item);

	int size();
}
