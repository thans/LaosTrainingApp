package com.uwcse.morepractice;

import java.io.File;
import java.util.List;

import com.uwcse.morepractice.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The quiz activity. In progress!
 * @author James
 */
public class QuizActivity extends Activity {

	private static final String BASE_DIRECTORY = "LaosTrainingApp";
	
	/**
	 * Used to pass in the file name of the quiz file. Currently, this activity
	 * attempts to locate this file at "sdcard/LaosTrainingApp/<QuizFileName>"
	 */
	public static final String QUIZ_FILE_NAME_KEY = "QuizFileName";
	
	/**
	 * Used to pass in the full file path of the quiz file.
	 */
	public static final String QUIZ_FILE_FULL_PATH_KEY = "QuizFileFullPath";
	
	// UI references
	private TextView mQuestionNumber;
	private TextView mQuestion;
	private TextView mHint;
	private Button mAnswer1;
	private Button mAnswer2;
	private Button mAnswer3;
	private Button mAnswer4;
	
	private Quiz quiz;
	private QuizQuestion currentQuestion;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quiz);
		
		// Check that the external storage directory is readable
		String sdcardDirState = Environment.getExternalStorageState();
		if (!(sdcardDirState.equals(Environment.MEDIA_MOUNTED) || sdcardDirState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
			// Show a toast to alert if the external storage directory is not readable,
			// then close this activity
			System.err.println("Unable to read from the external storage directory.");
			String text = "Unable to read from the external storage directory.";
			makeToast(getApplicationContext(), text);
			this.finish();
		}
		
		// Get the file path of the quiz file from the bundle
		Bundle extras = getIntent().getExtras();
		String quizFileName = extras.getString(QUIZ_FILE_NAME_KEY);
		String quizFileFullPath = extras.getString(QUIZ_FILE_FULL_PATH_KEY);
		
		String quizFilePath = null;
		// If the quiz's file name has been passed in, locate it at
		// "sdcard/LaosTrainingApp/<QuizFileName>
		if (!(quizFileName == null)) {
			File root = Environment.getExternalStorageDirectory();
			String rootPath = root.getPath(); // The SD card directory
			String appDirectoryPath = rootPath + "/" + BASE_DIRECTORY; // The "LaosTrainingApp" directory
			quizFilePath = appDirectoryPath + "/" + quizFileName; // The quiz file path
		}
		// If the quiz's full file path has been passed in, find the
		// quiz file using this full file path
		else if (!(quizFileFullPath == null)) {
			quizFilePath = quizFileFullPath;
		}
		// The file path has not been passed in, so close this activity
		else {
			System.err.println("The quiz's file path was not passed into this activity.");
			String text = "The quiz's file path was not passed into this activity.";
			makeToast(getApplicationContext(), text);
			this.finish();
		}
		
		// Check that the quiz file is readable
		File quizFile = new File(quizFilePath);
		if (!quizFile.exists() || !quizFile.isFile() || !quizFile.canRead()) {
			// Show a toast if the quiz file could not be found,
			// then close this activity
			System.err.println("Could not find the file " + quizFilePath);
			String text = "Could not find the file " + quizFilePath;
			makeToast(getApplicationContext(), text);
			this.finish();
		}
		
		// Parse a Quiz object from the CSV file
		this.quiz = CsvParser.parseQuizFromCsv(quizFilePath);
		if (quiz == null) {
			// Parsing error, close the activity
			System.err.println("The quiz file is not properly formatted! " + quizFilePath);
			String text = "The quiz file is not properly formatted! " + quizFilePath;
			makeToast(getApplicationContext(), text);
			this.finish();
		}
		
		// Create the UI references
		mQuestionNumber = (TextView) findViewById(R.id.quiz_question_number);
		mQuestion = (TextView) findViewById(R.id.quiz_question);
		mHint = (TextView) findViewById(R.id.quiz_hint);
		mAnswer1 = (Button) findViewById(R.id.quiz_answer_1);
		mAnswer2 = (Button) findViewById(R.id.quiz_answer_2);
		mAnswer3 = (Button) findViewById(R.id.quiz_answer_3);
		mAnswer4 = (Button) findViewById(R.id.quiz_answer_4);

		if (quiz.hasNext()) {
			// Set the question text
			this.currentQuestion = quiz.next();
			mQuestion.setText(currentQuestion.getQuestionText());
			
			// Set the answer buttons' text
			List<String> answers = currentQuestion.getAnswers();
			mAnswer1.setText(answers.get(0));
			mAnswer2.setText(answers.get(1));
			mAnswer3.setText(answers.get(2));
			mAnswer4.setText(answers.get(3));
		}
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	/**
	 * Called when an answer choice button is selected.
	 * @param view A reference to the View that was clicked.
	 */
	public void buttonOnClick(View view) {
		switch (view.getId()) {
	      case R.id.quiz_answer_1:
	        respondToAnswerSelection(0);
	        break;
	      case R.id.quiz_answer_2:
	        respondToAnswerSelection(1);
	        break;
	      case R.id.quiz_answer_3:
	        respondToAnswerSelection(2);
	        break;
	      case R.id.quiz_answer_4:
	        respondToAnswerSelection(3);
	        break;
	      }
	}
	
	/**
	 * Responds to an answer selection. If the selected answer is incorrect, the explanation will
	 * be displayed. If the selected answer is correct, the following question will be displayed.
	 * @param answerNum
	 */
	public void respondToAnswerSelection(int answerNum) {
		if (currentQuestion.isCorrectAnswer(answerNum)) {
			makeToast(getApplicationContext(), "Correct!");
			setNextQuestion();
		} else {
			mHint.setText(currentQuestion.getHint());
		}
		
	}
	
	/**
	 * Displays the next question in the UI. If there are no more questions, the activity is closed.
	 */
	public void setNextQuestion() {
		if (quiz.hasNext()) {
			currentQuestion = quiz.next();
			
			// Set the question text
			mQuestion.setText(currentQuestion.getQuestionText());
			
			// Set the answer buttons' text
			List<String> answers = currentQuestion.getAnswers();
			mAnswer1.setText(answers.get(0));
			mAnswer2.setText(answers.get(1));
			mAnswer3.setText(answers.get(2));
			mAnswer4.setText(answers.get(3));
			
			// Clear the question hint
			mHint.setText("");
		} else { // All questions have been answered
			makeToast(getApplicationContext(), "You've completed this quiz successfully.");
			this.finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.quiz, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);
			return rootView;
		}
	}
	
	/**
	 * Displays a toast message.
	 * @param context The application context, i.e. <code>getApplicationContext()</code>
	 * @param text The text to display in the toast.
	 */
	public static void makeToast(Context context, String text) {
		// TODO Move this into a utility class to use as a convenience method
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

}
