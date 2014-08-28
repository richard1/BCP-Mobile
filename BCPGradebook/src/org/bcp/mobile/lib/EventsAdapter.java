package org.bcp.mobile.lib;

import java.util.ArrayList;

import org.bcp.mobile.CalendarActivity.Event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.bcp.mobile.R;

public class EventsAdapter extends ArrayAdapter<Item> {

    Context context; 
    int layoutResourceId;    
    Item data[] = null;
    private LayoutInflater inflater;
    
    public EventsAdapter(Context context, int layoutResourceId, Item[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public EventsAdapter(Context context, int layoutResourceId, ArrayList<Item> data) {
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
				/*if(row == null) {
		            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		            row = inflater.inflate(layoutResourceId, parent, false);
		            
		            holder = new EventsHolder();
		            holder.title = (TextView) row.findViewById(R.id.event_title);
		            holder.date = (TextView) row.findViewById(R.id.event_date);
		            
		            row.setTag(holder);
		        }
		        else {
		            holder = (EventsHolder)row.getTag();
		        }*/
				v = inflater.inflate(R.layout.events_row, null);
		        Event event = (Event) i;
		        
		        final TextView title = (TextView) v.findViewById(R.id.event_title);
	            final TextView date = (TextView) v.findViewById(R.id.event_date);

		        if(title != null)
		        	title.setText(event.text);
		        if(date != null)
		        	date.setText(event.dayOfWeek + ", " +  event.month + " " + event.day);
			}
		}
        System.out.println("asdf sec? " + i.isSection() + ", " + v);
        return v;
    }
    
    @Override
    public int getCount() {
        return data.length;
    }
}