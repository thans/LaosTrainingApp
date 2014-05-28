package com.uwcse.morepractice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import au.com.bytecode.opencsv.CSVReader;

/**
 * A class that provides a static method for reading a quiz from a CSV file and returning a Quiz
 * object.
 * @author James
 */
public class CsvParser {

	/**
	 * Android logging tag.
	 */
	public static final String TAG = "CsvParser";
	
	public static final char DELIMITER = ',';
	public static final char ALTERNATE_DELIMITER = '\t';
	private static final char QUOTE = '"';
	private static final int LINE_NUM = 1;
	private static final int NUM_COLUMNS = 6;
	
	/**
	 * Reads a quiz stored in a CSV file and returns a Quiz object.
	 * @param path The path to the CSV file.
	 * @param delimiter The column delimiter character to use when parsing the file. 
	 * @return The Quiz object.
	 * @throws ParseException If there is some unexpected formatting in the CSV file. <code>e.getMessage()</code>
	 * returns a message with the line and column number where the unexpected item is found in the CSV file.
	 * @throws IOException If there is some error reading the file.
	 */
	public static Quiz parseQuizFromCsv(String path, char delimiter) throws QuizParseException, IOException {
		return parseQuizFromCsv(new File(path), delimiter);
	}
	
	/**
	 * Reads a quiz stored in a CSV file and returns a Quiz object.
	 * @param file A reference to the CSV file.
	 * @param delimiter The column delimiter character to use when parsing the file.
	 * @return The Quiz object.
	 * @throws ParseException If there is some unexpected formatting in the CSV file. <code>e.getMessage()</code>
	 * returns a message with the line and column number where the unexpected item is found in the CSV file.
	 * @throws IOException If there is some error reading the file.
	 */
	@SuppressWarnings("resource")
	public static Quiz parseQuizFromCsv(File file, char delimiter) throws QuizParseException, IOException {
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(file), delimiter, QUOTE, LINE_NUM);
		} catch (FileNotFoundException e) {
			reader = null;
			throw new IOException("The file " + file.getName() + " was not found.");
		}
		List<String[]> lines;
		try {
			lines = reader.readAll();
		} catch (IOException e) {
			lines = null;
			throw new IOException("Unable to read the file " + file.getName());
		}
		
		List<QuizQuestion> quizQuestions = new ArrayList<QuizQuestion>();
		int i = 0;
		while (true) {
			int excelLineNum = i + 2; // The line number in the Excel quiz
									  // file where this question begins
			String[] line1;
			String[] line2;
			String[] line3;
			String[] line4;
			try {
				line1 = lines.get(i++);
				line2 = lines.get(i++);
				line3 = lines.get(i++);
				line4 = lines.get(i++);
			} catch (IndexOutOfBoundsException e) {
				break;
			}
			// Question Number, Question, Answer, Correct Answer, Hint, Image
			// 0                1         2       3               4     5
			
			int questionNumber = 0;
			String question = "";
			List<String> answers = new ArrayList<String>();
			int correctAnswer = -1;
			String hint = "";
			String imageFile = "";
			
			if (line1.length < NUM_COLUMNS) {
				// If the number of tokens in the line does not match the expected
				// number of columns, one of two things may be occurring:
				// 1) The CSV file may be missing one of the expected columns, or
				// 2) the CSV may have been exported using the tab character
				//    as the column delimiter, instead of the comma character as expected.
				// Try to parse the quiz using the tab character as the delimiter,
				// and if it works, return the quiz, otherwise throw the original exception.
				try {
					// Attempt to the parse and return the quiz using the alternate delimiter
					Quiz quiz = parseQuizFromCsv(file, ALTERNATE_DELIMITER);
					return quiz;
				} catch (QuizParseException e){
					// If it fails, do nothing, and throw the original exception
				}
				throw new QuizParseException("The quiz file does not have "
						+ NUM_COLUMNS + " columns as expected. Please check that the \""
						+ DELIMITER + "\" character is used as the column delimiter when "
						+ "exporting the CSV file.");
			}
			
			// Parse the question number
			try {
				questionNumber = Integer.parseInt(line1[0]);
			} catch (NumberFormatException e) {
				questionNumber = 0;
			}
			// Parse the question text
			question = line1[1];
			if (question.equals("")) {
				throw new QuizParseException("The quiz file is missing a question. Please see line "
						+ excelLineNum + ", column B in " + file.getName() + ".");
			}
			// Parse the first answer choice
			if (line1[2].equals("")) {
				throw new QuizParseException("The quiz file is missing an answer. Please see line "
						+ excelLineNum + ", column C in " + file.getName() + ".");
			} else {
				answers.add(line1[2]);
			}
			// Parse the second answer choice
			if (line2[2].equals("")) {
				throw new QuizParseException("The quiz file is missing an answer. Please see line "
						+ (excelLineNum + 1) + ", column C in " + file.getName() + ".");
			} else {
				answers.add(line2[2]);
			}
			// Parse the third answer choice
			if (line3[2].equals("")) {
				throw new QuizParseException("The quiz file is missing an answer. Please see line "
						+ (excelLineNum + 2) + ", column C in " + file.getName() + ".");
			} else {
				answers.add(line3[2]);
			}
			// Parse the fourth answer choice
			if (line4[2].equals("")) {
				throw new QuizParseException("The quiz file is missing an answer. Please see line "
						+ (excelLineNum + 3) + ", column C in " + file.getName() + ".");
			} else {
				answers.add(line4[2]);
			}
			// Parse the correct answer number
			if (!line1[3].equals("")) {
				correctAnswer = 0;
			} else if (!line2[3].equals("")) {
				correctAnswer = 1;
			} else if (!line3[3].equals("")) {
				correctAnswer = 2;
			} else if (!line4[3].equals("")) {
				correctAnswer = 3;
			} else {
				throw new QuizParseException("The quiz file is missing a correct answer. Please see line "
						+ excelLineNum + ", column D in " + file.getName() + ".");
			}
			// Parse the hint, or keep the empty string if one is not provided
			hint = line1[4];
			// Parse the image file name, or keep the empty string if one is not provided
			imageFile = line1[5];
			
			QuizQuestion quizQuestion
					= new QuizQuestion(questionNumber, question, answers, correctAnswer, hint, imageFile);
			quizQuestions.add(quizQuestion);
		}
		Quiz quiz = new Quiz(quizQuestions);
		return quiz;
	}
	
	/**
	 * Reads a quiz stored in a CSV file, and returns a Quiz object
	 * @param path The path to the CSV file storing the quiz.
	 * @return The Quiz object.
	 */
	@Deprecated
	public static Quiz parseQuizFromCsv2(String path) throws ParseException {
		return parseQuizFromCsv2(new File(path));
	}
	
	/**
	 * Reads a quiz stored in a CSV file, and returns a Quiz object
	 * @param path A File reference to the CSV file storing the quiz.
	 * @return The Quiz object, or <code>null</code> if there was some error reading the quiz file.
	 */
	@Deprecated
	public static Quiz parseQuizFromCsv2(File file) throws ParseException {
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
				
//				String[] line = questionLines.get(i).split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				// http://stackoverflow.com/a/1757107
				
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
	
	/**
	 * An exception class for quiz parsing errors. 
	 */
	public static class QuizParseException extends Exception {
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param message The error message which attempts to report the line and column number
		 * in the Excel file where the unexpected item or formatting occurs.
		 */
		public QuizParseException(String message) {
			super(message);
		}
	}
	
}
