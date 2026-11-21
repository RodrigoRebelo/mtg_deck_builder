package app.scryfall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScryfallCard(
        String id,
        String name,
        @JsonProperty("type_line") String typeLine,
        @JsonProperty("mana_cost") String manaCost,
        @JsonProperty("oracle_text") String oracleText,
        @JsonProperty("image_uris") Map<String, String> imageUris,
        @JsonProperty("card_faces") List<CardFace> cardFaces,
        @JsonProperty("colors") List<String> colors,
        Map<String, String> prices) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CardFace(
            String name,
            @JsonProperty("mana_cost") String manaCost,
            @JsonProperty("type_line") String typeLine,
            @JsonProperty("oracle_text") String oracleText,
            @JsonProperty("image_uris") Map<String, String> imageUris,
            @JsonProperty("colors") List<String> colors) {}

    /** One symbol of a mana cost. type: W, U, B, R, G, C, N (generic/X), H (hybrid), SEP (the "//" between faces). */
    public record ManaSymbol(String text, String type) {

        /** Scryfall's official symbol image: {R} -> R.svg, {W/U} -> WU.svg, {2} -> 2.svg */
        public String svgUrl() {
            return "https://svgs.scryfall.io/card-symbols/" + text.replace("/", "") + ".svg";
        }
    }

    private static final Pattern SYMBOL = Pattern.compile("\\{([^}]+)}");

    /** The normal-size image, falling back to the front face for double-faced cards. */
    public String imageUrl() {
        if (imageUris != null) {
            return imageUris.get("normal");
        }
        if (cardFaces != null && !cardFaces.isEmpty() && cardFaces.get(0).imageUris() != null) {
            return cardFaces.get(0).imageUris().get("normal");
        }
        return null;
    }

    /** Colors as letters (W, U, B, R, G). Empty list = colorless. Null = no color data. */
    public List<String> colorLetters() {
        if (colors != null) {
            return colors;
        }
        if (cardFaces != null && !cardFaces.isEmpty()) {
            return cardFaces.get(0).colors(); // double-faced cards: front face
        }
        return null;
    }

    /** "R", "RG", ... or "" for colorless. Null if Scryfall gave no color data. */
    public String manaType() {
        List<String> letters = colorLetters();
        return letters == null ? null : String.join("", letters);
    }

    /** The mana cost split into symbols, ready to draw. Empty for lands and other costless cards. */
    public List<ManaSymbol> manaSymbols() {
        List<ManaSymbol> result = new ArrayList<>();

        if (manaCost != null && !manaCost.isEmpty()) {
            addSymbols(manaCost, result);
        } else if (cardFaces != null) {
            // Split, adventure and double-faced cards keep the cost on each face
            for (CardFace face : cardFaces) {
                if (face.manaCost() == null || face.manaCost().isEmpty()) {
                    continue;
                }
                if (!result.isEmpty()) {
                    result.add(new ManaSymbol("//", "SEP"));
                }
                addSymbols(face.manaCost(), result);
            }
        }
        return result;
    }

    private static void addSymbols(String cost, List<ManaSymbol> out) {
        Matcher m = SYMBOL.matcher(cost);
        while (m.find()) {
            String s = m.group(1);
            out.add(new ManaSymbol(s, classify(s)));
        }
    }

    private static String classify(String s) {
        if (s.matches("[WUBRG]")) return s;
        if (s.equals("C")) return "C";
        if (s.matches("\\d+|X|Y|Z")) return "N";
        return "H"; // hybrid, Phyrexian, snow...
    }
}