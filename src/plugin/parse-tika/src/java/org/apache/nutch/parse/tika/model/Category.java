package org.apache.nutch.parse.tika.model;

/**
 * Created by laphuoc on 9/29/2016.
 */
public class Category {
    int id;
    String category;
    String language;
    boolean is_system;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean is_system() {
        return is_system;
    }

    public void setIs_system(boolean is_system) {
        this.is_system = is_system;
    }
}
