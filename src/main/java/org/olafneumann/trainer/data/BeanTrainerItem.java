package org.olafneumann.trainer.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.olafneumann.settings.Setting;

public class BeanTrainerItem implements TrainerItem {
	private final List<TrainerItemListener> listeners = new ArrayList<>();
	private BeanTrainerModel model = null;

	private List<String> values = new ArrayList<>();

	public BeanTrainerItem() {
	}

	public BeanTrainerItem(final BeanTrainerModel model) {
		this(model, null);
	}

	public BeanTrainerItem(final BeanTrainerModel model, final String[] values) {
		setModel(model);
		if (values != null) {
			for (int i = 0; i < values.length; ++i) {
				this.values.set(i, values[i]);
			}
		}
	}

	@Override
	public void setValue(final int index, String value) {
		if (value == null) {
			value = "";
		}
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
	public void setKnown(final int index, final boolean known) {
		model.setKnown(this, index, known);
		fireItemChanged();
	}

	@Override
	public boolean isKnown(final int index) {
		return model.isKnown(this, index);
	}

	@Override
	public boolean isEmpty() {
		for (final String value : values) {
			if (value != null && !value.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Setting("model")
	public BeanTrainerModel getModel() {
		return model;
	}

	@Setting("model")
	public void setModel(final BeanTrainerModel model) {
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
	public void setEntries(final List<String> entries) {
		this.values = entries;
	}

	@Override
	public void addTrainerItemListener(final TrainerItemListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTrainerItemListener(final TrainerItemListener listener) {
		listeners.add(listener);
	}

	protected void fireItemChanged() {
		for (final TrainerItemListener listener : listeners) {
			try {
				listener.trainerItemChanged(this);
			} catch (final Exception e) {
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
		return Objects.hash(values);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BeanTrainerItem other = (BeanTrainerItem) obj;
		if (!Objects.equals(values, other.values)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final String value : values) {
			sb.append(" / ").append(value);
		}
		if (!values.isEmpty()) {
			for (int i = 0; i < 3; ++i) {
				sb.deleteCharAt(0);
			}
		}
		sb.append(" (").append(countKnown()).append("/").append(values.size()).append(")");
		return sb.toString();
	}
}
