package com.commercebank.serveranalyzer.BackEnd.Config;

public class ConfigFlag {
    private String title;
    private String value;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        String result = "";
        result += "      - title: " + title + "\n";
        result += "        value: " + value + "\n";
        return result;
    }
}
