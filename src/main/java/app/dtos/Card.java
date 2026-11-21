package app.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record Card(
        UUID id,
        UUID oracleId,
        String name,
        String manaCost,
        double manaValue,
        String typeLine,
        String oracleText,
        String power,
        String toughness,
        List<String> colors,
        List<String> colorIdentity,
        List<String> keywords,
        Map<String, String> legalities,
        String setCode,
        String setName,
        String collectorNumber,
        String rarity,
        String artist,
        LocalDate releasedAt,
        Map<String, String> imageUris,
        Optional<BigDecimal> priceUsd,
        Optional<BigDecimal> priceEur,
        Optional<BigDecimal> priceTix,
        Optional<Integer> edhrecRank,
        String scryfallUri
) {
    public boolean isLegalIn(String format) {
        return "legal".equals(legalities.get(format));
    }
}
