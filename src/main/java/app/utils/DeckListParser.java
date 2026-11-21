package app.utils;

import app.exceptions.DeckImportException;
import app.records.DeckLine;
import app.records.ParsedDeck;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DeckListParser {

    // "4 Llanowar Elves" or "4x Llanowar Elves"
    private static final Pattern LINE = Pattern.compile("^(\\d{1,3})x?\\s+(.+)$");
    // Arena exports add " (FDN) 227" after the name
    private static final Pattern SET_SUFFIX = Pattern.compile("\\s+\\([A-Za-z0-9]{2,6}\\)\\s*\\S*$");

    private enum Section { COMMANDER, MAIN, SIDEBOARD }

    public ParsedDeck parse(String text) {
        List<DeckLine> commander = new ArrayList<>();
        List<DeckLine> main = new ArrayList<>();
        List<DeckLine> side = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Section section = Section.MAIN;

        String[] lines = text.replace("\uFEFF", "").split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
                continue;
            }

            String header = line.replaceAll(":$", "").toLowerCase(Locale.ROOT);
            switch (header) {
                case "commander" -> { section = Section.COMMANDER; continue; }
                case "deck", "mainboard", "main" -> { section = Section.MAIN; continue; }
                case "sideboard" -> { section = Section.SIDEBOARD; continue; }
                default -> { }
            }

            Matcher m = LINE.matcher(line);
            int quantity = m.matches() ? Integer.parseInt(m.group(1)) : 0;
            if (quantity <= 0) {
                errors.add("line " + (i + 1) + " (\"" + line + "\")");
                continue;
            }

            DeckLine deckLine = new DeckLine(normalizeName(m.group(2)), quantity);
            switch (section) {
                case COMMANDER -> commander.add(deckLine);
                case MAIN -> main.add(deckLine);
                case SIDEBOARD -> side.add(deckLine);
            }
        }

        if (!errors.isEmpty()) {
            throw new DeckImportException("Could not read " + String.join(", ", errors));
        }
        if (main.isEmpty()) {
            throw new DeckImportException("The deck list has no cards.");
        }
        return new ParsedDeck(commander, main, side);
    }

    private static String normalizeName(String raw) {
        return SET_SUFFIX.matcher(raw.trim()).replaceFirst("")
                .replace('\u2019', '\'')   // curly apostrophes -> straight, as Scryfall writes them
                .replace('\u2018', '\'')
                .replaceAll("\\s+", " ")
                .trim();
    }
}