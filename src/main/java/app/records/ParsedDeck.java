package app.records;

import java.util.List;

public record ParsedDeck(List<DeckLine> commander, List<DeckLine> mainboard, List<DeckLine> sideboard) {

    public int commanderCount() {
        return commander.stream().mapToInt(DeckLine::quantity).sum();
    }

    public int mainboardCount() {
        return mainboard.stream().mapToInt(DeckLine::quantity).sum();
    }

    public int sideboardCount() {
        return sideboard.stream().mapToInt(DeckLine::quantity).sum();
    }
}
