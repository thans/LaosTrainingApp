package com.uwcse.morepractice;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import android.util.Log;

/**
 * A class that provides a static method for reading a quiz from a CSV file and returning a Quiz object.
 * @author James
 */
public class CsvParser {

	/**
	 * Reads a quiz stored in a CSV file, and returns a Quiz object
	 * @param path The path to the CSV file storing the quiz.
	 * @return The Quiz object.
	 */
	public static Quiz parseQuizFromCsv(String path) {
		return parseQuizFromCsv(new File(path));
	}
	
	/**
	 * Reads a quiz stored in a CSV file, and returns a Quiz object
	 * @param path A File reference to the CSV file storing the quiz.
	 * @return The Quiz object, or <code>null</code> if there was some error reading the quiz file.
	 */
	public static Quiz parseQuizFromCsv(File file) {
		Scanner scanner;
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.err.println("File " + file.getName() + " not found.");
			return null;
		}
		
		if (scanner.hasNextLine()) {
			// Throw away the first line of the CSV file, which stores the column titles:
			// Question Number, Question, Answer, Correct Answer, Explanation
			scanner.nextLine();
		}
		
		List<QuizQuestion> quizQuestions = new ArrayList<QuizQuestion>();
		
		// If there is a next line, it is assumed that there are at least NUM_QUESTIONS more lines,
		// i.e. with four question quizzes, if there is a next line, there should be at least four
		// more lines
		outerloop:
		while (scanner.hasNextLine()) {
			
			// questionLines contains the lines that represent a single quiz question
			List<String> questionLines = new ArrayList<String>();
			
			for (int i = 0; i < QuizQuestion.NUM_ANSWERS; i++) {
				try {
					questionLines.add(scanner.nextLine());
				} catch (NoSuchElementException e) {
					// The scanner reached the end of the file while in the middle of reading
					// a quiz question, so abort adding this quiz question
					break outerloop;
				}
			}
			
			int questionNumber = 0;
			String question = "";
			List<String> answers = new ArrayList<String>();
			int correctAnswer = -1;
			String hint = "";
			String imageFile = "";
			
			// Read the first line of a quiz question, which stores the question number and the question
			String[] firstLine = questionLines.get(0).split(",");
			stripQuotationMarks(firstLine);
			try {
				questionNumber = Integer.parseInt(firstLine[0]);
			} catch (NumberFormatException e) {
				questionNumber = 0;
			}
			question = firstLine[1];
			answers.add(firstLine[2]);
			if (firstLine.length > 3 && firstLine[3].equals("1")) {
				correctAnswer = 0;
			}
			if (firstLine.length > 4) {
				hint = firstLine[4];
			}
			if (firstLine.length > 5) {
				imageFile = firstLine[5];
			}
			
			// Read the following lines of a quiz question
			for (int i = 1; i < QuizQuestion.NUM_ANSWERS; i++) {
				String[] line = questionLines.get(i).split(",");
				// TODO
				// Match commas, except commas that are found with an odd number of quotation marks
				// to the right, and to the left
				// i.e. All commas, except those between quotes
				
				stripQuotationMarks(line);
				answers.add(line[2]);
				if (line.length > 3 && line[3].equals("1")) {
					correctAnswer = i;
				}
			}
			
			if (correctAnswer == -1) {
				System.err.println("The CSV file is not properly formatted!");
				scanner.close();
				return null;
			}
			
			// Add a QuizQuestion to list of QuizQuestions
			QuizQuestion quizQuestion = new QuizQuestion(questionNumber,
					question, answers, correctAnswer, hint, imageFile);
			quizQuestions.add(quizQuestion);
		}
		// After the list of QuizQuestions has been created, create and return the Quiz object
		scanner.close();
		return new Quiz(quizQuestions);
	}
	
	/**
	 * Strips extraneous quotation marks from each String in the String array.
	 * @param tokens The list of Strings to strip.
	 */
	private static void stripQuotationMarks(String[] tokens) {
		for (String token : tokens) {
			stripQuotationMarksFromString(token);
		}
	}
	
	/**
	 * Strips extraneous quotation marks from a String. i.e. "Quiz question" ==> Quiz question
	 * @param string The String to strip.
	 */
	private static void stripQuotationMarksFromString(String string) {
		if (string == null || string.length() == 0) {
			return;
		}
		if (string.charAt(0) == '"'
				&& string.charAt(string.length() - 1) == '"') {
			string.substring(1, string.length() - 1);
		}
	}
	
}
