package com.example.recview;

public class News {

    private String title;
    private String text;
    private String time;

    public News(String time, String title, String text){
        this.time=time;
        this.title = title;
        this.text = text;
    }

    public String getTime(){
        return this.time;
    }

    public void setTime(String time){
        this.time = time;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }
}