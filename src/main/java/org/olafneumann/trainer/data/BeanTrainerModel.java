package org.olafneumann.trainer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractListModel;

public class BeanTrainerModel extends AbstractListModel<BeanTrainerItem> implements TrainerModel<BeanTrainerItem> {
	private static final long serialVersionUID = 7256503462050207836L;

	private List<BeanTrainerItem> items = new ArrayList<>();
	private Map<BeanTrainerItem, Set<Integer>> known = new HashMap<>();

	private List<BeanTrainerItemGroup> groups = new ArrayList<>();

	boolean changes = false;

	@Override
	public synchronized int addItem(final String[] values) {
		return addItem(createItem(values));
	}

	public synchronized int addItem(final BeanTrainerItem item) {
		items.add(item);
		fireIntervalAdded(this, items.size() - 1, items.size() - 1);
		return items.size() - 1;
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public BeanTrainerItem get(final int index) {
		return items.get(index);
	}

	@Override
	public TrainerItem remove(final int index) {
		try {
			final BeanTrainerItem item = items.remove(index);
			known.remove(item);
			return item;
		} finally {
			fireIntervalRemoved(this, index, index);
		}
	}

	@Override
	public void resetKnown() {
		known.clear();
		fireContentsChanged(this, 0, size() - 1);
	}

	boolean isKnown(final BeanTrainerItem item, final int index) {
		return known.containsKey(item) && known.get(item).contains(index);
	}

	void setKnown(final BeanTrainerItem item, final int index, final boolean known) {
		if (!this.known.containsKey(item)) {
			this.known.put(item, new HashSet<Integer>());
		}
		if (known) {
			this.known.get(item).add(index);
		} else {
			this.known.get(item).remove(index);
		}
		if (this.known.get(item).isEmpty()) {
			this.known.remove(item);
		}
		fireContentsChanged(this, items.indexOf(item), items.indexOf(item));
	}

	@Override
	public boolean hasUnknownItems() {
		if (known.size() < items.size()) {
			return true;
		}
		for (final BeanTrainerItem item : known.keySet()) {
			if (!isCompletelyKnown(item)) {
				return true;
			}
		}
		return false;
	}

	private boolean isCompletelyKnown(final BeanTrainerItem item) {
		for (int i = 0; i < item.getValues().length; ++i) {
			if (!item.isKnown(i)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public QuestionItem randomQuestionItem() {
		return BeanQuestionItem.randomQuestionItem(this, items);
	}

	static int getInputSize() {
		return TrainerModelProvider.getInstance().getInputs(BeanTrainerModel.class).length;
	}

	@Override
	public int countKnownItems() {
		final int size = getInputSize();
		int counter = 0;
		for (final Entry<BeanTrainerItem, Set<Integer>> entry : known.entrySet()) {
			if (entry.getValue().size() == size) {
				++counter;
			}
		}
		return counter;
	}

	@Override
	public int countKnownItemInputs() {
		int counter = 0;
		for (final Set<Integer> knowns : known.values()) {
			counter += knowns.size();
		}
		return counter;
	}

	@Override
	public BeanTrainerItem getElementAt(final int index) {
		return get(index);
	}

	@Override
	public int getSize() {
		return size();
	}

	protected BeanTrainerItem createItem(final String[] values) {
		final BeanTrainerItem item = new BeanTrainerItem(this);
		for (int i = 0; i < values.length; ++i) {
			item.setValue(i, values[i]);
		}
		return item;
	}

	@Override
	public Iterator<BeanTrainerItem> iterator() {
		return items.iterator();
	}

	@Override
	public List<BeanTrainerItem> getItems() {
		return items;
	}

	public void setItems(final List<BeanTrainerItem> items) {
		this.items = items;
	}

	public Map<BeanTrainerItem, Set<Integer>> getKnown() {
		return known;
	}

	public void setKnown(final Map<BeanTrainerItem, Set<Integer>> known) {
		this.known = known;
	}

	@Override
	public boolean hasChanges() {
		return changes;
	}

	@Override
	public void setHasChanges(final boolean changes) {
		this.changes = changes;
	}

	@Override
	protected void fireContentsChanged(final Object source, final int index0, final int index1) {
		setHasChanges(true);
		super.fireContentsChanged(source, index0, index1);
	}

	@Override
	protected void fireIntervalAdded(final Object source, final int index0, final int index1) {
		setHasChanges(true);
		super.fireIntervalAdded(source, index0, index1);
	}

	@Override
	protected void fireIntervalRemoved(final Object source, final int index0, final int index1) {
		setHasChanges(true);
		super.fireIntervalRemoved(source, index0, index1);
	}

	@Override
	public List<BeanTrainerItemGroup> getGroups() {
		return groups;
	}

	public void setGroups(final List<BeanTrainerItemGroup> groups) {
		this.groups = groups;
	}
}