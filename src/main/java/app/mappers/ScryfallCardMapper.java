package app.mappers;

import app.dtos.Card;
import app.dtos.ScryfallCardDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public final class ScryfallCardMapper {

    private static final ObjectMapper JSON = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private ScryfallCardMapper() {}

    public static ScryfallCardDto parse(String json) {
        try {
            return JSON.readValue(json, ScryfallCardDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid Scryfall card JSON", e);
        }
    }

    public static Card fromJson(String json) {
        return toDomain(parse(json));
    }

    public static Card toDomain(ScryfallCardDto dto) {
        Map<String, String> prices = orEmpty(dto.prices());

        return new Card(
                parseUuid(dto.id()),
                parseUuid(dto.oracleId()),
                dto.name(),
                dto.manaCost(),
                dto.cmc() == null ? 0.0 : dto.cmc(),
                dto.typeLine(),
                dto.oracleText(),
                dto.power(),
                dto.toughness(),
                orEmpty(dto.colors()),
                orEmpty(dto.colorIdentity()),
                orEmpty(dto.keywords()),
                orEmpty(dto.legalities()),
                dto.set(),
                dto.setName(),
                dto.collectorNumber(),
                dto.rarity(),
                dto.artist(),
                dto.releasedAt() == null ? null : LocalDate.parse(dto.releasedAt()),
                orEmpty(dto.imageUris()),
                price(prices.get("usd")),
                price(prices.get("eur")),
                price(prices.get("tix")),
                Optional.ofNullable(dto.edhrecRank()),
                dto.scryfallUri()
        );
    }

    private static UUID parseUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private static Optional<BigDecimal> price(String value) {
        return value == null || value.isBlank()
                ? Optional.empty()
                : Optional.of(new BigDecimal(value));
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : List.copyOf(list);
    }

    private static <K, V> Map<K, V> orEmpty(Map<K, V> map) {
        if (map == null) return Map.of();

        Map<K, V> copy = new LinkedHashMap<>();
        map.forEach((key, value) -> {
            if (value != null) copy.put(key, value);
        });
        return Collections.unmodifiableMap(copy);
    }
}

