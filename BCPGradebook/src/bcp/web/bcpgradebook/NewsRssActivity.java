package bcp.web.bcpgradebook;

import java.util.ArrayList;

import bcp.web.bcpgradebook.lib.DatabaseHandler;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class NewsRssActivity extends SlidingFragmentActivity {
	MenuListFragment mFrag;
	SlidingMenu sm;
	private ListView myList;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> listContent = new ArrayList<String>();
	OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String title = adapter.getItem(position);
			String detail = "TODO: this goes to the BCP announcements page"; // TODO: redirect to BCP page
	        AlertDialog.Builder builder = new AlertDialog.Builder(NewsRssActivity.this);
	        builder.setTitle(title);
	        builder.setMessage(detail);
	        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            }
	        });
	        builder.show();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_rss);
		setTitle("News");
		setBehindContentView(R.layout.menu_frame);
		
		if (savedInstanceState == null) {
			mFrag = new MenuListFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, mFrag).commit();
		} else {
			mFrag = (MenuListFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
		}
		
		sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE); // fullscreen is bad

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		listContent.add("Some news happening");
		listContent.add("Wow so much news");
		listContent.add("I can't believe it");
		
		//android.R.layout.simple_list_item_1
		myList = (ListView) findViewById(R.id.news_list);
		adapter = new ArrayAdapter<String>(this, R.layout.course_item_row, listContent);
		myList.setAdapter(adapter);
		myList.setOnItemClickListener(listener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.activity_announcements, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		if(sm.isMenuShowing()) {
			toggle();
		}
		else {
			super.onBackPressed();
		}
	}
	
	protected void onResume() {
		super.onResume();
		if(sm.isMenuShowing()) {
			toggle();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				toggle();
				return true;
		    case R.id.menu_about:	    	
		    	String about = "By Richard Lin '13\n\nWith help from Jonathan Chang '13, Bryce Pauken '14\n\n" +
		    			"Based on Bryce's BCP Mobile app for iOS, this app was created to provide Android-loving " +
		    			"Bellarmine students a convenient way to check their grades, view announcements, and more.\n\n" +
		    			"If you're enjoying this app, please share this with your friends!";
		        AlertDialog.Builder builder = new AlertDialog.Builder(NewsRssActivity.this);
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
		    	new DatabaseHandler(this).deleteAll();
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
}

