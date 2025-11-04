package api_parser;

import api_parser.docType.IDocType;
import api_parser.docType.OpenApi30DocType;
import api_parser.docType.OpenApi31DocType;
import api_parser.docType.PostmanDocType;
import api_parser.model.GenerateResponse;
import api_parser.model.RequestSource;
import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;

import java.io.PrintWriter;
import java.util.*;

public class Generator {
    private List<RequestSource> requestSources;
    private String docType;
    private String encoding;
    private IBurpExtenderCallbacks callbacks;

    PrintWriter stdout;
    PrintWriter stderr;

    public Generator(List<RequestSource> requestSources,IBurpExtenderCallbacks callbacks, String encoding, PrintWriter stdout,PrintWriter stderr){
        this.stdout = stdout;
        this.stderr = stderr;

        this.callbacks = callbacks;
        this.requestSources = requestSources;
        this.encoding = encoding;
    }
    public GenerateResponse generate(String docType,String docName,boolean isUnique) {
        GenerateResponse resp = new GenerateResponse();
        try {
            // RequestSources kontrolü
            if (this.requestSources == null || this.requestSources.isEmpty()) {
                resp.setStatus(false);
                resp.setMessage("Request sources are empty!");
                return resp;
            }

            if (isUnique){
                this.requestSources = getUniqueEndpoints(this.requestSources);
            }

            // docType kontrolü
            IDocType exporter = createDocType(docType);
            if (exporter == null) {
                resp.setStatus(false);
                resp.setMessage("Unsupported docType: " + docType);
                return resp;
            }

            exporter.setCallbacks(this.callbacks);
            exporter.setStdout(this.stdout);

            String result = exporter.generate(this.requestSources, docName);
            resp.setStatus(true);
            resp.setMessage(result);
        } catch (Exception ex) {
            resp.setStatus(false);
            resp.setMessage("Error occurred: " + ex.getMessage());
        }

        return resp;
    }

    private IDocType createDocType(String docType) {
        switch (docType) {
            case "postman-v2.1":
                return new PostmanDocType();
            case "openapi-v3.0":
                return new OpenApi30DocType();
            case "openapi-v3.1":
                return new OpenApi31DocType();
            default:
                return null;
        }
    }


    public List<RequestSource> getUniqueEndpoints(List<RequestSource> requestSources) {
        if (requestSources == null || requestSources.isEmpty()) {
            return Collections.emptyList();
        }

        // Benzersiz endpointleri takip etmek için Set
        Set<String> seenEndpoints = new HashSet<>();
        List<RequestSource> uniqueRequests = new ArrayList<>();

        for (RequestSource requestSource : requestSources) {
            IHttpRequestResponse req = requestSource.getReq();
            IRequestInfo requestInfo = callbacks.getHelpers().analyzeRequest(req);

            // Endpoint'in yalnızca path kısmını al
            String endpointPath = requestInfo.getUrl().getPath();

            // HTTP metodunu al
            String method = requestInfo.getMethod();

            // Benzersiz endpoint'i tanımla (method + endpointPath)
            String endpoint = method + " " + endpointPath;

            // Eğer daha önce görülmediyse, listeye ekle
            if (!seenEndpoints.contains(endpoint)) {
                seenEndpoints.add(endpoint);
                uniqueRequests.add(requestSource);
            }
        }

        return uniqueRequests;
    }
}
