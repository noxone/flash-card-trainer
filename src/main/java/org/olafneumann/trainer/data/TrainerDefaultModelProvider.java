package org.olafneumann.trainer.data;

public interface TrainerDefaultModelProvider<M extends TrainerModel<?>> {
	M createModel();

	TrainerDefaultModelProvider<BeanTrainerModel> BEAN_TRAINER_MODEL_PROVIDER = () -> new BeanTrainerModel();
}
