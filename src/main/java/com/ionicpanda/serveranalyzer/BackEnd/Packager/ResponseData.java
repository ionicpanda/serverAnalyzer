package com.commercebank.serveranalyzer.BackEnd.Packager;

public class ResponseData {

    private String title;
    private String data;

    public ResponseData(String title, String data) {
        this.title = title;
        this.data = data;
    }
    public ResponseData(String title) {
        this.title = title;
        this.data = "null";
    }
    public ResponseData() {
        this.title = "null";
        this.data = "null";
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public boolean doesDataContain(String text) {
        System.out.println(data);
        System.out.println(text);
        if(data.indexOf(text) >= 0) {
            return true;
        } else if (data.toLowerCase().indexOf(text.trim().toLowerCase()) >= 0){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        builder.append(title);
        builder.append("\": \"");
        builder.append(data);
        builder.append("\"");
        return builder.toString();
    }

}