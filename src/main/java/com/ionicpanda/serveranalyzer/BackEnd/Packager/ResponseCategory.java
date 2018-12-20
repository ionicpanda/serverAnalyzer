package com.commercebank.serveranalyzer.BackEnd.Packager;

import java.util.ArrayList;

public class ResponseCategory {

    private String title;
    public String getTitle() {return title;}
    // flag values:
    // n = normal / no color
    // y = yellow
    // r = red
    private ResponseData flag;
    public void setFlag(char c){
	flag = new ResponseData("flag", Character.toString(c));
    }
    private ArrayList<ResponsePiece> pieces;


    public ResponseCategory(String title) {
        this.title = title;
	    setFlag('n');
        pieces = new ArrayList<>();
    }
    public ResponseCategory(String title, char flag){
        this.title = title;
        setFlag(flag);
        pieces = new ArrayList<>();
    }
    public ResponseCategory(String title, ArrayList<ResponsePiece> pieces) {
        this.title = title;
	    setFlag('n');
        this.pieces = pieces;
    }
    public ResponseCategory(String title, char flag, ArrayList<ResponsePiece> pieces) {
        this.title = title;
	    setFlag(flag);
        this.pieces = pieces;
    }

    public ResponsePiece getPieceByTitle(String title) {
        for(ResponsePiece piece : pieces) {
            if(title.equals(piece.getTitle())) return piece;
        }
        return  null;
    }

    public void setFlagOnTextForAllChildren(String text, char flag) {
        for(ResponsePiece piece : pieces) {
            piece.setFlagOnText(text, flag);
        }
        updateFlag();
    }

    public void updateFlag() {
        char currentFlag = 'n';
        for(ResponsePiece piece : pieces) {
            char pieceFlag = piece.getFlag().getData().charAt(0);
            switch(pieceFlag) {
                case 'y':
                    if(currentFlag == 'n') currentFlag = 'y';
                    break;
                case 'r':
                    if(currentFlag == 'n' || currentFlag == 'y') currentFlag = 'r';
                    break;
            }
        }
        setFlag(currentFlag);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        builder.append(title);
        builder.append("\": {");
	    builder.append(flag);
	    builder.append(",");
        for (ResponsePiece piece : pieces) {
            builder.append(piece);
            builder.append(",");
        }
        builder.deleteCharAt(builder.lastIndexOf(","));
        builder.append("}");
        return builder.toString();
    }

    public void addPiece(ResponsePiece piece) {
        pieces.add(piece);
    }
    public int getNumberOfPieces() {
        return pieces.size();
    }
    public ResponsePiece getPiece(int index) {
        return pieces.get(index);
    }
    public void removePiece(int index) {
        pieces.remove(index);
    }

}
