package api_parser;

import api_parser.model.GenerateResponse;
import api_parser.model.RequestSource;
import burp.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratorOpenApi30Test {

    @Test
    void generatesOpenApi30Document() {
        FakeCallbacks callbacks = new FakeCallbacks();
        FakeHttpService service = new FakeHttpService("example.com", 443, "https");
        byte[] requestBytes = ("GET /users HTTP/1.1\r\n" +
                "Host: example.com\r\n" +
                "Authorization: Bearer token\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n" +
                "{\"id\":1}").getBytes(StandardCharsets.UTF_8);
        FakeHttpRequestResponse request = new FakeHttpRequestResponse(requestBytes, service);

        List<RequestSource> sources = Collections.singletonList(new RequestSource(request, "GetUsers", "Users"));
        Generator generator = new Generator(sources, callbacks, "utf-8",
                new PrintWriter(OutputStream.nullOutputStream()),
                new PrintWriter(OutputStream.nullOutputStream()));

        GenerateResponse response = generator.generate("openapi-v3.0", "Test API", false);

        assertNotNull(response);
        assertTrue(response.getStatus(), "Expected generator to succeed");
        assertNotNull(response.getMessage());
        assertFalse(response.getMessage().isEmpty());
        assertTrue(response.getMessage().contains("\"openapi\":\"3.0.3\""));
    }

    private static class FakeHttpRequestResponse implements IHttpRequestResponse {
        private byte[] request;
        private byte[] response = new byte[0];
        private String comment;
        private String highlight;
        private IHttpService service;

        FakeHttpRequestResponse(byte[] request, IHttpService service) {
            this.request = request;
            this.service = service;
        }

        @Override
        public byte[] getRequest() {
            return request;
        }

        @Override
        public void setRequest(byte[] message) {
            this.request = message;
        }

        @Override
        public byte[] getResponse() {
            return response;
        }

        @Override
        public void setResponse(byte[] message) {
            this.response = message;
        }

        @Override
        public String getComment() {
            return comment;
        }

        @Override
        public void setComment(String comment) {
            this.comment = comment;
        }

        @Override
        public String getHighlight() {
            return highlight;
        }

        @Override
        public void setHighlight(String color) {
            this.highlight = color;
        }

        @Override
        public IHttpService getHttpService() {
            return service;
        }

        @Override
        public void setHttpService(IHttpService service) {
            this.service = service;
        }
    }

    private static class FakeHttpService implements IHttpService {
        private final String host;
        private final int port;
        private final String protocol;

        FakeHttpService(String host, int port, String protocol) {
            this.host = host;
            this.port = port;
            this.protocol = protocol;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public int getPort() {
            return port;
        }

        @Override
        public String getProtocol() {
            return protocol;
        }
    }

    private static class FakeCallbacks implements IBurpExtenderCallbacks {
        private final FakeExtensionHelpers helpers = new FakeExtensionHelpers();
        private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        @Override
        public void setExtensionName(String extensionName) { }

        @Override
        public IExtensionHelpers getHelpers() {
            return helpers;
        }

        @Override
        public OutputStream getStdout() {
            return stdout;
        }

        @Override
        public OutputStream getStderr() {
            return stderr;
        }

        @Override
        public void printOutput(String message) { }

        @Override
        public void printError(String message) { }

        @Override
        public void registerExtensionStateListener(IExtensionStateListener listener) { }

        @Override
        public List<IExtensionStateListener> getExtensionStateListeners() {
            return Collections.emptyList();
        }

        @Override
        public void removeExtensionStateListener(IExtensionStateListener listener) { }

        @Override
        public void registerHttpListener(IHttpListener listener) { }

        @Override
        public List<IHttpListener> getHttpListeners() {
            return Collections.emptyList();
        }

        @Override
        public void removeHttpListener(IHttpListener listener) { }

        @Override
        public void registerProxyListener(IProxyListener listener) { }

        @Override
        public List<IProxyListener> getProxyListeners() {
            return Collections.emptyList();
        }

        @Override
        public void removeProxyListener(IProxyListener listener) { }

        @Override
        public void registerScannerListener(IScannerListener listener) { }

        @Override
        public List<IScannerListener> getScannerListeners() {
            return Collections.emptyList();
        }

        @Override
        public void removeScannerListener(IScannerListener listener) { }

        @Override
        public void registerScopeChangeListener(IScopeChangeListener listener) { }

        @Override
        public List<IScopeChangeListener> getScopeChangeListeners() {
            return Collections.emptyList();
        }

        @Override
        public void removeScopeChangeListener(IScopeChangeListener listener) { }

        @Override
        public void registerContextMenuFactory(IContextMenuFactory factory) { }

        @Override
        public List<IContextMenuFactory> getContextMenuFactories() {
            return Collections.emptyList();
        }

        @Override
        public void removeContextMenuFactory(IContextMenuFactory factory) { }

        @Override
        public void registerMessageEditorTabFactory(IMessageEditorTabFactory factory) { }

        @Override
        public List<IMessageEditorTabFactory> getMessageEditorTabFactories() {
            return Collections.emptyList();
        }

        @Override
        public void removeMessageEditorTabFactory(IMessageEditorTabFactory factory) { }

        @Override
        public void registerScannerInsertionPointProvider(IScannerInsertionPointProvider provider) { }

        @Override
        public List<IScannerInsertionPointProvider> getScannerInsertionPointProviders() {
            return Collections.emptyList();
        }

        @Override
        public void removeScannerInsertionPointProvider(IScannerInsertionPointProvider provider) { }

        @Override
        public void registerScannerCheck(IScannerCheck check) { }

        @Override
        public List<IScannerCheck> getScannerChecks() {
            return Collections.emptyList();
        }

        @Override
        public void removeScannerCheck(IScannerCheck check) { }

        @Override
        public void registerIntruderPayloadGeneratorFactory(IIntruderPayloadGeneratorFactory factory) { }

        @Override
        public List<IIntruderPayloadGeneratorFactory> getIntruderPayloadGeneratorFactories() {
            return Collections.emptyList();
        }

        @Override
        public void removeIntruderPayloadGeneratorFactory(IIntruderPayloadGeneratorFactory factory) { }

        @Override
        public void registerIntruderPayloadProcessor(IIntruderPayloadProcessor processor) { }

        @Override
        public List<IIntruderPayloadProcessor> getIntruderPayloadProcessors() {
            return Collections.emptyList();
        }

        @Override
        public void removeIntruderPayloadProcessor(IIntruderPayloadProcessor processor) { }

        @Override
        public void registerSessionHandlingAction(ISessionHandlingAction action) { }

        @Override
        public List<ISessionHandlingAction> getSessionHandlingActions() {
            return Collections.emptyList();
        }

        @Override
        public void removeSessionHandlingAction(ISessionHandlingAction action) { }

        @Override
        public void unloadExtension() { }

        @Override
        public void addSuiteTab(ITab tab) { }

        @Override
        public void removeSuiteTab(ITab tab) { }

        @Override
        public void customizeUiComponent(java.awt.Component component) { }

        @Override
        public IMessageEditor createMessageEditor(IMessageEditorController controller, boolean editable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getCommandLineArguments() {
            return new String[0];
        }

        @Override
        public void saveExtensionSetting(String name, String value) { }

        @Override
        public String loadExtensionSetting(String name) {
            return null;
        }

        @Override
        public ITextEditor createTextEditor() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendToRepeater(String host, int port, boolean useHttps, byte[] request, String tabCaption) { }

        @Override
        public void sendToIntruder(String host, int port, boolean useHttps, byte[] request) { }

        @Override
        public void sendToIntruder(String host, int port, boolean useHttps, byte[] request, List<int[]> payloadPositions) { }

        @Override
        public void sendToComparer(byte[] data) { }

        @Override
        public void sendToSpider(URL url) { }

        @Override
        public IScanQueueItem doActiveScan(String host, int port, boolean useHttps, byte[] request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IScanQueueItem doActiveScan(String host, int port, boolean useHttps, byte[] request, List<int[]> insertionPointOffsets) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void doPassiveScan(String host, int port, boolean useHttps, byte[] request, byte[] response) { }

        @Override
        public IHttpRequestResponse makeHttpRequest(IHttpService service, byte[] request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IHttpRequestResponse makeHttpRequest(IHttpService service, byte[] request, boolean followRedirections) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] makeHttpRequest(String host, int port, boolean useHttps, byte[] request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] makeHttpRequest(String host, int port, boolean useHttps, byte[] request, boolean followRedirections) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] makeHttp2Request(IHttpService service, List<IHttpHeader> headers, byte[] body) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] makeHttp2Request(IHttpService service, List<IHttpHeader> headers, byte[] body, boolean followRedirections) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] makeHttp2Request(IHttpService service, List<IHttpHeader> headers, byte[] body, boolean followRedirections, String requestType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isInScope(URL url) {
            return false;
        }

        @Override
        public void includeInScope(URL url) { }

        @Override
        public void excludeFromScope(URL url) { }

        @Override
        public void issueAlert(String message) { }

        @Override
        public IHttpRequestResponse[] getProxyHistory() {
            return new IHttpRequestResponse[0];
        }

        @Override
        public IHttpRequestResponse[] getSiteMap(String urlPrefix) {
            return new IHttpRequestResponse[0];
        }

        @Override
        public IScanIssue[] getScanIssues(String urlPrefix) {
            return new IScanIssue[0];
        }

        @Override
        public void generateScanReport(String format, IScanIssue[] issues, java.io.File file) { }

        @Override
        public List<ICookie> getCookieJarContents() {
            return Collections.emptyList();
        }

        @Override
        public void updateCookieJar(ICookie cookie) { }

        @Override
        public void addToSiteMap(IHttpRequestResponse item) { }

        @Override
        public void restoreState(java.io.File file) { }

        @Override
        public void saveState(java.io.File file) { }

        @Override
        public java.util.Map<String, String> saveConfig() {
            return Collections.emptyMap();
        }

        @Override
        public void loadConfig(java.util.Map<String, String> config) { }

        @Override
        public String saveConfigAsJson(String... filterNames) {
            return "{}";
        }

        @Override
        public void loadConfigFromJson(String configJson) { }

        @Override
        public void setProxyInterceptionEnabled(boolean enabled) { }

        @Override
        public String[] getBurpVersion() {
            return new String[] {"2025", "4"};
        }

        @Override
        public String getExtensionFilename() {
            return "test";
        }

        @Override
        public boolean isExtensionBapp() {
            return false;
        }

        @Override
        public void exitSuite(boolean promptUser) { }

        @Override
        public ITempFile saveToTempFile(byte[] buffer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IHttpRequestResponsePersisted saveBuffersToTempFiles(IHttpRequestResponse message) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IHttpRequestResponseWithMarkers applyMarkers(IHttpRequestResponse message, List<int[]> requestMarkers, List<int[]> responseMarkers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getToolName(int toolFlag) {
            return "tool";
        }

        @Override
        public void addScanIssue(IScanIssue issue) { }

        @Override
        public IBurpCollaboratorClientContext createBurpCollaboratorClientContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[][] getParameters(byte[] request) {
            return new String[0][];
        }

        @Override
        public String[] getHeaders(byte[] request) {
            return new String[0];
        }

        @Override
        public void registerMenuItem(String menuTitle, IMenuItemHandler handler) { }
    }

    private static class FakeExtensionHelpers implements IExtensionHelpers {

        @Override
        public IRequestInfo analyzeRequest(IHttpRequestResponse requestResponse) {
            byte[] request = requestResponse.getRequest();
            String requestString = new String(request, StandardCharsets.UTF_8);
            String[] headerBodySplit = requestString.split("\r?\n\r?\n", 2);
            String headerSection = headerBodySplit.length > 0 ? headerBodySplit[0] : "";
            String[] lines = headerSection.split("\r?\n");

            String requestLine = lines.length > 0 ? lines[0] : "";
            String[] requestLineParts = requestLine.split(" ");
            String method = requestLineParts.length > 0 ? requestLineParts[0] : "GET";
            String urlPart = requestLineParts.length > 1 ? requestLineParts[1] : "/";

            URL url;
            try {
                if (urlPart.startsWith("http")) {
                    url = new URL(urlPart);
                } else {
                    IHttpService service = requestResponse.getHttpService();
                    String protocol = service != null ? service.getProtocol() : "http";
                    String host = service != null ? service.getHost() : "example.com";
                    int port = service != null ? service.getPort() : ("https".equals(protocol) ? 443 : 80);
                    url = new URL(protocol, host, port, urlPart);
                }
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }

            List<String> headers = new ArrayList<>();
            headers.addAll(Arrays.asList(lines));

            int bodyOffset = requestString.indexOf("\r\n\r\n");
            if (bodyOffset >= 0) {
                bodyOffset += 4;
            } else {
                bodyOffset = requestString.indexOf("\n\n");
                if (bodyOffset >= 0) {
                    bodyOffset += 2;
                } else {
                    bodyOffset = request.length;
                }
            }

            return new FakeRequestInfo(method, url, headers, bodyOffset);
        }

        @Override
        public IRequestInfo analyzeRequest(IHttpService httpService, byte[] request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRequestInfo analyzeRequest(byte[] request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IResponseInfo analyzeResponse(byte[] response) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IParameter getRequestParameter(byte[] request, String parameterName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String urlDecode(String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String urlEncode(String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] urlDecode(byte[] data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] urlEncode(byte[] data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] base64Decode(String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] base64Decode(byte[] data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String base64Encode(String data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String base64Encode(byte[] data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] stringToBytes(String string) {
            return string.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String bytesToString(byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }

        @Override
        public int indexOf(byte[] data, byte[] pattern, boolean caseSensitive, int start, int end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] buildHttpMessage(List<String> headers, byte[] body) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] buildHttpRequest(URL url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] addParameter(byte[] request, IParameter parameter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] removeParameter(byte[] request, IParameter parameter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] updateParameter(byte[] request, IParameter parameter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] toggleRequestMethod(byte[] request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IHttpService buildHttpService(String host, int port, String protocol) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IHttpService buildHttpService(String host, int port, boolean useHttps) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IParameter buildParameter(String name, String value, byte type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IHttpHeader buildHeader(String name, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IScannerInsertionPoint makeScannerInsertionPoint(String insertionPointName, byte[] baseRequest, int from, int to) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IResponseVariations analyzeResponseVariations(byte[]... responses) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IResponseKeywords analyzeResponseKeywords(List<String> keywords, byte[]... responses) {
            throw new UnsupportedOperationException();
        }
    }

    private static class FakeRequestInfo implements IRequestInfo {
        private final String method;
        private final URL url;
        private final List<String> headers;
        private final int bodyOffset;

        FakeRequestInfo(String method, URL url, List<String> headers, int bodyOffset) {
            this.method = method;
            this.url = url;
            this.headers = headers;
            this.bodyOffset = bodyOffset;
        }

        @Override
        public String getMethod() {
            return method;
        }

        @Override
        public URL getUrl() {
            return url;
        }

        @Override
        public List<String> getHeaders() {
            return headers;
        }

        @Override
        public List<IParameter> getParameters() {
            return Collections.emptyList();
        }

        @Override
        public int getBodyOffset() {
            return bodyOffset;
        }

        @Override
        public byte getContentType() {
            return IRequestInfo.CONTENT_TYPE_JSON;
        }
    }
}
