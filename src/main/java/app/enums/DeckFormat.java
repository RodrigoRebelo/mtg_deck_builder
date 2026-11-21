package app.enums;

public enum DeckFormat {
    STANDARD("Standard"),
    COMMANDER("Commander");

    private final String label;

    DeckFormat(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}