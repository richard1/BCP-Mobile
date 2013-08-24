package org.bcp.mobile.lib;

public class News implements Item {
    public String title;
    public String link;
    public String date;
    
    public News(String title, String link, String date) {
        super();
        this.title = title;
        this.link = link;
        this.date = date;
    }

	@Override
	public boolean isSection() {
		return false;
	}
}