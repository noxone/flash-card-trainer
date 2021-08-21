package org.olafneumann.trainer.data;

import java.util.Iterator;
import java.util.List;

public class BeanTrainerItemGroup implements TrainerItemGroup<BeanTrainerItem> {
	private BeanTrainerModel model;
	private String name;
	private List<BeanTrainerItem> items;

	public String getName() {
		return name;
	}

	@Override
	public QuestionItem randomQuestionItem() {
		return BeanQuestionItem.randomQuestionItem(this, items);
	}

	@Override
	public BeanTrainerModel getModel() {
		return model;
	}

	@Override
	public Iterator<BeanTrainerItem> iterator() {
		return items.iterator();
	}

	@Override
	public void add(final BeanTrainerItem item) {
		items.add(item);

	}

	@Override
	public void remove(final BeanTrainerItem item) {
		items.remove(item);
	}

	@Override
	public int size() {
		return items.size();
	}

	public List<BeanTrainerItem> getItems() {
		return items;
	}

	public void setItems(final List<BeanTrainerItem> items) {
		this.items = items;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setModel(final BeanTrainerModel model) {
		this.model = model;
	}

	@Override
	public String toString() {
		return getName();
	}
}
