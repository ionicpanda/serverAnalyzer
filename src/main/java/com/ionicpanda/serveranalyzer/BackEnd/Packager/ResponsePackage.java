package com.commercebank.serveranalyzer.BackEnd.Packager;

import java.util.ArrayList;

public class ResponsePackage {

    private ArrayList<ResponseCategory> responseCategories;

    public ResponsePackage(){
        responseCategories = new ArrayList<>();
    }
    public ResponsePackage(ArrayList<ResponseCategory> responseCategories) {
        this.responseCategories = responseCategories;
    }

    public void addCategory(ResponseCategory category) {
        responseCategories.add(category);
    }
    public ArrayList<ResponseCategory> getCategories() {
        return responseCategories;
    }

    public ResponseCategory getCategoryByTitle(String title) {
        for(ResponseCategory category : responseCategories) {
            if(title.equals(category.getTitle())) return category;
        }
        return null;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (ResponseCategory category : responseCategories){
            builder.append(category);
            builder.append(",");
        }
        builder.deleteCharAt(builder.lastIndexOf(","));
        builder.append("}");
        return builder.toString();
    }

}
