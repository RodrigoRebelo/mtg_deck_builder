package app.controllers.model;

import app.dtos.Card;
import app.entities.CardEntity;
import app.repositories.CardRepository;
import app.scryfall.ScryfallFacade;
import app.services.CardService;
import app.utils.CsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/cards")
public class CardsModelController {

    @Autowired
    private ScryfallFacade scryfallFacade;

    @Autowired
    private CsvParser csvParser;

    @Autowired
    private CardService cardService;

    @GetMapping("/{name}")
    public ResponseEntity<Card> getCard(@PathVariable String name) {

        try {

            Card card = scryfallFacade.getCard(name);

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(card);
        } catch (Exception e) {

            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{name}/price")
    public ResponseEntity<Optional<BigDecimal>> getCardPrice(@PathVariable String name) {

        Card card = scryfallFacade.getCard(name);

        if (card == null || card.priceEur().isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(card.priceEur());
        }
    }

    @PostMapping("/owned/import")
    public RedirectView importOwnedCards(@RequestParam("file") MultipartFile file, Model model, RedirectAttributes attributes) {

        RedirectView rv = new RedirectView();

        try {
            List<CsvParser.CardCount> cardTotal = csvParser.readCards(file);

            cardService.updateOwnedCards(cardTotal);

        } catch (IOException e) {
            model.addAttribute("error", "Could not read the file.");
        }

        rv.setUrl("/cards/owned");

        return rv;
    }
}
