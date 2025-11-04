package api_parser.docType;

import com.google.gson.JsonObject;

public class OpenApi31DocType extends BaseOpenApiDocType {

    @Override
    protected String getOpenApiVersion() {
        return "3.1.0";
    }

    @Override
    protected void customizeRootDocument(JsonObject document) {
        document.addProperty("jsonSchemaDialect", "https://json-schema.org/draft/2020-12/schema");
    }
}

