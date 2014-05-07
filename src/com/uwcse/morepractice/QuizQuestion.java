package com.uwcse.morepractice;

import java.util.List;

/**
 * Represents a single quiz question. A quiz question has a question number,
 * the question text, answer choices, explanations for incorrect answers,
 * and a single correct answer.
 * @author James
 */
public class QuizQuestion {

	/**
	 * The number of answer choices available for each quiz question.
	 */
	public static final int NUM_ANSWERS = 4;
	
	private int questionNumber;
	private String question;
	private List<String> answers;
	private int correctAnswer;
	private List<String> explanations;
	
	/**
	 * Constructs a new QuizQuestion object, which represents a single quiz question.
	 * @param questionNumber The question number.
	 * @param quizQuestion The question text.
	 * @param answers A list of answer choices.
	 * @param correctAnswer The zero-based index of the correct answer choice.
	 * @param explanations A list of explanations for incorrect answer choices.
	 */
	public QuizQuestion(int questionNumber,
			String quizQuestion,
			List<String> answers,
			int correctAnswer,
			List<String> explanations) {
		this.questionNumber = questionNumber;
		this.question = quizQuestion;
		this.answers = answers;
		this.correctAnswer = correctAnswer;
		this.explanations = explanations;
	}
	
	/**
	 * Returns true if <code>answerNum</code> is the index of the correct
	 * answer, and false if it is the index of an incorrect answer.
	 * Zero-based indexing is used.
	 * @param answerNum The index of the answer to check for correctness.
	 * @return True if <code>answerNum</code> is the index of the correct
	 * answer, and false if it is the index of an incorrect answer.
	 * @throws IllegalArgumentException if <code>answerNum</code> does not
	 * represent a valid answer choice.
	 */
	public boolean isCorrectAnswer(int answerNum) {
		if (answerNum < 0 || answerNum >= NUM_ANSWERS) {
			throw new IllegalArgumentException("Invalid answer selection: "
					+ answerNum
					+ ". Valid answer numbers are 0 through "
					+ (NUM_ANSWERS - 1)
					+ ".");
		}
		return answerNum == this.correctAnswer;
	}
	
	/**
	 * @return This question's question number.
	 */
	public int getQuestionNumber() {
		return this.questionNumber;
	}
	
	/**
	 * @return This question's question text.
	 */
	public String getQuestionText() {
		return this.question;
	}
	
	/**
	 * @return A list of the answer choices.
	 */
	public List<String> getAnswers() {
		return this.answers;
	}
	
	/**
	 * @return A list of explanations for why each incorrect answer is incorrect.
	 */
	public List<String> getExplanations() {
		return this.explanations;
	}
	
	/**
	 * Returns a textual representation of this quiz question.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Question #" + questionNumber + "\n");
		sb.append(question + "\n");
		for (int i = 0; i < answers.size(); i++) {
			sb.append("\t#" + (i + 1) + ": " + answers.get(i) + "\n");
			sb.append("\t(" + explanations.get(i) + ")\n");
		}
		sb.append("Correct answer choice: " + (correctAnswer + 1));
		return sb.toString();
	}
}
