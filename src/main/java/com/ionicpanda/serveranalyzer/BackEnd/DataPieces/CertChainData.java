package com.commercebank.serveranalyzer.BackEnd.DataPieces;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.commercebank.serveranalyzer.BackEnd.Packager.ResponsePiece;

import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.ArrayList;

public class CertChainData extends DataPiece {

    public CertChainData() {
        super("certificate_chain");
    }

    @Override
    public void fetchData(HttpsURLConnection connection, SSLSession session) {
            String result = "";
            try{

                //make array of Certificate objects and grab the cert chain from the https connection
                Certificate[] certificates = session.getPeerCertificates();

                //foreach c in Certificates print cert details to console.
                for(int i = 0; i < certificates.length; i++){
                    X509Certificate cert = (X509Certificate) certificates[i];
                    result += (cert.getSubjectX500Principal());
                    cert.checkValidity();
                }
            }

            catch (CertificateExpiredException e) {
                System.out.println("Certificate is expired");
                //call error json method after that is created
            }
            catch(CertificateNotYetValidException e){
                System.out.println("Certificate not yet valid");
//              //call error json
            }
            catch (IOException e){
                e.printStackTrace();
            }

            //System.out.println(result);
            dataResult = result;
    }

    public void fillCategoryData() {
        String[] blocks = dataResult.split(",");
        blocks = validateBlocks(blocks);
        for (String block : blocks) {
            String[] blockInfo = block.trim().split("=");
            category.addPiece(new ResponsePiece(blockInfo[0], blockInfo[1]));
        }
    }

    private String[] validateBlocks(String[] blocks) {
        ArrayList<String> newBlocks = new ArrayList<>();

        for(int i = 0; i < blocks.length; i++) {
            int index = blocks[i].indexOf('=');
            if(index < 0) {
                newBlocks.add(newBlocks.remove(newBlocks.size()-1) + ", " + blocks[i].trim());
            } else {
                newBlocks.add(blocks[i].trim());
            }
        }
        String[] result = new String[newBlocks.size()];
        for(int i = 0; i < newBlocks.size(); i++) {
            result[i] = newBlocks.get(i);
        }
        return result;
    }
}
