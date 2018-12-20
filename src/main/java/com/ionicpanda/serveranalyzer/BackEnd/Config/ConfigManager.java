package com.commercebank.serveranalyzer.BackEnd.Config;

import com.commercebank.serveranalyzer.BackEnd.Packager.ResponseCategory;
import com.commercebank.serveranalyzer.BackEnd.Packager.ResponsePackage;
import com.commercebank.serveranalyzer.BackEnd.Packager.ResponsePiece;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private ConfigRoot root;

    public void flagBasedOnConfig(ResponsePackage responsePackage) {
        if (root == null) {
            loadConfig();
        }
        flagSet(root.getRed(), responsePackage, 'r');
        flagSet(root.getYellow(), responsePackage, 'y');
    }

    private void flagSet(List<ConfigCategory> configCategory, ResponsePackage responsePackage, char flagValue) {
        for(ConfigCategory category : configCategory) {
            ResponseCategory responseCategory = responsePackage.getCategoryByTitle(translateCategory(category.getCategory()));
            if(responseCategory != null) flagCategory(category, responseCategory, flagValue);
        }
    }
    private void flagCategory(ConfigCategory configCategory, ResponseCategory responseCategory, char flagValue) {
        for(ConfigFlag flag : configCategory.getFlags()) {
            if(responseCategory != null) {
                ResponsePiece piece = responseCategory.getPieceByTitle(flag.getTitle());
                if(piece != null) {
                    piece.setFlagOnText(flag.getValue(), flagValue);
                }
            }
        }
    }

    private String translateCategory(String cat) {
        switch(cat) {
            case "Response Header":
                return "response_header";
            case "response_header":
                return "Response Header";
            case "Certificate Chain":
                return "certificate_chain";
            case "certificate_chain":
                return "Certificate Chain";
        }
        return "";
    }

    private void loadConfig(){
        Yaml yaml = new Yaml(new Constructor(ConfigRoot.class));
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("static/config.yaml");
        root = yaml.load(inputStream);
    }

}
