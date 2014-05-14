package com.uwcse.morepractice;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The quiz activity. In progress!
 * @author James
 */
public class QuizActivity extends Activity {

	private static final String TAG = "QuizActivity";
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
	
	public static final int QUESTION_FEEDBACK_DELAY = 1500;
	
	public static final int GET_QUIZ_SCORE_REQUEST = 1;
	public static final String QUIZ_SCORE_KEY = "QuizScore";
	
	// UI references
	private TextView mQuestionNumber;
	private TextView mQuestion;
	private TextView mHint;
	private TextView mQuestionFeedback;
	private Button mAnswer1;
	private Button mAnswer2;
	private Button mAnswer3;
	private Button mAnswer4;
	private ImageView mImageView;
	
	private String quizFilePath;
	private Quiz mQuiz;
	private QuizQuestion mCurrentQuestion;
	
	/**
	 * The maximum possible quiz score the user can receive.
	 */
	private int mMaxScore;
	
	/**
	 * The user's running quiz score. Two points are added for a first-attempt
	 * correct answer, and one point is added for a second-attempt correct answer.
	 * This is later presented to the user as one point for a first-attempt
	 * correct answer, and a half point for a second-attempt correct answer.
	 */
	private int mTotalScore;
	
	/**
	 * Set to true if the user has answered a quiz question incorrectly. The user has two
	 * chances to answer a quiz question. 
	 */
	private boolean mAnsweredIncorrectly;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quiz);
		
		mAnsweredIncorrectly = false;
		mMaxScore = 0;
		mTotalScore = 0;
		
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
		
		quizFilePath = null;
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
		this.mQuiz = CsvParser.parseQuizFromCsv(quizFilePath);
		if (mQuiz == null) {
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
		mQuestionFeedback = (TextView) findViewById(R.id.quiz_feedback);
		mAnswer1 = (Button) findViewById(R.id.quiz_answer_1);
		mAnswer2 = (Button) findViewById(R.id.quiz_answer_2);
		mAnswer3 = (Button) findViewById(R.id.quiz_answer_3);
		mAnswer4 = (Button) findViewById(R.id.quiz_answer_4);
		mImageView = new ImageView(this);

		mMaxScore = 2 * mQuiz.getNumQuestions();
		setNextQuestion();
		
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
	private void respondToAnswerSelection(int answerNum) {
		if (mCurrentQuestion.isCorrectAnswer(answerNum)) {
			if (mAnsweredIncorrectly == false) {
				// This is the user's first attempt on the question,
				// so the user gets a full point
				mQuestionFeedback.setText(R.string.plus_one);
				mTotalScore += 2;
			} else {
				// This is the user's second attempt on the question,
				// so the user gets a half point
				mQuestionFeedback.setText(R.string.plus_one_half);
				mTotalScore += 1;
			}
			mAnsweredIncorrectly = false;
			
			// Pause before setting the next question
			Handler handler = new Handler(); 
		    handler.postDelayed(new Runnable() { 
		         public void run() { 
		              setNextQuestion(); 
		         }
		    }, QUESTION_FEEDBACK_DELAY); 
		} else {
			if (mAnsweredIncorrectly == false) {
				// This is the user's first attempt on this question,
				// so set the hint
				mAnsweredIncorrectly = true;
				mHint.setText(mCurrentQuestion.getHint());
				mQuestionFeedback.setText(R.string.please_try_again);
			} else {
				// This is the user's second and final attempt on this question,
				// so show "Incorrect" and set the next question
				mAnsweredIncorrectly = false;
				mQuestionFeedback.setText(R.string.plus_zero);
				
				// Pause before setting the next question
				Handler handler = new Handler(); 
			    handler.postDelayed(new Runnable() { 
			         public void run() { 
			              setNextQuestion(); 
			         }
			    }, QUESTION_FEEDBACK_DELAY); 
			}
		}
	}
	
	/**
	 * Displays the next question in the UI. If there are no more questions, the activity is closed.
	 */
	private void setNextQuestion() {
		if (mQuiz.hasNext()) {
			mCurrentQuestion = mQuiz.next();
			
			// Clear the previous question's feedback, hint, and image
			mHint.setText("");
			mQuestionFeedback.setText("");
			mImageView.setImageBitmap(null);
			
			// Set the question number and question text
			int questionNumber = mCurrentQuestion.getQuestionNumber();
			mQuestionNumber.setText("#" + questionNumber);
			mQuestion.setText(mCurrentQuestion.getQuestionText());
			
			// Set the answer buttons' text
			List<String> answers = mCurrentQuestion.getAnswers();
			mAnswer1.setText(answers.get(0));
			mAnswer2.setText(answers.get(1));
			mAnswer3.setText(answers.get(2));
			mAnswer4.setText(answers.get(3));
			
			setImage();
		} else { // All questions have been answered
			this.finish();
		}
	}
	
	/**
	 * @param imageFileName The image's file name, i.e. "fridge_tag.jpg"
	 * @return The image file's full path, i.e.
	 * "/sdcard/LaosTrainingApp/Fridge Tag Package/Fridge Tag Quiz/fridge_tag.jpg" 
	 */
	private String getImageFullPath(String imageFileName) {
		// A quiz's images are expected to be placed in a directory
		// with the exact same name as the quiz, minus ".csv"
		
		// Remove the last period, add a slash, and add the image file name
		int indexOfPeriod = quizFilePath.lastIndexOf(".");
		String imageDirPath = quizFilePath.substring(0, indexOfPeriod);
		return imageDirPath + "/" + imageFileName;
	}
	
	private void setImage() {
		if (mCurrentQuestion.getImageFileName() == null ||
				mCurrentQuestion.getImageFileName() == "") {
			return;
		}
        String imageFileName = mCurrentQuestion.getImageFileName();
        String imageFullPath = getImageFullPath(imageFileName);

        File file = new File(imageFullPath);
        if (!file.exists()) {
        	Log.e(TAG, "The image file for question #" + mCurrentQuestion.getQuestionNumber()
        			+ " does not exist: " + imageFullPath);
        } else if (!file.canRead()) {
        	Log.e(TAG, "The image file for question #" + mCurrentQuestion.getQuestionNumber()
        			+ " is not readable: " + imageFullPath);
        } else {
        	Log.v(TAG, "Loading image for question #" + mCurrentQuestion.getQuestionNumber()
        			+ ":" + imageFullPath);
        }
        
		LinearLayout layout = (LinearLayout) findViewById(R.id.quiz_left_column);
        mImageView = new ImageView(this);
        Bitmap myBitmap = BitmapFactory.decodeFile(imageFullPath);
        mImageView.setImageBitmap(myBitmap);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mImageView.setLayoutParams(params);
        layout.addView(mImageView);
        mImageView.requestFocus();
	}

	@Override
	public void finish() {
	  if (mMaxScore == 0) {
		  // There was some error loading the quiz
		  setResult(RESULT_CANCELED);
		  super.finish();
	  } else {
		  // Pass the user's quiz score back to the calling activity
		  Intent data = new Intent();
		  data.putExtra(QUIZ_SCORE_KEY, getQuizScoreString());
		  setResult(RESULT_OK, data);
		  super.finish();
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

	private String getQuizScoreString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mTotalScore / 2);
		if (mTotalScore % 2 == 1) {
			sb.append("\u00BD"); // "1/2" symbol
		}
		sb.append(" out of ");
		sb.append(mMaxScore / 2);
		return sb.toString();
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
