var protocolList = ["SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"];

var listItem;
var text;
var package;
var coll = document.getElementsByClassName("dropdown");
var i;
var problemsList;
var fullReport;

window.onload = function () {
    problemsList = document.getElementById("issues");
    fullReport = document.getElementById("FR");
};

function clearList(){
    var fr = document.getElementById("FR");
    var prob = document.getElementById("issues");

    while (fr.firstChild) {
        fr.removeChild(fr.firstChild);
    }

    while(prob.firstChild){
        prob.removeChild(prob.firstChild);
    }
}

function start() {
    var urlField = document.getElementById("url");
    urlField.addEventListener("keydown", runOnEnter, false);
    var protocolCheck = document.getElementById("protocolCheck");
    protocolCheck.addEventListener("change", displayProtocolOptions, false);
    var button = document.getElementById("btn");
    button.addEventListener("click", postRequest, false);
    var pdfButton = document.getElementById("pdf");
    pdfButton.addEventListener("click", saveToPDF, false);


    for (i = 0; i < coll.length; i++) {
        coll[i].addEventListener("click", function () {
            this.classList.toggle("active");
            var content = this.nextElementSibling;
            if (content.style.display === "block") {
                content.style.display = "none";
            } else {
                content.style.display = "block";
            }
        });
    }
}

function runOnEnter(e) {
    if (e.key === "Enter") {
        document.getElementById("btn").click();
    }
}

//Displays a list of hardcoded protocols on the screen. They are IDed from protocol0 to protocolN.
function displayProtocolOptions() {
    if (protocolCheck.checked) {
        var checkboxesString = "";
        for (var i = 0; i < protocolList.length; i++) {
            checkboxesString += "<input type = \"checkbox\" id = \"protocol" + i + "\" style = \"margin-left: 35px;\" checked>";
            checkboxesString += "<label id = \"protocol" + i + "Label\">" + protocolList[i] + "</label>";
        }
        protocols.innerHTML = checkboxesString;
    }
    else {
        protocols.innerHTML = "";
    }
}

function certChainParse() {
    var certChainListFR = document.createElement("ul");
    var certChainListIssues = document.createElement("ul");
    var title = document.createTextNode("Certificate Chain");
    var temp;

    certChainListFR.innerHTML = "Certificate Chain";
    certChainListIssues.innerHTML = "Certificate Chain";

    //no flags, color accordingly, add to full report
    if (package.certificate_chain.flag == "n") {
        console.log("n");
    }
    //medium level concern, color yellow and add to concerns report, as well as full report
    else if (package.certificate_chain.flag == "y") {
        console.log("y");
    }
    //top level concern, color red and add to concerns report as well as full report
    else {
        console.log("r");
    }

    console.log(package.certificate_chain.CN.data);
    for (var x in package.certificate_chain) {
        if (x != "flag") {
            console.log(x);
            console.log(package.certificate_chain[x].data);
            if (package.certificate_chain[x].flag == "r") {
                //print to problems node and make it red, then append to full report node
                listItem = document.createElement("li");
                text = document.createTextNode(String(x) + " : " + String(package.certificate_chain[x].data));
                listItem.style.backgroundColor = "#ff4747";
                listItem.appendChild(text);
                temp = listItem.cloneNode(true);

                certChainListFR.appendChild(listItem);
                console.log(certChainListFR);
                certChainListIssues.appendChild(temp);
            }
            else if (package.certificate_chain[x].flag == "y") {
                //append to problems node and make it yellow, then append to full report
                listItem = document.createElement("li");
                text = document.createTextNode(String(x) + " : " + String(package.certificate_chain[x].data));

                listItem.style.backgroundColor = "#fbff49";
                listItem.appendChild(text);
                temp = listItem.cloneNode(true);

                certChainListFR.appendChild(listItem);
                certChainListIssues.appendChild(temp);
            }

            else {
                //just append to full report
                listItem = document.createElement("li");
                text = document.createTextNode(String(x) + " : " + String(package.certificate_chain[x].data));

                listItem.appendChild(text);
                certChainListFR.appendChild(listItem);

            }

            console.log(certChainListFR);

            fullReport.appendChild(certChainListFR);
            problemsList.appendChild(certChainListIssues);

        }
    }
}

function responseHeaderParse() {
    var responseHeaderFR = document.createElement("ul");
    var responseHeaderIssues = document.createElement("ul");
    var temp;

    responseHeaderFR.innerHTML = "Response Header";
    responseHeaderIssues.innerHTML = "Response Header";

    for (var x in package.response_header) {
        if (x != "flag") {
            if (package.response_header[x].flag == "r") {
                //print to problems node and make it red, then append to full report node
                listItem = document.createElement("li");
                text = document.createTextNode(String(x) + " : " + String(package.response_header[x].data));
                listItem.style.backgroundColor = "#ff4747";
                listItem.appendChild(text);
                temp = listItem.cloneNode(true);

                responseHeaderFR.appendChild(listItem);
                responseHeaderIssues.appendChild(temp);
            }
            else if (package.response_header[x].flag == "y") {
                //append to problems node and make it yellow, then append to full report
                listItem = document.createElement("li");
                text = document.createTextNode(String(x) + " : " + String(package.response_header[x].data));

                listItem.style.backgroundColor = "#fbff49";
                listItem.appendChild(text);
                temp = listItem.cloneNode(true);

                responseHeaderFR.appendChild(listItem);
                responseHeaderIssues.appendChild(temp);
            }

            else {
                //just append to full report
                listItem = document.createElement("li");
                text = document.createTextNode(String(x) + " : " + String(package.response_header[x].data));

                listItem.appendChild(text);
                responseHeaderFR.appendChild(listItem);

            }

            fullReport.appendChild(responseHeaderFR);
            problemsList.appendChild(responseHeaderIssues);

        }
    }
}

//Ajax script
function postRequest() {
    clearList();
    var url = document.getElementById("url").value;
    //Don't proceed if URL field was blank.
    if (url === "") {
        return;
    }

    var xhr = new XMLHttpRequest(),
        data = "url=" + url;

    //Adds protocol data as a comma-delimited string to be sent.
    data += "&protocols=";
    if (protocolCheck.checked) {
        for (var i = 0; i < protocolList.length; i++) {
            if (document.getElementById("protocol" + i).checked) {
                data += (document.getElementById("protocol" + i + "Label").innerHTML) + ",";
            }
        }
    }

    xhr.open("POST", "/analyzeURL", true);
    xhr.setRequestHeader('Content-Type', "application/x-www-form-urlencoded");
    xhr.send(data);
    var loader=document.getElementById("loader");
    loader.style.display = "block";

    xhr.onload = function () {
        //document.getElementById("output").innerHTML = this.responseText;
        console.log(this.responseText);
        package = JSON.parse(this.responseText);
        console.log(package);
        //document.getElementById("output").innerHTML = this.responseText;
        //testPackage = JSON.parse(this.responseText);
        loader.style.display="none";
        certChainParse();
        responseHeaderParse();
    }
}

function saveToPDF()
{
    // Just converts the full report right now. I will add the possible issues and format it to look nicer.
    var element = document.getElementById('FR');
    var opt = {
        margin:      1,
        filename:    'results.pdf',
        html2canvas: {scale: 2},
        jsPDF:       { unit: 'in', format: 'letter', orientation: 'portrait' }
    };
    html2pdf().set(opt).from(element).save();
}

window.addEventListener("load", start, false);

