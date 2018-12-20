package com.commercebank.serveranalyzer.BackEnd;

//Response Header Imports
import java.net.*;
import java.util.*;
import java.io.IOException;
/////////////////////////////////
import com.commercebank.serveranalyzer.BackEnd.DataPieces.*;
import com.commercebank.serveranalyzer.BackEnd.Packager.ResponseMapConverter;
import com.commercebank.serveranalyzer.BackEnd.Packager.ResponsePackage;

import java.util.Map;

import java.security.cert.X509Certificate;
import java.net.URL;
import javax.net.ssl.*;
import java.security.cert.Certificate;
import java.net.MalformedURLException;

public class ServerAnalyzerBackEnd {
    private boolean responseHeaderEmpty;
    private String url;
    private boolean validURL;
    private String[] protocols;

    private ArrayList<DataPiece> dataToFetch;

    public ServerAnalyzerBackEnd(String url, String rawProtocolsString) {
        this.url = url;
        this.validURL = validateURLSyntax();
        this.protocols = parseProtocolData(rawProtocolsString);
        this.dataToFetch = new ArrayList<>();
    }

    // Add all data pieces to fetch
    public void fetchAll() {
        dataToFetch.add(new ResponseHeaderData());
        dataToFetch.add(new CertChainData());
        //TODO: Evaluate if this is necessary, because ProtocolData sort of does the same thing but slower
//        dataToFetch.add(new CipherSuiteData());
        dataToFetch.add(new ProtocolData(protocols));
    }

    //add a single data piece to be fetched
    public void addDataPieceToFetch(DataPiece dataPiece) {
        dataToFetch.add(dataPiece);
    }

    public Map<String, Object> createOutput(){
        String host;

        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        String secureURL = "";
        URL testURL;
        int port;
        String cert_chain = "";

        if (!validURL)
        {
            System.out.println("Invalid URL syntax.");
            returnErrorJson();
        }
        //url is not null, assign to secureURL to create https connection
        if(url != null){
            secureURL = url;
        }

        //Initialize sesson and connection
        SSLSession session = null;
        HttpsURLConnection connection = null;

        //create URL object from 'url'
        //create https connection based off of url and then call getCertChain method to pull that cert chain
        try {
            testURL = new URL(secureURL);
            port = testURL.getPort();
            host = testURL.getHost();

            SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket socket = socketFactory.createSocket(host, port);

            session = ((SSLSocket) socket).getSession();
            connection = (HttpsURLConnection)testURL.openConnection();
        }
        catch(UnknownHostException e){
            System.out.println("This ip address could not be determined");
            returnErrorJson();
        }
        catch(MalformedURLException e){
            e.printStackTrace();
            returnErrorJson();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        // fetch data for all data pieces
        for (DataPiece dataPiece : dataToFetch) {
            dataPiece.fetchData(connection, session);
        }

        //put all data pieces into hashmap
        for (DataPiece dataPiece : dataToFetch) {
            params.put(dataPiece.getDataName(), dataPiece.getDataResult());
        }
        // return the hashmap
        return params;
    }

    public ResponsePackage createResponsePackage() {
        createOutput();
        ResponsePackage responsePackage = new ResponsePackage();
        for (DataPiece dataPiece : dataToFetch) {
            dataPiece.fillCategoryData();
            responsePackage.addCategory(dataPiece.getCategory());
            if(dataPiece.getDataName().equals("response_header")){
                if(dataPiece.getDataResult().equals("")){
                    responseHeaderEmpty = true;
                    System.out.println("Response header empty.");
                }
            }
            if(dataPiece.getDataName().equals("certificate_chain")){
                if(responseHeaderEmpty) {
                    parseCertForCN(dataPiece);
                }
            }
        }
        return responsePackage;
    }

    //Hopefully we'll never have to use this again
    public String convertMapToStringOutput() {
        Map<String, Object> map = createOutput();
        ResponsePackage responsePackage = ResponseMapConverter.convertMap(map);
        return responsePackage.toString();
//        ResponsePackage responsePackage = ResponseMapConverter.convertMap(map);
    }


    public boolean validateURLSyntax()
    {
        int firstColon;
        int secondColon;
        boolean URLsyntax = true;

        /* If the user types in google.com, corrects to https://google.com*/
        if (this.url.length() < 8)
        {
            this.url = "https://".concat(this.url);
        }
        else if (!this.url.substring(0,8).equals("https://") && !this.url.substring(0,6).equals("ftp://"))
        {
            this.url = "https://".concat(this.url);
        }

        /*Forces URL to start with https://www. or https:// with a "." and some domain name, or is FTP*/
        String URLregex = "^(https://www\\..*\\..*|https://.*\\..*|ftp://.*)";

        if (!this.url.matches(URLregex))
        {
            URLsyntax = false;
        }

        //Removes an ending forward slash if present
        if (this.url.charAt(this.url.length() - 1) == '/')
        {
            this.url = this.url.substring(0, this.url.length() - 1);
        }

        //Checks if a port number was specified. If not, appends :443 to the URL.
        String portRegex = "^.*:.*:.*";

        if (!this.url.matches(portRegex))
        {
            this.url = this.url + ":443";
        }

        //Something like https://www.google.com: and no port actually specified or junk is after the port.
        secondColon = this.url.lastIndexOf(':');
        if (this.url.charAt(this.url.length() - 1) == ':' || !Character.isDigit(this.url.charAt(secondColon + 1)))
        {
            URLsyntax = false;
        }
        return URLsyntax;
    }

    /* This only runs if the response header is empty. It checks if the common name matches the URL. If not,
    * make a note of it and stop. */
    public DataPiece parseCertForCN(DataPiece certChain)
    {
        String errorMessage = "<<HIGHLIGHT IN YELLOW>> Response header empty, and CN does not match URL: ";
        String certString = certChain.getDataResult();
        String commonName;

        /* Get the indexes of the start and end for the common name. */
        int CNstart = certString.indexOf("CN=") + 3;
        int commaIndex = certString.indexOf(',', CNstart);
        commonName = certString.substring(CNstart, commaIndex);

        /* Get rid of any wildcard characters if present*/
        commonName = commonName.replaceAll("\\*.", "");

        /* If the URL doesn't contain the common name*/
        if (!url.contains(commonName))
        {
            certString += errorMessage + commonName;
            certChain.setDataResult(certString);
            certChain.getCategory().getPieceByTitle("CN").setFlag('y');
        }
        else
        {
            //Something else is wrong if the response header's empty. This only checked if it didn't match the common name.
        }

        return certChain;
    }

    /*Converts the raw data from the frontend to an array of Strings.*/
    public String[] parseProtocolData(String raw)
    {
        String[] splitProtocols = null;
        /*If it's not empty, then start making the array. Otherwise, it will be returned as null.*/
        if (!raw.equals(""))
        {
            raw = raw.substring(0, raw.length() - 1);
            splitProtocols = raw.split(",");
        }

        return splitProtocols;
    }

    public void returnErrorJson(){
        System.out.println("Returning error json");
    }

}



