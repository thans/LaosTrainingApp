package com.example.laostrainingapp;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Quiz implements Iterator<QuizQuestion> {

	private List<QuizQuestion> quizQuestions;
	private int current;
	
	public Quiz(List<QuizQuestion> quizQuestions) {
		this.quizQuestions = quizQuestions;
		this.current = 0;
	}
	
	public int getNumQuestions() {
		return quizQuestions.size();
	}
	
	@Override
	public boolean hasNext() {
		return current < quizQuestions.size();
	}
	
	@Override
	public QuizQuestion next() {
		if (!hasNext()) {
			throw new NoSuchElementException("Past the end of the list of quiz questions.");
		}
		QuizQuestion toReturn = quizQuestions.get(current);
		current++;
		return toReturn;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("The remove operation is not supported by this iterator.");
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (QuizQuestion question : quizQuestions) {
			sb.append(question.toString() + "\n\n");
		}
		return sb.toString();
	}

}
