package api_parser.docType;

import api_parser.model.RequestHeader;
import api_parser.model.RequestSource;
import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates an OpenAPI 3.0 compliant specification from Burp request/response objects.
 * The implementation mirrors the OpenApi31DocType generator but omits 3.1 specific
 * metadata to maintain compatibility with tools that only understand OpenAPI 3.0.x.
 */
public class OpenApi30DocType implements IDocType {
    private IBurpExtenderCallbacks callbacks;
    private PrintWriter stdout;

    public void setStdout(PrintWriter stdout) {
        this.stdout = stdout;
    }

    @Override
    public void setCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public IBurpExtenderCallbacks getCallbacks() {
        return this.callbacks;
    }

    @Override
    public String generate(List<RequestSource> requestSources, String docName) {
        JsonObject openApiDoc = new JsonObject();

        openApiDoc.addProperty("openapi", "3.0.3");

        JsonObject info = new JsonObject();
        info.addProperty("title", docName);
        info.addProperty("version", "1.0.0");
        openApiDoc.add("info", info);

        JsonArray servers = new JsonArray();
        JsonObject server = new JsonObject();
        server.addProperty("url", getBaseUrlFromRequests(requestSources));
        servers.add(server);
        openApiDoc.add("servers", servers);

        JsonObject paths = new JsonObject();
        JsonObject components = new JsonObject();
        JsonObject securitySchemes = new JsonObject();

        requestSources.stream()
                .collect(Collectors.groupingBy(RequestSource::getFolderName))
                .forEach((folderName, requests) -> {
                    requests.forEach(requestSource -> {
                        JsonObject pathItem = new JsonObject();
                        String method = getRequestMethod(requestSource.getReq()).toLowerCase();
                        JsonObject pathDetails = new JsonObject();

                        JsonObject responses = new JsonObject();
                        JsonObject response200 = new JsonObject();
                        response200.addProperty("description", "Successful response");
                        responses.add("200", response200);
                        pathDetails.add("responses", responses);

                        JsonObject requestBody = generateRequestBody(requestSource.getReq());
                        if (requestBody != null) {
                            pathDetails.add("requestBody", requestBody);
                        }

                        JsonObject authScheme = null;
                        for (RequestHeader header : getRequestHeaders(requestSource.getReq())) {
                            if (header.getKey().equalsIgnoreCase("Authorization")) {
                                authScheme = processAuthHeader(header.getValue());
                                pathDetails.add("security", createSecurityArray(authScheme.get("type").getAsString()));
                                addToSecuritySchemes(securitySchemes, authScheme);
                            }
                        }

                        if (authScheme == null) {
                            pathDetails.addProperty("x-noauth", "true");
                        }

                        pathItem.add(method, pathDetails);
                        paths.add(getRequestPath(requestSource.getReq()), pathItem);
                    });
                });

        openApiDoc.add("paths", paths);

        if (securitySchemes.size() > 0) {
            components.add("securitySchemes", securitySchemes);
            openApiDoc.add("components", components);
        }

        return openApiDoc.toString();
    }

    private String getContentType(IHttpRequestResponse request) {
        return getRequestHeaders(request).stream()
                .filter(header -> header.getKey().equalsIgnoreCase("Content-Type"))
                .map(RequestHeader::getValue)
                .findFirst()
                .orElse(null);
    }

    private JsonObject generateRequestBody(IHttpRequestResponse request) {
        String contentType = getContentType(request);
        String body = getRequestBody(request);

        if (body == null || body.isEmpty()) {
            return null;
        }

        JsonObject requestBody = new JsonObject();
        JsonObject content = new JsonObject();
        JsonObject mediaTypeObject = new JsonObject();

        if ("application/json".equalsIgnoreCase(contentType)) {
            JsonObject exampleWrapper = new JsonObject();
            exampleWrapper.add("example", JsonParser.parseString(body).getAsJsonObject());
            mediaTypeObject.add("examples", exampleWrapper);
        } else if ("application/xml".equalsIgnoreCase(contentType)) {
            JsonObject exampleWrapper = new JsonObject();
            try {
                JsonObject xmlJson = convertXmlToJson(body);
                exampleWrapper.add("example", xmlJson);
            } catch (Exception e) {
                exampleWrapper.addProperty("example", body);
            }
            mediaTypeObject.add("examples", exampleWrapper);
        } else {
            JsonObject exampleWrapper = new JsonObject();
            exampleWrapper.addProperty("example", body);
            mediaTypeObject.add("examples", exampleWrapper);
        }

        content.add(contentType != null ? contentType : "text/plain", mediaTypeObject);

        requestBody.add("description", new JsonPrimitive("Some Desc..."));
        requestBody.add("content", content);

        return requestBody;
    }

    private JsonObject convertXmlToJson(String xmlString) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
        Document doc = builder.parse(input);

        JsonObject jsonObject = new JsonObject();
        convertXmlNodeToJson(doc.getDocumentElement(), jsonObject);
        return jsonObject;
    }

    private void convertXmlNodeToJson(Node node, JsonObject jsonObject) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;

            if (element.getChildNodes().getLength() == 1 && element.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                jsonObject.addProperty(element.getTagName(), element.getTextContent().trim());
            } else {
                JsonObject childJson = new JsonObject();
                for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                    Node childNode = element.getChildNodes().item(i);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        convertXmlNodeToJson(childNode, childJson);
                    }
                }
                if (childJson.size() > 0) {
                    jsonObject.add(element.getTagName(), childJson);
                }
            }
        }
    }

    private void addToSecuritySchemes(JsonObject securitySchemes, JsonObject authScheme) {
        String type = authScheme.get("type").getAsString();
        if (!securitySchemes.has(type)) {
            JsonObject scheme = new JsonObject();
            if ("bearer".equalsIgnoreCase(type)) {
                scheme.addProperty("type", "http");
                scheme.addProperty("scheme", "bearer");
                scheme.addProperty("bearerFormat", "JWT");
            } else if ("basic".equalsIgnoreCase(type)) {
                scheme.addProperty("type", "http");
                scheme.addProperty("scheme", "basic");
            } else {
                scheme.addProperty("type", type);
            }
            securitySchemes.add(type, scheme);
        }
    }

    private JsonArray createSecurityArray(String type) {
        JsonArray securityArray = new JsonArray();
        JsonObject securityObject = new JsonObject();
        securityObject.add(type, new JsonArray());
        securityArray.add(securityObject);
        return securityArray;
    }

    @Override
    public JsonObject processAuthHeader(String authValue) {
        JsonObject auth = new JsonObject();

        if (authValue == null || authValue.isEmpty()) {
            return auth;
        }

        if (authValue.toLowerCase().startsWith("bearer ")) {
            auth.addProperty("type", "bearer");
        } else if (authValue.toLowerCase().startsWith("basic ")) {
            auth.addProperty("type", "basic");
        } else {
            String[] parts = authValue.trim().split("\\s+", 2);
            if (parts.length == 2) {
                auth.addProperty("type", parts[0].toLowerCase());
            } else {
                auth.addProperty("type", "unknown");
            }
        }

        return auth;
    }

    private String getBaseUrlFromRequests(List<RequestSource> requestSources) {
        return requestSources.stream()
                .map(requestSource -> getRequestUrl(requestSource.getReq()))
                .map(url -> {
                    try {
                        return new java.net.URL(url).getProtocol() + "://" + new java.net.URL(url).getHost();
                    } catch (java.net.MalformedURLException e) {
                        return null;
                    }
                })
                .filter(url -> url != null)
                .distinct()
                .findFirst()
                .orElse(null);
    }
}
