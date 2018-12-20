package com.commercebank.serveranalyzer.BackEnd.Packager;

import java.util.HashMap;
import java.util.Map;

public class ResponseMapConverter {

    public static ResponsePackage convertMap(Map<String, Object> map) {
        ResponsePackage responsePackage = new ResponsePackage();
        HashMap<String, Object> hashMap = new HashMap<>(map);

        for(Map.Entry<String, Object> pair : hashMap.entrySet()) {
            String value = (String) pair.getValue();
            value = value.replaceAll("\"", "\\\\");
            ResponsePiece piece = new ResponsePiece(pair.getKey(), value);
            ResponseCategory category = new ResponseCategory(pair.getKey());
            category.addPiece(piece);
            responsePackage.addCategory(category);
        }

        return responsePackage;
    }
}
