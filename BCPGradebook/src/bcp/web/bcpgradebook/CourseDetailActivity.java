package bcp.web.bcpgradebook;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CourseDetailActivity extends Activity {
	
	private ListView myList;
	ArrayAdapter<String> adapter;
	private ArrayList<String> listContent = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		String id = intent.getStringExtra(GradeViewActivity.COURSE_ID);
		
		listContent.add("yoo");
		listContent.add(id);
		setContentView(R.layout.activity_course_detail);
		
		populateList(R.id.listView2, listContent);
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
