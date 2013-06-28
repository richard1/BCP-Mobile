package org.bcp.mobile.lib;

import java.util.ArrayList;

import org.bcp.mobile.CalendarActivity.Event;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.bcp.mobile.R;

public class EventsAdapter extends ArrayAdapter<Event> {

    Context context; 
    int layoutResourceId;    
    Event data[] = null;
    
    public EventsAdapter(Context context, int layoutResourceId, Event[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
    
    public EventsAdapter(Context context, int layoutResourceId, ArrayList<Event> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data.toArray(new Event[data.size()]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        EventsHolder holder = null;
        
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new EventsHolder();
            holder.title = (TextView) row.findViewById(R.id.event_title);
            holder.date = (TextView) row.findViewById(R.id.event_date);
            
            row.setTag(holder);
        }
        else {
            holder = (EventsHolder)row.getTag();
        }
        
        Event event = data[position];

        holder.title.setText(event.text);
        holder.date.setText(event.dayOfWeek + ", " +  event.month + " " + event.day);
        return row;
    }
    
    @Override
    public int getCount() {
        return data.length;
    }
    
    static class EventsHolder {
        TextView title;
        TextView date;
    }
}