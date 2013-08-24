package org.bcp.mobile.lib;

import java.util.ArrayList;

import org.bcp.mobile.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsAdapter extends ArrayAdapter<Item> {

    Context context; 
    int layoutResourceId;    
    Item data[] = null;
    LayoutInflater inflater;
    
    public NewsAdapter(Context context, int layoutResourceId, Item[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public NewsAdapter(Context context, int layoutResourceId, ArrayList<Item> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data.toArray(new Item[data.size()]);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	View v = convertView;

		final Item i = data[position];
		if (i != null) {
			if(i.isSection()) {
				SectionItem si = (SectionItem)i;
				v = inflater.inflate(R.layout.list_item_section, null);

				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				
				final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
				sectionView.setText(si.getTitle());
				
			} else {
				News news = (News) i;
				v = inflater.inflate(R.layout.news_row, null);
				final TextView title = (TextView) v.findViewById(R.id.news_title);
				final TextView date = (TextView) v.findViewById(R.id.news_date);
				
				if (title != null) 
					title.setText(news.title);
				if(date != null)
					date.setText(news.date);
			}
		}
		return v;
    	/*
        View row = convertView;
        NewsHolder holder = null;
        
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new NewsHolder();
            holder.title = (TextView) row.findViewById(R.id.news_title);
            holder.date = (TextView) row.findViewById(R.id.news_date);
            
            row.setTag(holder);
        }
        else {
            holder = (NewsHolder)row.getTag();
        }
        
        Item news = data[position];

        holder.title.setText(news.title);
        if(news.date.contains(":")) {
        	holder.date.setText("Posted on " + news.date.substring(0, news.date.indexOf(":") - 3));
        }
        else {
        	holder.date.setText(news.date);
        }
        return row;
        */
    }
    
    @Override
    public int getCount() {
        return data.length;
    }
    
    static class NewsHolder {
        TextView title;
        TextView date;
    }
}