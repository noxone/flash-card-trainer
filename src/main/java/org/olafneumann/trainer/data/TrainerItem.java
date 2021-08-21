package org.olafneumann.trainer.data;

public interface TrainerItem {
	void setValue(int index, String value);

	String[] getValues();

	void setKnown(int index, boolean known);

	boolean isKnown(int index);

	void addTrainerItemListener(TrainerItemListener listener);

	void removeTrainerItemListener(TrainerItemListener listener);

	boolean isEmpty();
}
