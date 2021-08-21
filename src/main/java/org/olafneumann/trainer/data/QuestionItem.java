package org.olafneumann.trainer.data;

public interface QuestionItem {
	String getQuestion();

	String[] getAnswers();

	QuestionItem getNext(boolean known);

	TrainerModelInput getQuestionInput();

	TrainerModelInput[] getAnswerInputs();

	void addQuestionItemListener(QuestionItemListener listener);

	void removeQuestionItemListener(QuestionItemListener listener);

}
