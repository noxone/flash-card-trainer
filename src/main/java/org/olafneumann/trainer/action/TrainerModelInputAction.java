package org.olafneumann.trainer.action;

import java.util.List;

import javax.swing.ImageIcon;

import org.olafneumann.trainer.data.TrainerModelInput;

public interface TrainerModelInputAction {
	ImageIcon getIcon();

	List<String> getTexts(String inputText);

	void performInputAction(TrainerModelInput input, String text);

	String getActionTooltip();
}
