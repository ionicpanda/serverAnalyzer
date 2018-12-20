package com.commercebank.serveranalyzer.BackEnd.Packager;

public class ResponsePiece {

    private String title;
    public String getTitle() {return title;}
    // flag values:
    // n = normal
    // w = warning
    // b = bad
    private ResponseData flag;
    public void setFlag(char c) {
        flag = new ResponseData("flag", Character.toString(c));
    }
    public ResponseData getFlag() {
        return flag;
    }
    private ResponseData data;
    public void setData(String data) {
        this.data = new ResponseData("data", escapeQuotes(data));
    }
    public void setDataDirect(String data) {
        this.data = new ResponseData("data", data);
    }

    public ResponsePiece(String title) {
        this(title, "null");
    }
    public ResponsePiece(String title, String data) {
        this(title, data, 'n');
    }
    public ResponsePiece(String title, String data, char flag){
        this.title = title;
        setData(data);
        setFlag(flag);
    }

    public void setFlagOnText(String text, char flag) {
        if(data.doesDataContain(text)) {
            this.setFlag(flag);
        }
    }

    private String escapeQuotes(String data) {
        if(data.indexOf('"') < 0) return data;

        String newData = "";
        for(int i = 0; i < data.length(); i++) {
            if(data.charAt(i) == '"') {
                newData += "\\";
            }
            newData += data.charAt(i);
        }
        return newData;
    }

    public void resetFlag() {
        this.setFlag('n');
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        builder.append(title);
        builder.append("\": {");
        builder.append(flag);
        builder.append(", ");
        builder.append(data);
        builder.append("}");
        return builder.toString();
    }

}
