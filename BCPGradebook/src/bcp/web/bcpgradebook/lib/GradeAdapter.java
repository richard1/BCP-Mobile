package bcp.web.bcpgradebook.lib;

import java.util.ArrayList;

import bcp.web.bcpgradebook.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GradeAdapter extends ArrayAdapter<Grade>{

    Context context; 
    int layoutResourceId;    
    Grade data[] = null;
    
    public GradeAdapter(Context context, int layoutResourceId, Grade[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
    
    public GradeAdapter(Context context, int layoutResourceId, ArrayList<Grade> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data.toArray(new Grade[data.size()]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        GradeHolder holder = null;
        
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new GradeHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
            holder.txtTitle = (TextView) row.findViewById(R.id.txtTitle);
            holder.subtext = (TextView) row.findViewById(R.id.subtext);
            
            row.setTag(holder);
        }
        else {
            holder = (GradeHolder)row.getTag();
        }
        
        Grade grade = data[position];
        
        
        //holder.subtext.setText(grade.getPercentWithExtra()); // was grade.subtitle
        String subtitleText = grade.getPercentWithExtra();
	    Spannable WordtoSpan = new SpannableString(subtitleText);
	    
	    if(subtitleText.contains("-")) { // grade decrease :(
	    	WordtoSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#d14836")), subtitleText.indexOf(grade.extra), subtitleText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    	WordtoSpan.setSpan(new StyleSpan(Typeface.BOLD), subtitleText.indexOf(grade.extra), subtitleText.length(), 0);
	    } else if(subtitleText.contains("+")) {
	    	if(subtitleText.contains("+0.00%")) { // no change in grade :|
	    		WordtoSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#AAAAAA")), subtitleText.indexOf(grade.extra), subtitleText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    	} else { // grade increase :)
	    		WordtoSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#009933")), subtitleText.indexOf(grade.extra), subtitleText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    		WordtoSpan.setSpan(new StyleSpan(Typeface.BOLD), subtitleText.indexOf(grade.extra), subtitleText.length(), 0);
	    	}
	    }
	    	
	    holder.subtext.setText(WordtoSpan);
        
        holder.txtTitle.setText(grade.title);
        holder.imgIcon.setImageResource(grade.icon);
        
        return row;
    }
    
    @Override
    public int getCount() {
        return data.length;
    }
    
    static class GradeHolder {
        ImageView imgIcon;
        TextView txtTitle;
        TextView subtext;
    }
}