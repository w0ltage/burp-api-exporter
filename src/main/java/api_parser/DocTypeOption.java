package api_parser;

public final class DocTypeOption {
    private final String id;
    private final String label;
    private final String defaultExtension;

    public DocTypeOption(String id, String label, String defaultExtension) {
        this.id = id;
        this.label = label;
        this.defaultExtension = defaultExtension == null ? "" : defaultExtension;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }

    @Override
    public String toString() {
        return label;
    }
}
