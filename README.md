# Server Analyzer

Web Application Configuration Vulnerability Analyzer
Written using springboot, maven and java.

## Description
Given a host and port, this application gathers the following information about the server: 
Available protocols. (eg. SSL3, TLS 1.1, TLS 1.2)
Available ssl ciphers.
Hosted certificate. (Including chain)
Response headers.

A report is generated on the front end that shows security vulnerabilities.
The user is able to save the information as a pdf.
To check for certain vulnerabilities there is a configuration file that the user will need to use to flag certain information. 

![alt text](analyzer.PNG  "Screenshot")

## Documentation 
The “Server Analyzer” is a small web application that identifies potential vulnerabilities on a given server and gathering SSL connection information. This application has multiple functions that check available protocols, SSL Ciphers, Response Header, and Hosted Certificates.

## Functionality: 

### Datapieces:
The DataPieces directory contains all of the different modules that go out and retrieve information from the server.  All of these functionalities, be it certificate chain, supported protocols, or response header, are all inheriting their core structure from the DataPiece.java class.
Pathway for Data Retrieval Modules(Items  1-3):
	/src/main/java/com/serveranalyer/BackEnd/DataPieces/.


### Protocol / Cipher Suites Function:
The ProtocolData.java file is used to find supported protocols and cipher suites to the associated site. It first gathers a list of all protocols the client supports. If for any reason a specific protocol is not supported, it will refuse to establish a connection with it (even if a box for it on the Front-End is checked or the website itself supports it). After it establishes a connection, it then goes and performs a handshake for every Cipher Suite supported for said protocol.

For this to work, a Socket is opened. After opening a Socket, the instance is switched out for the first protocol on the list. After switching out, it then force checks to see if the client supports it. When everything is set, it pulls a list of supported Cipher Suites to use, then moves on to the next for-loop. The handshakes are performed for each Cipher Suite it selects. For each one performed, it will print (in console) details about whether or not it was successful.

This process can take some time, however. Handshakes are performed one at a time to help prevent the possibility of setting off a security system a site may have.

### Hosted Certificate Chain Function:

The CertChainData.java file is the core module for this purpose.  To accomplish this, an HttpsConnection object and an SSL session object are passed in from ServerAnalyzerBackEnd.java.  The session object is used to call the method getPeerCertificates(), which is a built-in method into Java SSLsession from javax.net.ssl.  This method returns an array of Certificate objects, which is included in java.security.cert.

After this is completed, the array of Certificates is looped through and casted to an X509 Certificate object (which is also included in java.security.cert).  This is to allow for much more information to be obtained about the certificate, such as its Distinguished Name.  This is accomplished by calling the getSubjectx500Principal() method on the x509 Certificate object.  Following that method call, that information is concatenated onto a string called result, and then its validity is checked by calling the checkValidity method on the object (also built into the X509 Certificate class).

After the Cert has been checked, the variable dataResult is set to the contents of result and a ResponsePiece object is created.  To get the nuts and bolts of how to ResponsePiece methods within each DataPiece module work, please read the documentation below regarding the JSON packager.
 
### Response Header Function:
The ResponseHeaderData.java file is another core module. A HttpsURLConnection object and an SSL session object are passed in from ServerAnalyzerBackEnd.java. The HttpsURLConnections object is used to call the getHeaderFields method. This method returns an unmodifiable Map of the header fields. The Map keys are Strings that represent the response-header field names. Each Map value is an unmodifiable List of Strings that represents the corresponding field values. 

The response header contains the date, size, and type of file that the server is sending back to the client as well as server information. The header is attached to the files sent back to the client.

### Flagging Function:
	The data is checked against a configuration file located in the static 
directory called config.yaml.  The actual checks are performed on the 
backend. All code related to the configuration file can be found in the BackEnd/Config/ folder. ConfigRoot, ConfigCategory, ConfigFlag, and ConfigPresent are all java representations of the data found in the yaml file (see below). These contain methods to get the data out of them. ConfigManager controls everything else about the config file. Whenever data is fetched the ConfigManager will load the config file, and parse the data before returning it to the user. flagBasedOnConfig will take a given ResponsePackage load the config file, and pass on down to flagSet if there is data to be flagged. flagSet will cycle through each category, and if anything needs to be flagged pass information down to flagCategory. flagCategory will check what the responseCategory is and handle flagging based on category. 

checkIfPresent will pass data onto checkPresenceList if it finds information in the config file. If anything is found as not present, the configManager will create a new ResponseCategory and ResponsePiece in the ResponsePackage to send the data to the frontend.


After the JSON object is received by the front end.  The JavaScript 
located in script.js is used to then complete the parsing and flagging  
of the output.  In the postRequest function, there is an onload function that calls methods to parse the returning JSON object called certChainParse(), responseHeaderParse(), and protocolParse().  These are called to parse their respective portions of the JSON object and loop through all of its fields to see what the flag value is (‘n’ for no flag, ‘y’ for yellow, or ‘r’ for red). The list is created by first creating two unordered lists, one representing the ‘full report’ (all items will be included), and the other representing the issues report (only highlighted items will be included). After that, the list items are appended for each field of the JSON object subfields (for example ‘package.certificate_chain.[any of its fields]’).

In this example, package represents the whole JSON object returned from the back end, certificate_chain represents the subfield within package which has its own fields nested inside (which are being looped through to parse).  These subfields are appended as list items (for example CN: [value]), and flagged as each subfield has its own flag field.  After all of the list items are appended to both the unordered lists, the lists themselves are appended to divs contained in the index.html page (which is the static home page that one sees when first accessing the application).

This process is done with all of the printed fields (certificate chain, response header, and protocols) with only slight variations between the three which necessitate seperate functions. 

Red Flag:  A red flag represents critical concerns.
Yellow Flag: A yellow flag represents a significant but less critical concern.

The config file is called config.yaml and is located in the resources/static/ folder with index.html. A sample config file is included with a sample configuration and more details. The general structure of the config.yaml file can be found below. 


<color> can be “red” or “yellow”
<category> the title as it shows in the output. (When inside a “present:” section you can also use “all” for a category to check if it’s in any categories.)
<title> the bolded part on the left of the output. An exception is with cert chain, O will check all O values (e.g. O, O2, O3, etc.) instead of specifying a number
<value> the value to check for flagging. Checks if value is present, for example, “google” will flag “Google, Inc”



<color>:
  - category: <category>
    flags:
      - title: <title>
        value: <value>
      - title: <title>
        value: <value>
    present:
      - title: <title>
        value: <value>
  - category: <category>
    flags:
      - title: <title>
        value: <value>
      - title: <title>
        value: <value>
    present:
      - title: <title>
        value: <value>

	
### Saving Function:
After findings are generated, a save button will appear. Once clicked, the report will be generated as a PDF and will give the user the option to choose a save location. Essentially the functionality takes what appears in the results section and then generates a pdf version of the results. If the save feature does not allow the user to choose a file location, then check whether the browser has already chosen a location for files to be downloaded.
 
URL Validation and Autocomplete Helper
After the URL is passed into the back end, it is sanitized using the 
validateURLsyntax() method. Using regular expressions and other string comparison methods, the URL must pass a variety of checks to make sure it is usable:
If the URL doesn’t start with https://, force it to begin with that.
Check if, generally speaking, the URL “looks like” an actual URL.
If there is no port number, default to using port 443.
If there is a port specified, make sure the port makes sense (e.g. is a number).
Only syntax is checked at this stage. A user entering garbage that passes the sanity checks (e.g. “fhvuiolbvudvjk.com”) is checked when attempting to open the socket.

Packager
Inside the BackEnd/Packager/ folder are the classes responsible for packaging the data. The class structure mimics JSON objects in some ways, and each class has a toString method that will output the data in a way that will allow for a valid JSON.parse() on the front end, to convert data into a JSON object. ResponseData is the base, holding only a title and data, and can be used to represent a single JSON variable. ResponsePiece holds two ResponseData objects: one to represent a flag and another to represent its own data. ResponseCategory holds a title, a flag, and a list of ResponsePieces. Finally, the ResponsePackage class holds a list of Response pieces, and is the top level. 


How To Use:

How To Use Website:
Type in the textbox your desired url to check.
Url examples: www.google.com, https://www.google.com, google.com, https://www.google.com:443
Click the button “Analyze” below the textbox to generate the report.
After the report is generated you have the option to save the output into a PDF file. This is accomplished by clicking the “Save” button in the bottom right-hand corner. See item 5 “Saving Function” for more information.	
Issues:

Known Issues: 
Since Let’s Encrypt is not part of the trust store by default, you will need to ensure that you have either of the minimum versions of Java installed:
Java 7u111 or greater
Java 8u101 or greater
Some older protocols are not available due to hard coding within the Spring Boot Framework and Tomcat.
Although we have exhaustively tested various websites, some false flagging may occur.
Using an ‘Ad Blocker’ in the browser while using Server Analyzer may cause output to be partially displayed or not displayed at all.

