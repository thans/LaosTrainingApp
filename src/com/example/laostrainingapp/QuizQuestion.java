package com.example.laostrainingapp;

import java.util.List;

/**
 * Represents a single quiz question. A quiz question has a question number, answer choices, explanations
 * for incorrect answers, and a correct answer.
 * @author James
 */
public class QuizQuestion {

	public static final int NUM_ANSWERS = 4;
	
	private int questionNumber;
	private String question;
	private List<String> answers;
	private int correctAnswer;
	private List<String> explanations;
	
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
	
	public boolean isCorrectAnswer(int answerNum) {
		if (answerNum < 1 || answerNum > NUM_ANSWERS) {
			throw new IllegalArgumentException("Invalid answer selection: "
					+ answerNum
					+ ". Valid answer numbers are 1 through "
					+ NUM_ANSWERS
					+ ".");
		}
		return answerNum == this.correctAnswer;
	}
	
	public int getQuestionNumber() {
		return this.questionNumber;
	}
	
	public String getQuestion() {
		return this.question;
	}
	
	public List<String> getAnswers() {
		return this.answers;
	}
	
	public List<String> getExplanations() {
		return this.explanations;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Question #" + questionNumber + "\n");
		sb.append(question + "\n");
		for (int i = 1; i <= answers.size(); i++) {
			sb.append("\t#" + i + ": " + answers.get(i) + "\n");
			sb.append("\t(" + explanations.get(i) + ")\n");
		}
		sb.append("Correct answer choice: " + correctAnswer);
		return sb.toString();
	}
}
