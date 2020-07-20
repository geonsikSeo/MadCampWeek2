package com.example.project2.ui.phonebook;

public class ProfileData {

    private String id;
    private String name;
    private String state;
    private String photo;
    private boolean expanded;

    public ProfileData(String id, String name, String state, String photo) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.photo = photo;
    }
    public ProfileData()
    {

    }
    public boolean getExpanded() {
        return this.expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
