package com.commercebank.serveranalyzer.BackEnd.DataPieces;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.CertificateException;

import com.commercebank.serveranalyzer.BackEnd.Packager.ResponsePiece;

public class ProtocolData extends DataPiece{
    private String[] requestedProtocols;

    private ResponsePiece responsePieces[];

    public ProtocolData(String[] requestedProtocols)
    {
        super("available_protocols");
        this.requestedProtocols = requestedProtocols;

        if(requestedProtocols != null) {
            responsePieces = new ResponsePiece[requestedProtocols.length];
            for (int i = 0; i < requestedProtocols.length; i++) {
                responsePieces[i] = new ResponsePiece(requestedProtocols[i]);
            }
        }
    }

    @Override
    public void fetchData(HttpsURLConnection connection, SSLSession session) {
        if (requestedProtocols != null) {
            System.out.println("" + "\nSTART PROTOCOL CHECK...");
            String[] exhaustiveProtocolList = {"SSLv2", "SSLv2hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"};
            ArrayList<String> supportedProtocols = new ArrayList<String>();
            //String[] enabledProtocols = null;
            String[] sslCiphers = null;
            String result = "";

            String host = session.getPeerHost();
            int port = session.getPeerPort();
            InetSocketAddress address = new InetSocketAddress(host, port);

            SecureRandom randomKey = new SecureRandom();

            int connectTimeout = 0;
            int readTimeout = 1000;

            boolean halt = false;   //Halt everything if something goes wrong.

            //[TODO] When custom Trust and Key Managers are created, implement a way to read from file.
            TrustManager[] trustManagers = null;
            KeyManager[] keyManagers = null;
            //Use the default Trust Managers (until custom ones are implemented)
            try {
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmFactory.init((KeyStore) null);

                trustManagers = tmFactory.getTrustManagers();

                keyManagers = null;

                SSLSocket socket = (SSLSocket) connection.getSSLSocketFactory().createSocket();
                String[] t = socket.getSupportedProtocols();
                for (int i = 0; i < t.length; i++) {
                    supportedProtocols.add(t[i]);
                }
                //enabledProtocols = socket.getEnabledProtocols();

            } catch (NoSuchAlgorithmException AlgorithmE) {
                System.out.println("COULD NOT RETRIEVE DEFAULT TRUST MANAGER (???)");
                AlgorithmE.printStackTrace();
            } catch (KeyStoreException KeyE) {
                System.out.println("COULD NOT SET DEFAULT TMF KEYSTORE (???)");
                KeyE.printStackTrace();
            } catch (IOException e) {
                System.out.println("UNABLE TO CREATE A SOCKET.");
                e.printStackTrace();
                halt = true;
            }

            System.out.println("SUPPORTED ON YOUR SIDE:");
            for (int i = 0; i < supportedProtocols.size(); i++) {
                System.out.println(supportedProtocols.get(i));
            }

            System.out.println("ENABLED ON YOUR SIDE:");
            for (int i = 0; i < requestedProtocols.length; i++) {
                System.out.println(requestedProtocols[i]);
            }

            System.out.println("CURRENTLY USING " + session.getProtocol() + " PROTOCOL.");

            HashSet<String> ciphers = new HashSet<>();        //Create an empty hash list to hold cipher suites

            String lastProtocol;     //Last protocol used


            for (int i = 0; i < requestedProtocols.length && !halt; ++i) {
                String protocol = requestedProtocols[i];        //Selects a given protocol from an array within a for-loop
                String[] supportedCiphers = null;            //Placeholder for determining if the client can use the cipher suite

                try {
                    SSLContext s = SSLContext.getInstance(protocol);
                    s.init(null, null, randomKey);
                    supportedCiphers = s.getSocketFactory().getSupportedCipherSuites();
                } catch (NoSuchAlgorithmException noAlgorithm) {
                    System.out.print(protocol + " IS NOT SUPPORTED BY THE CLIENT! SKIPPING...");
                    supportedProtocols.remove(protocol);
                    continue;                               //Skip this protocol entirely.
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;                               //Skip the protocol in case of an unknown exception
                }
                // Restrict all cipher suites to those that are specified by sslCipherSuites
                ciphers.clear();
                ciphers.addAll(Arrays.asList(supportedCiphers));
                if (null != sslCiphers)
                    ciphers.retainAll(Arrays.asList(sslCiphers));

                if (ciphers.isEmpty()) {
                    System.err.println("OVERLAPPING CIPHER SUITES WERE NOT FOUND FOR " + protocol);
                    supportedProtocols.remove(protocol);
                    continue;                               //Continue with the next protocol
                }

                lastProtocol = "";

                for (Iterator<String> j = ciphers.iterator(); j.hasNext() && !halt; ) {
                    String cipherSuite = j.next();
                    String status;

                    SSLSocket socket = null;
                    String error = null;

                    try {
                        SSLSocketFactory socketFactory = getSSLSocketFactory(protocol, new String[]{protocol}, new String[]{cipherSuite}, randomKey, trustManagers, keyManagers);
                        socket = createSSLSocket(address, host, port, connectTimeout, readTimeout, socketFactory);

                        socket.startHandshake();

                        SSLSession sess = socket.getSession();

                        assert protocol.equals(sess.getProtocol());
                        assert cipherSuite.equals(sess.getCipherSuite());


                        status = "Accepted";
                        if (lastProtocol.isEmpty()) {
                            lastProtocol = protocol;
                            result += lastProtocol + " ";
                        }
                    } catch (SSLHandshakeException handshakeE) {
                        Throwable cause = handshakeE.getCause();
                        if (null != cause && cause instanceof CertificateException) {
                            status = "Untrusted";
                            error = "Server certificate is not trusted. All other connections will fail similarly.";
                            halt = true;
                        } else
                            status = "Rejected";

                        error = "SHE: " + handshakeE.getLocalizedMessage() + ", type=" + handshakeE.getClass().getName() + ", nested=" + handshakeE.getCause();
                    } catch (SSLException SSLEx) {
                        error = "SE: " + SSLEx.getLocalizedMessage();

                        status = "Rejected";
                    } catch (SocketTimeoutException TimeoutE) {
                        error = "SocketException" + TimeoutE.getLocalizedMessage();

                        status = "Timeout";
                    } catch (SocketException SocketE) {
                        error = SocketE.getLocalizedMessage();

                        status = "Failed";
                    } catch (IOException IOEx) {
                        error = IOEx.getLocalizedMessage();

                        IOEx.printStackTrace();
                        status = "Failed";
                    } catch (Exception e) {
                        error = e.getLocalizedMessage();

                        e.printStackTrace();
                        status = "Failed";
                    } finally {
                        if (socket != null) try {
                            socket.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }

                    if (error != null)
                        System.out.print(String.format("\nSTATUS: %s\nPROTOCOL: %s\nCIPHER SUITE: %s\nERROR CAUGHT: %s",
                                status,
                                protocol,
                                cipherSuite,
                                error));
                    else if (!"Rejected".equals(status))
                        System.out.print(String.format("\nSTATUS: %s\nPROTOCOL: %s\nCIPHER SUITE: %s",
                                status,
                                protocol,
                                cipherSuite));
                }

                responsePieces[i].setDataDirect(arrayToJSONString(supportedCiphers));
            }

            System.out.println(result);
            dataResult = result;
            System.out.println("END PROTOCOL CHECK.\n");

        } else {
            System.out.println("PROTOCOL CHECK SKIPPED!");
            dataResult = "disabled";
        }
    }

    private static SSLSocket createSSLSocket(InetSocketAddress a, String h, int p, int rTimeout, int cTimeout, SSLSocketFactory sf)
        throws IOException {
        Socket s = new Socket();
        s.setSoTimeout(rTimeout);
        s.connect(a, cTimeout);

        return (SSLSocket)sf.createSocket(s, h, p, true);
    }

    private static SSLSocketFactory getSSLSocketFactory(String protocol, String[] enabledProtocols, String[] sslCipherSuites, SecureRandom r, TrustManager[] tm, KeyManager[] km)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance(protocol);
        if (tm != null && km != null)
            sc.init(km, tm, r);
        else
            sc.init(null, null, r);
        SSLSocketFactory socketFactory = sc.getSocketFactory();

        //[TODO] Find a way to use a custom SSLSocketFactory when the keymanager and trustmanager are NOT null.
        //if (enabledProtocols != null || sslCipherSuites != null)
            //socketFactory = new SSLSocketFactory(socketFactory, enabledProtocols, sslCipherSuites);

        return socketFactory;
    }

    //[TODO] Same as above
    private SSLSocketFactory CustomSSLSocketFactory(SSLSocketFactory baseFactory, String[] enabledProtocols, String[] sslCipherSuites) {
        return null;
    }

    private String arrayToJSONString(String[] array) {
        StringBuilder output = new StringBuilder();
        output.append("[");
        for(int i = 0; i < array.length; i++){
            output.append("\\\"");
            output.append(array[i]);
            output.append("\\\",");
        }
        output.deleteCharAt(output.lastIndexOf(","));
        output.append("]");
        return output.toString();
    }

    //needs to fill out the categoy defined at the start of the method
    public void fillCategoryData() {
        if(requestedProtocols == null) {
            category.addPiece(new ResponsePiece(dataName, "no protocols requested"));
        } else {
            for (int i = 0; i < responsePieces.length; i++) {
                category.addPiece(responsePieces[i]);
            }
        }
    }
}