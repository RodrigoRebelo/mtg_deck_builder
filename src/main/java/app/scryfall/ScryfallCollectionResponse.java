package app.scryfall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScryfallCollectionResponse(
        List<ScryfallCard> data,
        @JsonProperty("not_found") List<Map<String, String>> notFound) {}
