package com.example.laostrainingapp;

import java.io.File;

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

public class QuizActivity extends Activity {

	private static final String BASE_DIRECTORY = "LaosTrainingApp";
	private static final String DEFAULT_QUIZ_FILE_NAME = "quiz.csv";
	
	private TextView mQuestion;
	private Button mAnswer1;
	private Button mAnswer2;
	private Button mAnswer3;
	private Button mAnswer4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quiz);
		
		// Create the UI references
		mQuestion = (TextView) findViewById(R.id.quiz_question);
		mAnswer1 = (Button) findViewById(R.id.quiz_answer_1);
		mAnswer2 = (Button) findViewById(R.id.quiz_answer_2);
		mAnswer3 = (Button) findViewById(R.id.quiz_answer_3);
		mAnswer4 = (Button) findViewById(R.id.quiz_answer_4);
		
		// Check that the external storage directory is readable
		String sdcardDirState = Environment.getExternalStorageState();
		if (!(sdcardDirState.equals(Environment.MEDIA_MOUNTED) || sdcardDirState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
			// Show a toast to alert if the external storage directory cannot be read from
			String text = "Unable to read from the external storage directory.";
			makeToast(getApplicationContext(), text);
		}
		
		// Read from the external storage directory
		File root = Environment.getExternalStorageDirectory();
		String rootPath = root.getPath();
		String quizPath = rootPath + "/" + BASE_DIRECTORY + "/" + DEFAULT_QUIZ_FILE_NAME;
		
		File quizFile = new File(quizPath);
		if (!quizFile.exists() || !quizFile.isFile() || !quizFile.canRead()) {
			// Show a toast if the quiz file could not be found
			String text = "Could not find the file 'quiz.csv'";
			makeToast(getApplicationContext(), text);
		}

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
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
			View rootView = inflater.inflate(R.layout.fragment_quiz, container,
					false);
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
