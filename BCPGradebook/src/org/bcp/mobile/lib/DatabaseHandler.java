package org.bcp.mobile.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bcp.mobile.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
	 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "grades";
 
    // Contacts table name
    private String TABLE_GRADES = "gradesTable"; // TODO: delete the stuff in grades
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_LETTER = "letter";
    private static final String KEY_PERCENT = "percent";
    private static final String KEY_SEMESTER = "semester";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
    	System.out.println("Create table: " + TABLE_GRADES);
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_GRADES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_LETTER + " TEXT," + KEY_PERCENT + " TEXT," 
                + KEY_SEMESTER + " INTEGER" + ")";
        System.out.println(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
 
        // Create tables again
        onCreate(db);
    }
 
    public void add(Grade grade) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, grade.title); // Contact Name
        values.put(KEY_LETTER, getGradeFromId(grade.icon)); // Contact Name
        values.put(KEY_PERCENT, grade.subtitle); // Contact Phone
        values.put(KEY_SEMESTER, grade.semester);
 
        // Inserting Row
        db.insert(TABLE_GRADES, null, values);
        db.close(); // Closing database connection
    }
 
    // Getting single contact
    public Grade get(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_GRADES, new String[] { KEY_ID,
                KEY_NAME, KEY_LETTER, KEY_PERCENT, KEY_SEMESTER }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        Grade contact = new Grade(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(3), 
                Integer.parseInt(cursor.getString(4))); // TODO: confirm
        cursor.close();
        // return contact
        return contact;
    }
     
    // Getting All Contacts
    public List<Grade> getAll() {
        List<Grade> gradeList = new ArrayList<Grade>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GRADES;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	Grade grade = new Grade();
            	//grade.setID(Integer.parseInt(cursor.getString(0)));
            	grade.setName(cursor.getString(1));
            	grade.setLetter(getIdFromGrade(cursor.getString(2)));
            	grade.setPercent(cursor.getString(3));
            	grade.setSemester(Integer.parseInt(cursor.getString(4)));
                // Adding contact to list
            	gradeList.add(grade);
            } while (cursor.moveToNext());
        }
 
        cursor.close();
        // return contact list
        return gradeList;
    }
    
    public List<Grade> getAllWithSemester(int semester) {
        List<Grade> gradeList = new ArrayList<Grade>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GRADES + " WHERE " + KEY_SEMESTER + "='" + semester + "'";
        System.out.println(selectQuery);
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	Grade grade = new Grade();
            	//grade.setID(Integer.parseInt(cursor.getString(0)));
            	grade.setName(cursor.getString(1));
            	grade.setLetter(getIdFromGrade(cursor.getString(2)));
            	grade.setPercent(cursor.getString(3));
            	grade.setSemester(Integer.parseInt(cursor.getString(4)));
                // Adding contact to list
            	gradeList.add(grade);
            } while (cursor.moveToNext());
        }
 
        cursor.close();
        // return contact list
        return gradeList;
    }
    
    public HashMap<String, String> getPercentTitleMap(int semester) {
        HashMap<String, String> map = new HashMap<String, String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GRADES + " WHERE " + KEY_SEMESTER + "='" + semester + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	map.put(cursor.getString(1), cursor.getString(3)); // course name, grade percentage
            } while (cursor.moveToNext());
        }
 
        cursor.close();
        // return contact list
        return map;
    }
 
    /*
    // Updating single contact
    public int update(Grade grade) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, grade.title);
        values.put(KEY_LETTER, grade.icon);
        values.put(KEY_PERCENT, grade.subtitle);
 
        // updating row
        return db.update(TABLE_GRADES, values, KEY_ID + " = ?",
                new String[] { String.valueOf(grade.getID()) });
    }
    */
    
    /*
    // Deleting single contact
    public void delete(Grade grade) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GRADES, KEY_ID + " = ?",
                new String[] { String.valueOf(grade.getID()) });
        db.close();
    }
 */
    
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GRADES, null, null);
        db.close();
    }
 
    public int getGradesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_GRADES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
 
        // return count
        return cursor.getCount();
    }
    
    public String getGradeFromId(int id) {
		// Please don't judge me.
		if(id == R.drawable.grade_aplus) return "A+";
		else if(id == R.drawable.grade_a) return "A";
		else if(id == R.drawable.grade_aminus) return "A-";
		else if(id == R.drawable.grade_bplus) return "B+";
		else if(id == R.drawable.grade_b) return "B";
		else if(id == R.drawable.grade_bminus) return "B-";
		else if(id == R.drawable.grade_cplus) return "C+";
		else if(id == R.drawable.grade_c) return "C";
		else if(id == R.drawable.grade_cminus) return "C-";
		else if(id == R.drawable.grade_dplus) return "D+";
		else if(id == R.drawable.grade_d) return "D";
		else if(id == R.drawable.grade_dminus) return "D-";
		return "F";
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
		return R.drawable.grade_f;
	}
}