package org.olafneumann.trainer.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class BeanQuestionItem implements QuestionItem, TrainerItemListener {
	static QuestionItem randomQuestionItem(final QuestionItemProvider provider,
			final Collection<BeanTrainerItem> items) {
		final List<List<Object>> possibleItems = new ArrayList<>();
		for (final BeanTrainerItem item : items) {
			if (item.hasUnknown()) {
				final List<Object> parts = new ArrayList<>();
				parts.add(item);

				for (int i = 0; i < item.getValueCount(); ++i) {
					if (!item.isEmpty() && !item.isKnown(i)) {
						parts.add(i);
					}
				}
				if (parts.size() > 1) {
					possibleItems.add(parts);
				}
			}
		}

		if (possibleItems.isEmpty()) {
			return null;
		}

		final List<Object> itemParts = possibleItems.get((int) (possibleItems.size() * Math.random()));
		final int direction = (Integer) itemParts.get(1 + (int) ((itemParts.size() - 1) * Math.random()));
		return new BeanQuestionItem(BeanTrainerModel.class, provider, (TrainerItem) itemParts.get(0), direction);
	}

	private final Class<? extends TrainerModel<?>> clazz;
	private final QuestionItemProvider provider;
	private final TrainerItem item;
	private final int direction;

	private final Set<QuestionItemListener> listeners = new HashSet<>();

	private BeanQuestionItem(final Class<? extends TrainerModel<?>> clazz, final QuestionItemProvider provider,
			final TrainerItem item, final int direction) {
		this.clazz = clazz;
		this.provider = provider;
		this.item = item;
		this.direction = direction;
	}

	@Override
	public String getQuestion() {
		return item.getValues()[direction];
	}

	@Override
	public String[] getAnswers() {
		final List<String> out = new ArrayList<>(Arrays.asList(item.getValues()));
		out.remove(direction);
		return out.toArray(new String[0]);
	}

	@Override
	public TrainerModelInput getQuestionInput() {
		return TrainerModelProvider.getInstance().getInputs(clazz)[direction];
	}

	@Override
	public TrainerModelInput[] getAnswerInputs() {
		final List<TrainerModelInput> out = new ArrayList<>(
				Arrays.asList(TrainerModelProvider.getInstance().getInputs(clazz)));
		out.remove(direction);
		return out.toArray(new TrainerModelInput[0]);
	}

	@Override
	public QuestionItem getNext(final boolean known) {
		if (item.isKnown(direction) != known) {
			item.setKnown(direction, known);
		}
		return provider.randomQuestionItem();
	}

	@Override
	public void addQuestionItemListener(final QuestionItemListener listener) {
		item.addTrainerItemListener(this);
		listeners.add(listener);
	}

	@Override
	public void removeQuestionItemListener(final QuestionItemListener listener) {
		listeners.remove(listener);
		if (listeners.isEmpty()) {
			item.removeTrainerItemListener(this);
		}
	}

	@Override
	public void trainerItemChanged(final TrainerItem item) {
		for (final QuestionItemListener listener : listeners) {
			listener.questionItemChanged(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(direction, item);
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
		final BeanQuestionItem other = (BeanQuestionItem) obj;
		if (direction != other.direction) {
			return false;
		}
		if (!Objects.equals(item, other.item)) {
			return false;
		}
		return true;
	}
}