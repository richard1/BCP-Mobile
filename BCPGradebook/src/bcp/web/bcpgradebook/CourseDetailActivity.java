package bcp.web.bcpgradebook;

import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CourseDetailActivity extends ListActivity {
	
	private ListView myList;
	ArrayAdapter<String> adapter;
	private ArrayList<String> listContent = new ArrayList<String>();
	OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String title = adapter.getItem(position);
			String detail = "Score: 14 / 15\nPercentage: 93.33%\nGrade: A\n\nCategory: Placeholder\nDue Date: Oct 23";
	        AlertDialog.Builder builder = new AlertDialog.Builder(CourseDetailActivity.this);
	        builder.setTitle(title);
	        builder.setMessage(detail);
	        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            }
	        });
	        builder.show();
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_detail);
		
		getActionBar().setHomeButtonEnabled(true);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = this.getIntent();
		String id = intent.getStringExtra(GradeViewActivity.COURSE_ID);
		String title = intent.getStringExtra("title");
		if(title != null) {
			setTitle(title);
		} else {
			setTitle("Unknown Course");
		}
		
		listContent.add("Unit 1 Test");
		listContent.add("Workbook pg. 23-25");
		listContent.add("Quarter 1 Participation");
		listContent.add("Unit 2 Test");
		listContent.add("Homework 2/5/13");
		listContent.add("Ch. 5 Quiz");
		listContent.add("Extra Credit");
		listContent.add("SOOO still waiting on the script");
		listContent.add("these are placeholders");
		listContent.add(id);
		
		adapter = new ArrayAdapter<String>(this, /*R.id.course_item_row*/android.R.layout.simple_list_item_1, listContent);
		setListAdapter(adapter);
		
		getListView().setOnItemClickListener(listener);
		
		//populateList(R.id.listView2, listContent);
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
	        	String about = "By Richard Lin '13\n\nWith help from Jonathan Chang '13, Bryce Pauken '14\n\n" +
		    			"Based on Bryce's BCP Mobile app for iOS, this app was created to provide Android-loving " +
		    			"Bellarmine students a convenient way to check their grades, view announcements, and more.\n\n" +
		    			"If you're enjoying this app, please share this with your friends!";
		        AlertDialog.Builder builder = new AlertDialog.Builder(CourseDetailActivity.this);
		        builder.setTitle("About");
		        builder.setMessage(about);
		        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            }
		        });
		        builder.setNegativeButton("View on GitHub", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/richard1/"));
		            	startActivity(browserIntent);
		            }
		        });
		        builder.show();
		        return true;
	        case R.id.menu_logout:
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
