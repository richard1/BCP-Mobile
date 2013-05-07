package bcp.web.bcpgradebook;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class GradeViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		String username = intent.getStringExtra(MainActivity.USERNAME);
		String password = intent.getStringExtra(MainActivity.PASSWORD);
		setContentView(R.layout.activity_grade_view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_grade_view, menu);
		return true;
	}

}
