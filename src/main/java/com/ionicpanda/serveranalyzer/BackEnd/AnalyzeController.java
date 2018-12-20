package com.commercebank.serveranalyzer.BackEnd;

import com.commercebank.serveranalyzer.BackEnd.Config.ConfigManager;
import com.commercebank.serveranalyzer.BackEnd.Packager.ResponsePackage;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class AnalyzeController {

    @ResponseBody
    @RequestMapping(value="/analyzeURL")
    public String getURLInformation(@RequestParam String url, @RequestParam String protocols)
    {
        ServerAnalyzerBackEnd backEnd = new ServerAnalyzerBackEnd(url, protocols);
        backEnd.fetchAll();
        ResponsePackage responsePackage = backEnd.createResponsePackage();

        new ConfigManager().flagBasedOnConfig(responsePackage);
        return responsePackage.toString();
    }

    
}