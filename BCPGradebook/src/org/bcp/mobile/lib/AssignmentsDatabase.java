package org.bcp.mobile.lib;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AssignmentsDatabase extends SQLiteOpenHelper {
	 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "assignments";
 
    // Contacts table name
    private String TABLE_GRADES = "assignments"; // TODO: delete the stuff in assignments
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type"; // asg or cat
    private static final String KEY_COURSE = "course"; // course this asg is tied to
    private static final String KEY_NAME = "name";
    private static final String KEY_DATE = "date";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_SCORE = "score";
    private static final String KEY_TOTAL = "total";
    private static final String KEY_LETTER = "letter";
    private static final String KEY_PERCENT = "percent";
    private static final String KEY_SEMESTER = "semester";
    private static final String KEY_WEIGHT = "weight";
 
    public AssignmentsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
    	System.out.println("Create table: " + TABLE_GRADES);
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_GRADES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TYPE + " TEXT,"
        		+ KEY_COURSE + " TEXT," + KEY_NAME + " TEXT,"
        		+ KEY_DATE + " TEXT," + KEY_CATEGORY + " TEXT,"
        		+ KEY_SCORE + " DOUBLE," + KEY_TOTAL + " DOUBLE,"
                + KEY_LETTER + " TEXT," + KEY_PERCENT + " TEXT," 
                + KEY_SEMESTER + " INTEGER," + KEY_WEIGHT + " TEXT"
                + ")";
        System.out.println("CREATE DB QUERY: " + CREATE_CONTACTS_TABLE);
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
 
    public void add(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        //values.put(KEY_ID, assignment.id);
        values.put(KEY_TYPE, assignment.type);
        values.put(KEY_COURSE, assignment.course);
        values.put(KEY_NAME, assignment.name);
        values.put(KEY_DATE, assignment.date);
        values.put(KEY_CATEGORY, assignment.category);
        values.put(KEY_SCORE, assignment.score);
        values.put(KEY_TOTAL, assignment.total);
        values.put(KEY_LETTER, assignment.letter);
        values.put(KEY_PERCENT, assignment.percent);
        values.put(KEY_SEMESTER, assignment.semester);
        values.put(KEY_WEIGHT, assignment.weight);
 
        // Inserting Row
        db.insert(TABLE_GRADES, null, values);
        db.close(); // Closing database connection
    }
 
    // Getting single contact
    public Assignment get(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_GRADES, new String[] { KEY_ID, KEY_TYPE, KEY_COURSE, KEY_NAME, KEY_DATE, KEY_CATEGORY,
        		KEY_SCORE, KEY_TOTAL, KEY_LETTER, KEY_PERCENT, KEY_SEMESTER, KEY_WEIGHT}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        Assignment assignment = new Assignment(
                cursor.getString(1), cursor.getString(2), cursor.getString(3), 
                cursor.getString(4), cursor.getString(5), Double.parseDouble(cursor.getString(6)), 
                Double.parseDouble(cursor.getString(7)), cursor.getString(8), cursor.getString(9), 
                Integer.parseInt(cursor.getString(10)), cursor.getString(11)); // TODO: confirm
        return assignment;
    }
     
    // Getting All Contacts
    public List<Item> getAll() {
        List<Item> assignmentList = new ArrayList<Item>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GRADES;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        boolean addedAsgHeader = false, addedCatHeader = false;
        if (cursor.moveToFirst()) {
            do {
            	if(!addedAsgHeader && cursor.getString(1).equals("Asg")) {
            		System.out.println("asdf ADDING ASG HEADER");
            		assignmentList.add(new SectionItem("Assignments"));
            		addedAsgHeader = true;
            	}
            	if(!addedCatHeader && cursor.getString(1).equals("Cat")) {
            		System.out.println("asdf ADDING CAT HEADER");
            		assignmentList.add(new SectionItem("Categories"));
            		addedCatHeader = true;
            	}
            	Assignment assignment = new Assignment(
                        cursor.getString(1), cursor.getString(2), cursor.getString(3), 
                        cursor.getString(4), cursor.getString(5), Double.parseDouble(cursor.getString(6)), 
                        Double.parseDouble(cursor.getString(7)), cursor.getString(8), cursor.getString(9), 
                        Integer.parseInt(cursor.getString(10)), cursor.getString(11)); // TODO: confirm
            	assignmentList.add(assignment);
            } while (cursor.moveToNext());
        }
 
        // return contact list
        return assignmentList;
    }
    
    public List<Item> getAllWithSemesterAndCourse(int semester, String course) {
        List<Item> assignmentList = new ArrayList<Item>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GRADES + " WHERE " + KEY_SEMESTER + "='" + semester + "' AND " + KEY_COURSE + "='" + course + "'";
        System.out.println(selectQuery);
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        boolean addedAsgHeader = false, addedCatHeader = false;
        if (cursor.moveToFirst()) {
            do {
            	if(!addedAsgHeader && cursor.getString(1).equals("Asg")) {
            		assignmentList.add(new SectionItem("Assignments"));
            		addedAsgHeader = true;
            	}
            	if(!addedCatHeader && cursor.getString(1).equals("Cat")) {
            		assignmentList.add(new SectionItem("Categories"));
            		addedCatHeader = true;
            	}
            	Assignment assignment = new Assignment(
                        cursor.getString(1), cursor.getString(2), cursor.getString(3), 
                        cursor.getString(4), cursor.getString(5), Double.parseDouble(cursor.getString(6)), 
                        Double.parseDouble(cursor.getString(7)), cursor.getString(8), cursor.getString(9), 
                        Integer.parseInt(cursor.getString(10)), cursor.getString(11)); // TODO: confirm
            	assignmentList.add(assignment);
            } while (cursor.moveToNext());
        }
 
        // return contact list
        return assignmentList;
    }
    
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GRADES, null, null);
        db.close();
    }
 
    public int getAssignmentsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_GRADES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
 
        // return count
        return cursor.getCount();
    }
}