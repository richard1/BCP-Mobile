package org.bcp.mobile.lib;

public class Assignment implements Item {

    public String type;
    public String course;
    public String name;
    public String date;
    public String category;
    public double score;
    public double total;
    public String letter;
    public String percent;
    public int semester;
    public String weight;
    
    public Assignment(String type, String course, String name, String date, 
    				  String category, double score, double total, String letter, 
    				  String percent, int semester, String weight) {
        super();

        this.type = type;
        this.course = course;
        this.name = name;
        this.date = date;
        this.category = category;
        this.score = score;
        this.total = total;
        this.letter = letter;
        this.percent = percent;
        this.semester = semester;
        this.weight = weight;
    }

	@Override
	public boolean isSection() {
		return false;
	}
}
