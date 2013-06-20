package bcp.web.bcpgradebook.lib;

public class Grade {
    public int icon;
    public String title;
    public String subtitle;
    public int semester;
    
    public Grade() {
        super();
    }
    
    public Grade(int icon, String title, String subtitle, int semester) {
        super();
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
        this.semester = semester;
    }
    
    public void setLetter(int icon) {
    	this.icon = icon;
    }
    
    public void setName(String title) {
    	this.title = title;
    }
    
    public void setPercent(String subtitle) {
    	this.subtitle = subtitle;
    }
    
    public void setSemester(int semester) {
    	this.semester = semester;
    }
}
