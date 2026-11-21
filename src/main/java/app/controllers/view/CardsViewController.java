package app.controllers.view;

import app.entities.CardEntity;
import app.scryfall.ScryfallCard;
import app.scryfall.ScryfallService;
import app.services.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CardsViewController {

    public record ManaTotal(String key, String symbol, String label, long total) {}

    private static final Map<String, String[]> MANA_BUCKETS = new LinkedHashMap<>();
    static {
        //                 key        symbol  label
        MANA_BUCKETS.put("W",       new String[]{"W", "White"});
        MANA_BUCKETS.put("U",       new String[]{"U", "Blue"});
        MANA_BUCKETS.put("B",       new String[]{"B", "Black"});
        MANA_BUCKETS.put("R",       new String[]{"R", "Red"});
        MANA_BUCKETS.put("G",       new String[]{"G", "Green"});
        MANA_BUCKETS.put("C",       new String[]{"C", "Colorless"});
        MANA_BUCKETS.put("UNKNOWN", new String[]{"?", "Unknown"});
    }

    @Autowired
    private CardService cardService;

    @Autowired
    private ScryfallService scryfallService;

    @GetMapping("/cards/owned")
    public String getOwnedCards(Model model) {
        List<CardEntity> cards = cardService.getAllCards();

        Map<String, ScryfallCard> details = scryfallService.getCardsByName(
                cards.stream().map(CardEntity::getName).toList());

        model.addAttribute("cards", cards);
        model.addAttribute("details", details);
        model.addAttribute("manaTotals", manaTotals(cards, details));
        return "owned-cards";
    }

    /** Copies owned per color. A multicolor card counts toward each of its colors. */
    private static List<ManaTotal> manaTotals(List<CardEntity> cards, Map<String, ScryfallCard> details) {
        Map<String, Long> totals = new LinkedHashMap<>();

        for (CardEntity card : cards) {
            for (String key : colorKeys(details.get(card.getName()))) {
                totals.merge(key, card.getNumberOfCards(), Long::sum);
            }
        }

        return MANA_BUCKETS.entrySet().stream()
                .filter(e -> totals.getOrDefault(e.getKey(), 0L) > 0)
                .map(e -> new ManaTotal(e.getKey(), e.getValue()[0], e.getValue()[1], totals.get(e.getKey())))
                .toList();
    }

    private static List<String> colorKeys(ScryfallCard detail) {
        if (detail == null || detail.colorLetters() == null) {
            return List.of("UNKNOWN");
        }
        if (detail.colorLetters().isEmpty()) {
            return List.of("C");
        }
        return new ArrayList<>(detail.colorLetters());
    }
}