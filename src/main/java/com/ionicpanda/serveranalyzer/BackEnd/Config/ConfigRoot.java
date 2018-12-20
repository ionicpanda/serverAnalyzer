package com.commercebank.serveranalyzer.BackEnd.Config;

import java.util.List;

public class ConfigRoot {
    private List<ConfigCategory> red;
    private List<ConfigCategory> yellow;

    public List<ConfigCategory> getRed() {
        return red;
    }

    public void setRed(List<ConfigCategory> red) {
        this.red = red;
    }

    public List<ConfigCategory> getYellow() {
        return yellow;
    }

    public void setYellow(List<ConfigCategory> yellow) {
        this.yellow = yellow;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("red:\n");
        for(ConfigCategory category : red) {
            result.append(category.toString());
        }
        result.append("\nyellow:\n");
        for(ConfigCategory category : yellow) {
            result.append(category.toString());
        }
        return result.toString();
    }
}
