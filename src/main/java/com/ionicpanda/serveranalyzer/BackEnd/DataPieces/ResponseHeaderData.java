package com.commercebank.serveranalyzer.BackEnd.DataPieces;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.commercebank.serveranalyzer.BackEnd.Packager.ResponsePiece;

public class ResponseHeaderData extends DataPiece {

    public ResponseHeaderData() {
        super("response_header");
    }

    @Override
    public void fetchData(HttpsURLConnection connection, SSLSession session) {
        String result = "";
        //Response Header Function
        try {
            System.out.println("HTTP Headers");
            System.out.println();

            Map<String, List<String>> map = connection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());

                if(entry.getKey() == null){
                    if(entry.getValue().toString().contains("HTTP/")) {
                        result += "Status-Code: " + entry.getValue();
                    }
                    else{
                        //call error json function here later
                        //need to add error message
                        System.out.println("Error out here");
                    }
                }
                else{
                    result += entry.getKey() + ": " + entry.getValue();
                }
            }

            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(result);

        dataResult = result;
    }

    public void fillCategoryData() {
        String outputPattern = "[a-zA-Z0-9-_]+: \\[[a-zA-Z0-9-_=\\\\!\\.\\/ ;:,]+\\]";
        ArrayList<String> blocks = returnMatches(dataResult, outputPattern);
        for(String block : blocks) {
            int firstColon = block.indexOf(':');
            String pieceName = block.substring(0, firstColon);
            String pieceData = block.substring(firstColon + 1);
            pieceData = pieceData.substring(2, pieceData.length()-1);
            category.addPiece(new ResponsePiece(pieceName, pieceData));
        }
    }

    public ArrayList<String> returnMatches(String input, String regex) {
        ArrayList<String> matches = new ArrayList<>();
        Matcher matchFinder = Pattern.compile(regex).matcher(input);
        while(matchFinder.find()) {
            matches.add(matchFinder.group(0));
        }
        return matches;
    }
}
