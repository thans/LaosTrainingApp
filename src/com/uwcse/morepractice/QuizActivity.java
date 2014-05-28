package com.uwcse.morepractice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.uwcse.morepractice.CsvParser.QuizParseException;

/**
 * The quiz activity. In progress!
 * @author James
 */
public class QuizActivity extends Activity {

	/**
	 * Android logging tag.
	 */
	private static final String TAG = "QuizActivity";
	
	/**
	 * Key used to pass in the full file path of the CSV quiz file.
	 */
	public static final String QUIZ_FILE_FULL_PATH_KEY = "QuizFileFullPath";

	/**
	 * Key used to pass in the file name of the CSV quiz file. Currently, this activity attempts to
	 * locate this file at "/sdcard/[BASE_DIRECTORY]/[QuizFileName]"
	 */
	public static final String QUIZ_FILE_NAME_KEY = "QuizFileName";

	/**
	 * The application's base directory.
	 */
	private static final String BASE_DIRECTORY = "LaosTrainingApp";

	/**
	 * The request code used to request a quiz score result from this activity.
	 */
	public static final int GET_QUIZ_SCORE_REQUEST = 1;

	/**
	 * Key used to pass the user's quiz score back to the calling activity.
	 */
	public static final String QUIZ_SCORE_KEY = "QuizScore";
	
	/**
	 * Key used to pass to the calling activity the number of star icons to display with the user's
	 * quiz score, out of five possible stars. 
	 */
	public static final String QUIZ_SCORE_STAR_KEY = "QuizScoreStars";
	
	/**
	 * Key used to pass a helpful error message back to the calling activity. If there is some error
	 * parsing the CSV quiz file, the error message attempts to indicate the line and column
	 * number where the parsing error occurs.
	 */
	public static final String QUIZ_ERROR_MSG_KEY = "QuizError";

	/**
	 * The delay between a user's answer selection button press and the loading of the next
	 * question.
	 */
	public static final int QUESTION_FEEDBACK_DELAY = 1500;
	
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
	private Button mNextButton;
	
	/**
	 * The file path of the CSV quiz file to read.
	 */
	private String mQuizFilePath;
	
	/**
	 * The quiz object, parsed from the CSV quiz file.
	 */
	private Quiz mQuiz;
	
	/**
	 * The current quiz question displayed in the UI.
	 */
	private QuizQuestion mCurrentQuestion;
	
	/**
	 * The maximum possible quiz score the user can receive.
	 */
	private int mMaxScore;
	
	/**
	 * The user's running quiz score. Two points are added for a first-attempt correct answer, and
	 * one point is added for a second-attempt correct answer. This is later presented to the user
	 * as one point for a first-attempt correct answer, and a half point for a second-attempt
	 * correct answer.
	 */
	private int mTotalScore;
	
	/**
	 * Used to keep track of the user's two attempts to answer a quiz question. Set to true if the
	 * user answers a question incorrectly on their first attempt.
	 */
	private boolean mAnsweredIncorrectly;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quiz);
		
		mMaxScore = 0;
		mTotalScore = 0;
		mAnsweredIncorrectly = false;
		
		// Check that the external storage directory is readable
		String sdcardDirState = Environment.getExternalStorageState();
		if (!(sdcardDirState.equals(Environment.MEDIA_MOUNTED)
				|| sdcardDirState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
			// Show a toast to alert that the external storage directory is not readable,
			// then close this activity
			Log.e(TAG, "Unable to read from the external storage directory.");
			this.finish();
		}
		
		// Get the file path of the quiz file from the bundle
		Bundle extras = getIntent().getExtras();
		String quizFileName = extras.getString(QUIZ_FILE_NAME_KEY);
		String quizFileFullPath = extras.getString(QUIZ_FILE_FULL_PATH_KEY);
		
		mQuizFilePath = null;
		// If the quiz's file name has been passed in, locate it at
		// "/sdcard/[BASE_DIRECTORY]/<QuizFileName>
		if (!(quizFileName == null)) {
			File root = Environment.getExternalStorageDirectory();
			String rootPath = root.getPath(); // The SD card directory
			String appBaseDirectoryPath = rootPath + "/" + BASE_DIRECTORY; // The base directory
			mQuizFilePath = appBaseDirectoryPath + "/" + quizFileName; // The full quiz file path
		}
		// If the quiz's full file path has been passed in, find the
		// quiz file using this full file path
		else if (!(quizFileFullPath == null)) {
			mQuizFilePath = quizFileFullPath;
		}
		// The file path has not been passed in, so close this activity
		else {
			Log.e(TAG, "The quiz's file path was not passed into this activity.");
			this.finish();
		}
		
		// Check that the quiz file is readable
		File quizFile = new File(mQuizFilePath);
		if (!quizFile.exists() || !quizFile.isFile() || !quizFile.canRead()) {
			// Show a toast if the quiz file could not be found,
			// then close this activity
			Log.e(TAG, "Could not find the CSV quiz file " + mQuizFilePath);
			this.finish();
		}
		
		// Parse a Quiz object from the CSV file
		try {
			this.mQuiz = CsvParser.parseQuizFromCsv(mQuizFilePath, CsvParser.DELIMITER);
		} catch (QuizParseException e) {
			// If there is a parsing error, pass the error message to the calling activity so that
			// it may be displayed in the UI for the quiz file writer.
			Log.e(TAG, "Error parsing the CSV quiz file.");
			Log.e(TAG, e.getMessage());
			this.finishResultNotOk(e.getMessage());
			return;
		} catch (IOException e) {
			Log.e(TAG, "Error reading the CSV quiz file.");
			Log.e(TAG, e.getMessage());
			this.finishResultNotOk(e.getMessage());
			return;
		}
		
		if (mQuiz == null) {
			// Parsing error, close the activity
			Log.e(TAG, "Error parsing the CSV quiz file. It is not properly formatted");
			Log.e(TAG, "Quiz file: " + mQuizFilePath);
			this.finish();
		}
		mMaxScore = 2 * mQuiz.getNumQuestions();
		
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
		mNextButton = (Button) findViewById(R.id.quiz_next);

		// Set the first quiz question
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
	    	mAnswer1.setEnabled(false);
	    	mAnswer1.setTextColor(Color.GRAY);
	        respondToAnswerSelection(0);
	        break;
	      case R.id.quiz_answer_2:
	    	mAnswer2.setEnabled(false);
	    	mAnswer2.setTextColor(Color.GRAY);
	        respondToAnswerSelection(1);
	        break;
	      case R.id.quiz_answer_3:
	    	mAnswer3.setEnabled(false);
	    	mAnswer3.setTextColor(Color.GRAY);
	        respondToAnswerSelection(2);
	        break;
	      case R.id.quiz_answer_4:
	    	mAnswer4.setEnabled(false);
	    	mAnswer4.setTextColor(Color.GRAY);
	        respondToAnswerSelection(3);
	        break;
	      case R.id.quiz_next:
	    	setNextQuestion();
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
				onFirstAttemptCorrectAnswer();
			} else {
				onSecondAttemptCorrectAnswer();
			}
			mAnsweredIncorrectly = false;
		} else {
			if (mAnsweredIncorrectly == false) {
				onFirstAttemptIncorrectAnswer();
			} else {
				onSecondAttemptIncorrectAnswer();
			}
		}
	}
	
	private void onFirstAttemptCorrectAnswer() {
		// This is the user's first attempt on the question,
		// so the user gets a full point
		mQuestionFeedback.setText(R.string.plus_one);
		mTotalScore += 2;
		disableAnswerButtons();
		mNextButton.setEnabled(true);
		mNextButton.setTextColor(Color.WHITE);
		showCorrectAnswer();
	}
	
	private void onSecondAttemptCorrectAnswer() {
		// This is the user's second attempt on the question,
		// so the user gets a half point
		mQuestionFeedback.setText(R.string.plus_one_half);
		mTotalScore += 1;
		disableAnswerButtons();
		mNextButton.setEnabled(true);
		mNextButton.setTextColor(Color.WHITE);
		showCorrectAnswer();
	}
	
	private void onFirstAttemptIncorrectAnswer() {
		// This is the user's first attempt on this question,
		// so set the hint
		mAnsweredIncorrectly = true;
		if (mCurrentQuestion.getHint() == null || mCurrentQuestion.getHint().equals("")) {
			// If a hint was not supplied, show "Please try again"
			mHint.setText(getResources().getString(R.string.please_try_again));
		} else {
			// If a hint was supplied, show it
			mHint.setText(getResources().getString(R.string.hint) + ": " + mCurrentQuestion.getHint());
		}
	}
	
	private void onSecondAttemptIncorrectAnswer() {
		// This is the user's second and final attempt on this question,
		// so show "Incorrect" and set the next question
		mAnsweredIncorrectly = false;
		mQuestionFeedback.setText(R.string.plus_zero);
		disableAnswerButtons();
		mNextButton.setEnabled(true);
		mNextButton.setTextColor(Color.WHITE);
		
		// Show the correct answer
		showCorrectAnswer();
	}
	
	private void showCorrectAnswer() {
		int correctAnswer = mCurrentQuestion.getCorrectAnswer();
		switch (correctAnswer) {
		case 0:
			mAnswer1.setTypeface(null, Typeface.BOLD);
			mAnswer1.setTextColor(Color.WHITE);
			break;
		case 1:
			mAnswer2.setTypeface(null, Typeface.BOLD);
			mAnswer2.setTextColor(Color.WHITE);
			break;
		case 2:
			mAnswer3.setTypeface(null, Typeface.BOLD);
			mAnswer3.setTextColor(Color.WHITE);
			break;
		case 3:
			mAnswer4.setTypeface(null, Typeface.BOLD);
			mAnswer4.setTextColor(Color.WHITE);
			break;
		default:
			break;
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
			Log.v(TAG, "Setting question number " + questionNumber);
			mQuestionNumber.setText("#" + questionNumber);
			mQuestion.setText(mCurrentQuestion.getQuestionText());
			
			// Set the answer buttons' text
			List<String> answers = mCurrentQuestion.getAnswers();
			mAnswer1.setText(answers.get(0));
			mAnswer2.setText(answers.get(1));
			mAnswer3.setText(answers.get(2));
			mAnswer4.setText(answers.get(3));
			
			mNextButton.setEnabled(false);
			mNextButton.setTextColor(Color.GRAY);
			enableAnswerButtons();
			setImage();
		} else { // All questions have been answered
			this.finishResultOk();
		}
	}
	
	/**
	 * If the quiz question has a corresponding image file, display it in the UI. If it does not,
	 * do nothing.
	 */
	private void setImage() {
		if (mCurrentQuestion.getImageFileName() == null ||
				mCurrentQuestion.getImageFileName() == "") {
			Log.v(TAG, "No image file for question number " + mCurrentQuestion.getQuestionNumber());
			return;
		}
        String imageFileName = mCurrentQuestion.getImageFileName();
        String imageFullPath = getImageFullPath(imageFileName);
        
        if (imageFullPath == null || imageFullPath == "") {
        	Log.v(TAG, "No image file for question number " + mCurrentQuestion.getQuestionNumber());
        	return;
        }

		RelativeLayout layout = (RelativeLayout) findViewById(R.id.quiz_left_column);
        mImageView = new ImageView(this);
        Bitmap myBitmap = decodeFile(new File(imageFullPath));
        mImageView.setImageBitmap(myBitmap);
        RelativeLayout.LayoutParams params
        		= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        				ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, mHint.getId());
        mImageView.setLayoutParams(params);
        layout.addView(mImageView);
        mImageView.requestFocus();
        Log.v(TAG, "Set the image for question number " + mCurrentQuestion.getQuestionNumber());
	}

	/**
	 * A method used to scale Bitmaps down to a manageable size. Necessary for avoiding
	 * OutOfMemoryErrors when loading many large image files. 
	 */
	private Bitmap decodeFile(File f){
		/*
		Courtesy: http://stackoverflow.com/questions/477572/
		strange-out-of-memory-issue-while-loading-an-image-to-a-bitmap-object/823966#823966
		*/
	    try {
	        // Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f), null, o);

	        // The new size we want to scale to
	        final int REQUIRED_SIZE = 550;

	        // Find the correct scale value. It should be a power of 2.
	        int scale = 1;
	        while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
	            scale *= 2;
	    	}

	        // Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	    } catch (FileNotFoundException e) {}
	    return null;
	}
		
	/**
	 * Takes the file name of an image and returns its full path. Within a package directory,
	 * there should be a single directory that contains quiz image files. This method locates
	 * the image within this directory. If the image file exists and is readable, its full path
	 * is returned. Otherwise, the empty string is returned.
	 * @param imageFileName The file name of the image.
	 * @return The full path of the image file.
	 */
	private String getImageFullPath(String imageFileName) {
		int indexOfLastSlash = mQuizFilePath.lastIndexOf("/");
		String packageDirectoryPath = mQuizFilePath.substring(0, indexOfLastSlash);
		File packageDirectory = new File(packageDirectoryPath);
		File[] packageDirectoryFiles = packageDirectory.listFiles();
		for (File file : packageDirectoryFiles) {
			// Look for a directory within the package directory. Only one should exist.
			if (file.isDirectory()) {
				String imageFullPath = file.getPath() + "/" + imageFileName;
				File image = new File(imageFullPath);
				if (image.exists() && image.canRead()) {
					return imageFullPath;
				} else {
					break;
				}
			}
		}
		return "";
	}
	
	/**
	 * @param imageFileName The image's file name, i.e. "fridge_tag.jpg"
	 * @return The image file's full path, i.e.
	 * "/sdcard/LaosTrainingApp/Fridge Tag Package/Fridge Tag Quiz/fridge_tag.jpg" 
	 */
	@Deprecated
	private String getImageFullPath2(String imageFileName) {
		// A quiz's images are expected to be placed in a directory
		// with the exact same name as the quiz, minus ".csv"
		
		// Remove the last period, add a slash, and add the image file name
		int indexOfPeriod = mQuizFilePath.lastIndexOf(".");
		String imageDirPath = mQuizFilePath.substring(0, indexOfPeriod);
		return imageDirPath + "/" + imageFileName;
	}

	/**
	 * Finish this activity with RESULT_OK, and return the user's quiz score.
	 */
	public void finishResultOk() {
		Intent data = new Intent();
		data.putExtra(QUIZ_SCORE_KEY, getQuizScoreString());
		data.putExtra(QUIZ_SCORE_STAR_KEY, getQuizScoreNumStars());
		setResult(RESULT_OK, data);
		finish();
	}
	
	/**
	 * Finish this activity with RESULT_CANCELLED, and return the helpful error message.
	 * @param errorMsg The error message to return to the calling activity.
	 */
	public void finishResultNotOk(String errorMsg) {
		Intent data = new Intent();
		data.putExtra(QUIZ_ERROR_MSG_KEY, errorMsg);
		setResult(RESULT_CANCELED, data);
		finish();
	}
	
	/**
	 * @return The user's quiz score as a String.
	 */
	private String getQuizScoreString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mTotalScore / 2);
		if (mTotalScore % 2 == 1) {
			sb.append("\u00BD"); // "1/2" symbol
		}
		sb.append(" " + getResources().getString(R.string.out_of) + " ");
		sb.append(mMaxScore / 2);
		return sb.toString();
	}
	
	/**
	 * @return Return the number of star icons to display with the user's score, out of five stars
	 * possible.
	 */
	private int getQuizScoreNumStars() {
		double score = (double) mTotalScore / (double) mMaxScore;
		if (score >= 0.8) {
			return 5;
		} else if (score >= 0.6) {
			return 4;
		} else if (score >= 0.4) {
			return 3;
		} else if (score >= 0.2) {
			return 2;
		} else if (score > 0){
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Enable all of the answer selection buttons.
	 */
	private void enableAnswerButtons() {
		mAnswer1.setEnabled(true);
		mAnswer2.setEnabled(true);
		mAnswer3.setEnabled(true);
		mAnswer4.setEnabled(true);
		mAnswer1.setTextColor(Color.WHITE);
		mAnswer2.setTextColor(Color.WHITE);
		mAnswer3.setTextColor(Color.WHITE);
		mAnswer4.setTextColor(Color.WHITE);
		mAnswer1.setTypeface(null, Typeface.NORMAL);
		mAnswer2.setTypeface(null, Typeface.NORMAL);
		mAnswer3.setTypeface(null, Typeface.NORMAL);
		mAnswer4.setTypeface(null, Typeface.NORMAL);
	}

	/**
	 * Disable all of the answer selection buttons.
	 */
	private void disableAnswerButtons() {
		mAnswer1.setEnabled(false);
		mAnswer2.setEnabled(false);
		mAnswer3.setEnabled(false);
		mAnswer4.setEnabled(false);
		mAnswer1.setTextColor(Color.GRAY);
		mAnswer2.setTextColor(Color.GRAY);
		mAnswer3.setTextColor(Color.GRAY);
		mAnswer4.setTextColor(Color.GRAY);
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
	
	public static void makeToast(Context context, String text) {
		// TODO Move this into a utility class to use as a convenience method
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
		}
}
