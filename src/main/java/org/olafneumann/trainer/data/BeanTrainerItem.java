package org.olafneumann.trainer.data;

import java.util.ArrayList;
import java.util.List;

import org.olafneumann.settings.Setting;

public class BeanTrainerItem implements TrainerItem {
	private List<TrainerItemListener> listeners = new ArrayList<TrainerItemListener>();
	private BeanTrainerModel model = null;

	private List<String> values = new ArrayList<String>();

	public BeanTrainerItem() {
	}

	public BeanTrainerItem(BeanTrainerModel model) {
		this(model, null);
	}

	public BeanTrainerItem(BeanTrainerModel model, String[] values) {
		setModel(model);
		if (values != null)
			for (int i = 0; i < values.length; ++i)
				this.values.set(i, values[i]);
	}

	@Override
	public void setValue(int index, String value) {
		if (value == null)
			value = "";
		values.set(index, value);
		fireItemChanged();
	}

	@Override
	public String[] getValues() {
		return values.toArray(new String[0]);
	}

	int getValueCount() {
		return values.size();
	}

	@Override
	public void setKnown(int index, boolean known) {
		model.setKnown(this, index, known);
		fireItemChanged();
	}

	@Override
	public boolean isKnown(int index) {
		return model.isKnown(this, index);
	}

	@Override
	public boolean isEmpty() {
		for (String value : values)
			if (value != null && !value.isEmpty())
				return false;
		return true;
	}

	@Setting("model")
	public BeanTrainerModel getModel() {
		return model;
	}

	@Setting("model")
	public void setModel(BeanTrainerModel model) {
		this.model = model;
		for (int i = values.size(); i < TrainerModelProvider.getInstance().getInputs(getModel()).length; ++i) {
			values.add("");
		}
	}

	@Setting("values")
	public List<String> getEntries() {
		return values;
	}

	@Setting("values")
	public void setEntries(List<String> entries) {
		this.values = entries;
	}

	@Override
	public void addTrainerItemListener(TrainerItemListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTrainerItemListener(TrainerItemListener listener) {
		listeners.add(listener);
	}

	protected void fireItemChanged() {
		for (TrainerItemListener listener : listeners) {
			try {
				listener.trainerItemChanged(this);
			} catch (Exception e) {
			}
		}
	}

	int countKnown() {
		int counter = 0;
		for (int i = 0; i < values.size(); ++i) {
			if (isKnown(i)) {
				++counter;
			}
		}
		return counter;
	}

	boolean hasUnknown() {
		if (countKnown() < values.size()) {
			for (int i = 0; i < values.size(); ++i) {
				if (!isKnown(i) && values.get(i).trim().length() > 0) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeanTrainerItem other = (BeanTrainerItem) obj;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String value : values) {
			sb.append(" / ").append(value);
		}
		if (!values.isEmpty()) {
			for (int i = 0; i < 3; ++i)
				sb.deleteCharAt(0);
		}
		sb.append(" (").append(countKnown()).append("/").append(values.size()).append(")");
		return sb.toString();
	}
}
