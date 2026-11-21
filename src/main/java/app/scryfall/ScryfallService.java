package app.scryfall;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;

@Service
public class ScryfallService {

    private static final int MAX_BATCH = 75;

    private final RestClient scryfall = RestClient.builder()
            .baseUrl("https://api.scryfall.com")
            .defaultHeader("User-Agent", "MyCardApp/1.0")
            .defaultHeader("Accept", "application/json")
            .build();

    private final Cache<String, ScryfallCard> cache = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterWrite(Duration.ofHours(24))
            .build();

    /** Returns the cards found, keyed by the name you passed in. Missing names are absent from the map. */
    public Map<String, ScryfallCard> getCardsByName(Collection<String> names) {
        Map<String, ScryfallCard> result = new HashMap<>();
        List<String> toFetch = new ArrayList<>();

        // 1. Serve what we can from the cache
        for (String name : new LinkedHashSet<>(names)) {
            ScryfallCard cached = cache.getIfPresent(key(name));
            if (cached != null) {
                result.put(name, cached);
            } else {
                toFetch.add(name);
            }
        }

        // 2. Fetch only the misses, in batches of 75
        for (int i = 0; i < toFetch.size(); i += MAX_BATCH) {
            List<String> batch = toFetch.subList(i, Math.min(i + MAX_BATCH, toFetch.size()));

            ScryfallCollectionResponse response = scryfall.post()
                    .uri("/cards/collection")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("identifiers", batch.stream().map(n -> Map.of("name", n)).toList()))
                    .retrieve()
                    .body(ScryfallCollectionResponse.class);

            for (ScryfallCard card : response.data()) {
                cache.put(key(card.name()), card);
                cache.put(key(frontFace(card.name())), card); // so "Delver of Secrets" also hits
            }

            pause();
        }

        // 3. Fill the result for the names we just fetched
        for (String name : toFetch) {
            ScryfallCard card = cache.getIfPresent(key(name));
            if (card != null) {
                result.put(name, card);
            }
        }
        return result;
    }

    private static String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private static String frontFace(String name) {
        int idx = name.indexOf(" // ");
        return idx > 0 ? name.substring(0, idx) : name;
    }

    private static void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
