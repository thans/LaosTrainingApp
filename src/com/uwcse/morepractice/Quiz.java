package com.uwcse.morepractice;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Represents a quiz, which is composed of a list of <code>QuizQuestion</code>s.
 * This <code>Quiz</code> class implements <code>Iterator</code> for iterating
 * through its quiz questions. 
 * @author James
 */
public class Quiz implements Iterator<QuizQuestion> {

	private List<QuizQuestion> quizQuestions;
	private int current;
	
	/**
	 * Constructs a new <code>Quiz</code> object.
	 * @param quizQuestions A list of <code>QuizQuestion</code>
	 */
	public Quiz(List<QuizQuestion> quizQuestions) {
		this.quizQuestions = quizQuestions;
		this.current = 0;
	}
	
	/**
	 * @return The number of questions in this quiz.
	 */
	public int getNumQuestions() {
		return quizQuestions.size();
	}
	
	/**
	 * Returns true if there is another quiz question available, false if all
	 * of the questions have been iterated through.
	 */
	@Override
	public boolean hasNext() {
		return current < quizQuestions.size();
	}
	
	/**
	 * Advances the iterator to the next quiz question if one is available.
	 * @throws NoSuchElementException if the iterator has already iterated
	 * through of all of the quiz questions.
	 */
	@Override
	public QuizQuestion next() {
		if (!hasNext()) {
			throw new NoSuchElementException("Past the end of the list of quiz questions.");
		}
		QuizQuestion toReturn = quizQuestions.get(current);
		current++;
		return toReturn;
	}

	/**
	 * The remove operation is not supported by this iterator!
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("The remove operation is not supported by this iterator.");
	}
	
	/**
	 * Returns a textual representation of all of the quiz questions in this quiz.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (QuizQuestion question : quizQuestions) {
			sb.append(question.toString() + "\n\n");
		}
		return sb.toString();
	}

}
