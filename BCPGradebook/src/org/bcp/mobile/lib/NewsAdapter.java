package org.bcp.mobile.lib;

import java.util.ArrayList;

import org.bcp.mobile.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NewsAdapter extends ArrayAdapter<News> {

    Context context; 
    int layoutResourceId;    
    News data[] = null;
    
    public NewsAdapter(Context context, int layoutResourceId, News[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
    
    public NewsAdapter(Context context, int layoutResourceId, ArrayList<News> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data.toArray(new News[data.size()]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
        
        News news = data[position];

        holder.title.setText(news.title);
        holder.date.setText("Posted on " + news.date.substring(0, news.date.indexOf(":") - 3));
        return row;
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