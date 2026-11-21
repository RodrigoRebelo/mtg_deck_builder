package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScryfallCardDto(
        String id,
        String oracleId,
        String name,
        String lang,
        String releasedAt,
        String layout,
        String manaCost,
        Double cmc,
        String typeLine,
        String oracleText,
        String power,
        String toughness,
        List<String> colors,
        List<String> colorIdentity,
        List<String> keywords,
        Map<String, String> legalities,
        String set,
        String setName,
        String collectorNumber,
        String rarity,
        String artist,
        Map<String, String> imageUris,
        Map<String, String> prices,
        Integer edhrecRank,
        String scryfallUri
) {}
