package app.controllers.model;

import app.entities.DeckEntity;
import app.enums.DeckFormat;
import app.exceptions.DeckImportException;
import app.records.ParsedDeck;
import app.services.DeckService;
import app.utils.DeckListParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class DecksModelController {

    @Autowired
    private DeckService deckService;

    @Autowired
    private DeckListParser deckListParser;

    @PostMapping("/decks/import")
    public String importDeck(@RequestParam("name") String name,
                             @RequestParam("format") DeckFormat format,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        String deckName = name.trim();

        if (deckName.isEmpty()) {
            return fail(redirectAttributes, "Please enter a deck name.");
        }
        if (deckService.existsByName(deckName)) {
            return fail(redirectAttributes, "A deck named \"" + deckName + "\" already exists.");
        }
        if (file.isEmpty()) {
            return fail(redirectAttributes, "The file is empty.");
        }

        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            ParsedDeck parsed = deckListParser.parse(text);
            DeckEntity deck = deckService.create(deckName, format, parsed);

            redirectAttributes.addFlashAttribute("message",
                    "Added " + deck.getFormat().getLabel() + " deck \"" + deck.getName() + "\": "
                            + deck.getMainboardCount() + " cards"
                            + (deck.getCommanderCount() > 0 ? " + " + deck.getCommanderCount() + " commander" : "")
                            + (deck.getSideboardCount() > 0 ? " + " + deck.getSideboardCount() + " sideboard" : ""));
        } catch (DeckImportException e) {
            return fail(redirectAttributes, e.getMessage());
        } catch (IOException e) {
            return fail(redirectAttributes, "Could not read the file.");
        } catch (DataIntegrityViolationException e) {
            // two requests with the same name at once: the unique constraint caught it
            return fail(redirectAttributes, "A deck named \"" + deckName + "\" already exists.");
        }
        return "redirect:/decks";
    }

    private String fail(RedirectAttributes redirectAttributes, String error) {
        redirectAttributes.addFlashAttribute("error", error);
        return "redirect:/decks";
    }
}
