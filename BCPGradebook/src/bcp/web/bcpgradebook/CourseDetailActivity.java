package bcp.web.bcpgradebook;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class CourseDetailActivity extends Activity {
	
	private ListView myList;
	ArrayAdapter<String> adapter;
	private ArrayList<String> listContent = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("A Random Course");
		getActionBar().setHomeButtonEnabled(true);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = this.getIntent();
		String id = intent.getStringExtra(GradeViewActivity.COURSE_ID);
		
		listContent.add("yoo");
		listContent.add(id);
		setContentView(R.layout.activity_course_detail);
		
		populateList(R.id.listView2, listContent);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	            return true;
	        case R.id.menu_about:
		    	Toast.makeText(getApplicationContext(), "About (doesn't do anything)!", Toast.LENGTH_SHORT).show();
		        return true;
	        case R.id.menu_logout:
	        	Toast.makeText(getApplicationContext(), "Logging out...", Toast.LENGTH_SHORT).show();
		        getSharedPreferences("username", MODE_PRIVATE).edit().clear().commit();
		        getSharedPreferences("password", MODE_PRIVATE).edit().clear().commit();
		        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		        startActivity(intent);
		        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		        return true;
		    case R.id.menu_settings:
		    	Toast.makeText(getApplicationContext(), "Settings (doesn't do anything)!", Toast.LENGTH_SHORT).show();
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void populateList(int list, ArrayList<String> content) {
		myList = (ListView)findViewById(list);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, content);
		myList.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_course_detail, menu);
		return true;
	}

}
