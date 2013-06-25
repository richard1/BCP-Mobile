package bcp.web.bcpgradebook;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
		
		adapter = new ArrayAdapter<String>(this, R.layout.course_item_row, listContent);
		setListAdapter(adapter);
		
		getListView().setOnItemClickListener(listener);
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
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void populateList(int list, ArrayList<String> content) {
		myList = (ListView)findViewById(list);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, content);
		myList.setAdapter(adapter);
	}
}
