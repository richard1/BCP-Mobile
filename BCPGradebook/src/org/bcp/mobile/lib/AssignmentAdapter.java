package org.bcp.mobile.lib;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.bcp.mobile.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AssignmentAdapter extends ArrayAdapter<Item> {

    Context context; 
    int layoutResourceId;    
    Item data[] = null;
    DecimalFormat decimalFormat = new DecimalFormat("##0.##");
    LayoutInflater inflater;
    
    public AssignmentAdapter(Context context, int layoutResourceId, Item[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public AssignmentAdapter(Context context, int layoutResourceId, ArrayList<Item> data) {
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
				System.out.println("ASDF NEW SECTION: " + si.getTitle());
				v = inflater.inflate(R.layout.list_item_section, null);

				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				
				final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
				sectionView.setText(si.getTitle());
				
			} else {
				Assignment asg = (Assignment) i;
				v = inflater.inflate(R.layout.course_item_row, null);
				final TextView name = (TextView) v.findViewById(R.id.asg_name);
				final TextView percent = (TextView) v.findViewById(R.id.asg_percent);
				final ImageView letter = (ImageView) v.findViewById(R.id.asg_letter);
				
				if (name != null) 
					name.setText(asg.name);
				if(percent != null)
					percent.setText(decimalFormat.format(asg.score) + " / " + decimalFormat.format(asg.total));
				if(letter != null)
					letter.setImageResource(getIdFromGrade(asg.letter));
			}
		}
		return v;
    	/*
        View row = convertView;
        AssignmentHolder holder = null;
        
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new AssignmentHolder();
            holder.name = (TextView) row.findViewById(R.id.asg_name);
            holder.percent = (TextView) row.findViewById(R.id.asg_percent);
            holder.letter = (ImageView) row.findViewById(R.id.asg_letter);
            
            row.setTag(holder);
        }
        else {
            holder = (AssignmentHolder)row.getTag();
        }
        
        Assignment asg = data[position];

        holder.name.setText(asg.name);
        holder.percent.setText(decimalFormat.format(asg.score) + " / " + decimalFormat.format(asg.total));
        holder.letter.setImageResource(getIdFromGrade(asg.letter));
        return row;*/
    }
    
    @Override
    public int getCount() {
        return data.length;
    }
    
    public int getIdFromGrade(String grade) {
		// Please don't judge me.
		if(grade.equals("A+")) return R.drawable.grade_aplus;
		else if(grade.equals("A")) return R.drawable.grade_a;
		else if(grade.equals("A-")) return R.drawable.grade_aminus;
		else if(grade.equals("B+")) return R.drawable.grade_bplus;
		else if(grade.equals("B")) return R.drawable.grade_b;
		else if(grade.equals("B-")) return R.drawable.grade_bminus;
		else if(grade.equals("C+")) return R.drawable.grade_cplus;
		else if(grade.equals("C")) return R.drawable.grade_c;
		else if(grade.equals("C-")) return R.drawable.grade_cminus;
		else if(grade.equals("D+")) return R.drawable.grade_dplus;
		else if(grade.equals("D")) return R.drawable.grade_d;
		else if(grade.equals("D-")) return R.drawable.grade_dminus;
		else if(grade.equals("F")) return R.drawable.grade_f;
		return R.drawable.grade_f;
	}
    
    static class AssignmentHolder {
        TextView name;
        TextView percent;
        ImageView letter;
    }
}