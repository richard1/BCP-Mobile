package org.bcp.mobile.lib;

import java.util.ArrayList;

import org.bcp.mobile.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NewsAdapter extends ArrayAdapter<Item> {
   
    private Item data[] = null;
    private LayoutInflater inflater;
    
    public NewsAdapter(Context context, int layoutResourceId, Item[] data) {
        super(context, layoutResourceId, data);
        this.data = data;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public NewsAdapter(Context context, int layoutResourceId, ArrayList<Item> data) {
        super(context, layoutResourceId, data);
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
    }
    
    @Override
    public int getCount() {
        return data.length;
    }
}