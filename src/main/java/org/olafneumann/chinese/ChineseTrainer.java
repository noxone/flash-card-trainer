package org.olafneumann.chinese;

import org.olafneumann.trainer.Settings;
import org.olafneumann.trainer.Trainer;
import org.olafneumann.trainer.data.BeanTrainerItem;
import org.olafneumann.trainer.data.BeanTrainerModel;
import org.olafneumann.trainer.data.TrainerModel;

public class ChineseTrainer extends Trainer<BeanTrainerItem, BeanTrainerModel> {
	public static void main(final String[] args) {
		new ChineseTrainer().startApplication(getFileFromArguments(args));
	}

	@Override
	public BeanTrainerModel createModel() {
		return new BeanTrainerModel();
	}

	@Override
	protected Class<? extends TrainerModel<?>> getModelClass() {
		return BeanTrainerModel.class;
	}

	@Override
	protected void startUi(final Settings settings, final TrainerModel<?> model) {
		super.startUi(settings, model);
	}
}
