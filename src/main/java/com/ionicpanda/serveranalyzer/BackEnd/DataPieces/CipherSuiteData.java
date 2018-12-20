package com.commercebank.serveranalyzer.BackEnd.DataPieces;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import com.commercebank.serveranalyzer.BackEnd.Packager.ResponsePiece;

public class CipherSuiteData extends DataPiece {

    String[] supportedCipherSuites;

    public CipherSuiteData() {
        super("cipher_suite");
    }

    @Override
    public void fetchData(HttpsURLConnection connection, SSLSession session) {
        String result = session.getCipherSuite();
        
        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        supportedCipherSuites = socketFactory.getSupportedCipherSuites();
         for(int i = 0; i < supportedCipherSuites.length; i++){
            result += supportedCipherSuites[i];
            System.out.println(i);
        }

        System.out.println("\nCIPHERSUITEDATA");
        System.out.println(result);

        dataResult = result;
    }

    public void fillCategoryData() {
        for (int i = 0; i < supportedCipherSuites.length; i++) {
            category.addPiece(new ResponsePiece("Supported Suite " + i, supportedCipherSuites[i]));
        }

    }

}
