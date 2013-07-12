package org.bcp.mobile;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bcp.mobile.R;
import org.bcp.mobile.lib.Assignment;
import org.bcp.mobile.lib.AssignmentAdapter;
import org.bcp.mobile.lib.AssignmentsDatabase;
import org.bcp.mobile.lib.Item;
import org.bcp.mobile.lib.SectionItem;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class CourseDetailActivity extends ListActivity {
	
	AssignmentAdapter adapter;
	private ArrayList<Item> listContent = new ArrayList<Item>();
	OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			DecimalFormat decimalFormat = new DecimalFormat("##0.##");
			Assignment asg = (Assignment) adapter.getItem(position);
			String title = asg.name;
			String detail = "";
			if(asg.type.equals("Cat")) {
				detail = "Score: " + decimalFormat.format(asg.score) + " / " + decimalFormat.format(asg.total) + "\nPercentage: " +
						asg.percent + "\nGrade: " + asg.letter + "\n\nWeight: " + asg.weight + "%";
			}
			else {
				detail = "Score: " + decimalFormat.format(asg.score) + " / " + decimalFormat.format(asg.total) + "\nPercentage: " +
						asg.percent + "\nGrade: " + asg.letter + "\n\nCategory: " + asg.category + "\nDue Date: " + asg.date;
			}
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
	AssignmentsDatabase adb;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_detail);
		
		getActionBar().setHomeButtonEnabled(true);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = this.getIntent();
		String title = intent.getStringExtra("title");
		int semester = intent.getIntExtra("semester", 0);
		if(title != null) {
			setTitle(title);
		} else {
			setTitle("Unknown Course");
		}
		
		adb = new AssignmentsDatabase(this);
		List<Item> asgs = adb.getAllWithSemesterAndCourse(semester, title);
		listContent.addAll(asgs);
		
		adapter = new AssignmentAdapter(this, R.layout.course_item_row, listContent);
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
}
