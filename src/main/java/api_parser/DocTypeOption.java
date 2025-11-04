package api_parser;

public final class DocTypeOption {
    private final String id;
    private final String label;

    public DocTypeOption(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
