package app.scryfall;

import app.entities.CardEntity;
import app.services.CardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Component
public class CardCacheWarmer {

    private static final Logger log = LoggerFactory.getLogger(CardCacheWarmer.class);

    @Autowired
    private final CardService cardService;

    @Autowired
    private final ScryfallService scryfallService;

    public CardCacheWarmer(CardService cardService, ScryfallService scryfallService) {
        this.cardService = cardService;
        this.scryfallService = scryfallService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpOnStartup() {
        // Run in the background so a slow Scryfall doesn't hold up the app
        CompletableFuture.runAsync(() -> {
            try {
                List<String> names = cardService.getAllCards().stream()
                        .map(CardEntity::getName)
                        .toList();

                if (names.isEmpty()) {
                    log.info("No owned cards saved, skipping cache warm-up. It will load on the first upload.");
                    return;
                }

                log.info("Warming card cache with {} cards", names.size());
                Map<String, ScryfallCard> loaded = scryfallService.getCardsByName(names);
                log.info("Card cache ready: {} of {} cards loaded", loaded.size(), names.size());
            } catch (Exception e) {
                // Never let a Scryfall outage stop the app from starting
                log.warn("Card cache warm-up failed, cards will be fetched on demand", e);
            }
        });
    }
}
