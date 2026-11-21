package app.services;

import app.entities.CardEntity;
import app.repositories.CardRepository;
import app.utils.CsvParser.CardCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    public List<CardEntity> updateOwnedCards(List<CardCount> cards) {
        // Collapse duplicates in the incoming list (same card on several CSV lines): quantities are summed
        Map<String, CardCount> incoming = new LinkedHashMap<>();
        Map<String, Long> quantities = new HashMap<>();
        Map<String, String> manaTypes = new HashMap<>();
        for (CardCount c : cards) {
            incoming.putIfAbsent(c.scryfallId(), c);
            quantities.merge(c.scryfallId(), (long) c.numberOfCards(), Long::sum);
            if (c.manaType() != null) {
                manaTypes.putIfAbsent(c.scryfallId(), c.manaType()); // first known value wins
            }
        }

        List<CardEntity> existing = cardRepository.findAll();
        Map<String, CardEntity> existingByScryfallId = existing.stream()
                .collect(Collectors.toMap(CardEntity::getScryfallId, Function.identity()));

        // Remove cards in the database that aren't in the list
        List<CardEntity> toDelete = existing.stream()
                .filter(e -> !incoming.containsKey(e.getScryfallId()))
                .toList();
        cardRepository.deleteAll(toDelete);

        // Add new cards and update changed ones
        List<CardEntity> toSave = new ArrayList<>();
        for (Map.Entry<String, CardCount> entry : incoming.entrySet()) {
            String scryfallId = entry.getKey();
            CardCount c = entry.getValue();
            Long quantity = quantities.get(scryfallId);
            String manaType = manaTypes.get(scryfallId); // null when the caller didn't provide one

            CardEntity entity = existingByScryfallId.get(scryfallId);
            if (entity == null) {
                toSave.add(new CardEntity(c.name(), quantity, scryfallId, manaType));
                continue;
            }

            boolean changed = false;

            if (!entity.getNumberOfCards().equals(quantity)) {
                entity.setNumberOfCards(quantity);
                changed = true;
            }
            if (!entity.getName().equals(c.name())) {
                entity.setName(c.name());
                changed = true;
            }
            // Only overwrite when we actually received a value, so a CSV without colors
            // doesn't erase manaType data that is already stored
            if (manaType != null && !manaType.equals(entity.getManaType())) {
                entity.setManaType(manaType);
                changed = true;
            }

            if (changed) {
                toSave.add(entity);
            }
        }
        cardRepository.saveAll(toSave);

        // Return the final state of the table
        return cardRepository.findAll();
    }

    public List<CardEntity> getAllCards() {

        return cardRepository.findAll();
    }
}