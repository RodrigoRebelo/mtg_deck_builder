package app.services;

import app.entities.DeckEntity;
import app.enums.DeckFormat;
import app.enums.DeckSection;
import app.records.DeckLine;
import app.records.ParsedDeck;
import app.repositories.DeckRepository;
import app.utils.CsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeckService {

    private final DeckRepository deckRepository;

    public DeckService(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @Transactional(readOnly = true)
    public List<DeckEntity> getAllDecks() {
        return deckRepository.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return deckRepository.existsByName(name);
    }

    @Transactional
    public DeckEntity create(String name, DeckFormat format, ParsedDeck parsed) {
        DeckEntity deck = new DeckEntity(name, format);
        addSection(deck, parsed.commander(), DeckSection.COMMANDER);
        addSection(deck, parsed.mainboard(), DeckSection.MAINBOARD);
        addSection(deck, parsed.sideboard(), DeckSection.SIDEBOARD);
        return deckRepository.save(deck); // cascade saves the DECK_CARDS rows
    }

    private void addSection(DeckEntity deck, List<DeckLine> lines, DeckSection section) {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (DeckLine line : lines) {
            quantities.merge(line.name(), line.quantity(), Integer::sum);
        }
        quantities.forEach((cardName, qty) -> deck.addCard(cardName, qty, section));
    }
}
