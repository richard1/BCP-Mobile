package bcp.web.bcpgradebook;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	
	public static final String USERNAME = "bcp.web.bcpgradebook.uname";
	public static final String PASSWORD = "bcp.web.bcpgradebook.passwd";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void login(View view)
	{
		Intent intent = new Intent(this, GradeViewActivity.class);
		EditText uNameField = (EditText)findViewById(R.id.loginname);
		EditText passField = (EditText)findViewById(R.id.loginpass);
		String theName = uNameField.getText().toString();
		String thePass = passField.getText().toString();
		intent.putExtra(USERNAME, theName);
		intent.putExtra(PASSWORD, thePass);
		startActivity(intent);
	}

}
