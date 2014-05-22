package com.uwcse.morepractice;

import java.util.List;

/**
 * Represents a single quiz question. A quiz question has a question number,
 * the question text, answer choices, a question hint, and a single correct answer.
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
	private String hint;
	private String imageFileName;
	
	/**
	 * Constructs a new QuizQuestion object, which represents a single quiz question.
	 * @param questionNumber The question number.
	 * @param quizQuestion The question text.
	 * @param answers A list of answer choices.
	 * @param correctAnswer The zero-based index of the correct answer choice.
	 * @param hint A hint for the question.
	 */
	public QuizQuestion(int questionNumber,
			String quizQuestion,
			List<String> answers,
			int correctAnswer,
			String hint) {
		this.questionNumber = questionNumber;
		this.question = quizQuestion;
		this.answers = answers;
		this.correctAnswer = correctAnswer;
		this.hint = hint;
	}
	
	/**
	 * Constructs a new QuizQuestion object, which represents a single quiz question.
	 * @param questionNumber The question number.
	 * @param quizQuestion The question text.
	 * @param answers A list of answer choices.
	 * @param correctAnswer The zero-based index of the correct answer choice.
	 * @param hint A hint for the question.
	 * @param imageFilePath The file path to the image that should be displayed with
	 * this question.
	 */
	public QuizQuestion(int questionNumber,
			String quizQuestion,
			List<String> answers,
			int correctAnswer,
			String hint,
			String imageFilePath) {
		this.questionNumber = questionNumber;
		this.question = quizQuestion;
		this.answers = answers;
		this.correctAnswer = correctAnswer;
		this.hint = hint;
		this.imageFileName = imageFilePath;
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
	 * @return The index of the correct answer. Zero-based indexing is used.
	 */
	public int getCorrectAnswer() {
		return correctAnswer;
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
	 * @return A hint for the question.
	 */
	public String getHint() {
		return this.hint;
	}
	
	/**
	 * @return The file name of the image that goes with this question if one exists,
	 * otherwise the empty string.
	 */
	public String getImageFileName() {
		return this.imageFileName;
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
		}
		sb.append("Hint: " + hint + "\n");
		sb.append("Correct answer: " + (correctAnswer + 1));
		return sb.toString();
	}
}
