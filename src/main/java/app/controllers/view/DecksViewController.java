package app.controllers.view;

import app.enums.DeckFormat;
import app.services.DeckService;
import app.utils.DeckListParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DecksViewController {

    @Autowired
    private DeckService deckService;

    @Autowired
    private DeckListParser deckListParser;

    public DecksViewController(DeckService deckService, DeckListParser deckListParser) {
        this.deckService = deckService;
        this.deckListParser = deckListParser;
    }

    @GetMapping("/decks")
    public String decks(Model model) {
        model.addAttribute("decks", deckService.getAllDecks());
        model.addAttribute("formats", DeckFormat.values());
        return "decks";
    }
}
