package com.commercebank.serveranalyzer.BackEnd.DataPieces;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.commercebank.serveranalyzer.BackEnd.Packager.ResponseCategory;
import com.commercebank.serveranalyzer.BackEnd.Packager.ResponsePiece;

public abstract class DataPiece {

    protected String dataName;
    protected String dataResult;
    protected ResponseCategory category;

    public DataPiece(String dataName) {
        this.dataName = dataName;
        category = new ResponseCategory(dataName);
    }

    public void fetchData(HttpsURLConnection connection, SSLSession session) {}
    public String getDataName() {return this.dataName;}
    public String getDataResult() {return dataResult;}
    public void setDataResult(String dataResult) {this.dataResult = dataResult;}
    public ResponseCategory getCategory() {return category;}
    
    public abstract void fillCategoryData();
}
