package app.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvParser {

    public record CardCount(String name, int numberOfCards, String scryfallId, String manaType) {

        // Keeps old call sites compiling while manaType isn't known yet
        public CardCount(String name, int numberOfCards, String scryfallId) {
            this(name, numberOfCards, scryfallId, null);
        }
    }

    public List<CardCount> readCards(MultipartFile file) throws IOException {
        Map<String, CardCount> cards = new LinkedHashMap<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {

            for (CSVRecord record : parser) {
                if (record.size() < 7) {
                    continue;
                }

                String name = record.get(0);

                if (name.isBlank()) {
                    continue;
                }

                int quantity = parseQuantity(record.get(6));

                String scryfallId = record.get(8);

                cards.merge(
                        name,
                        new CardCount(name, quantity, scryfallId),
                        (existing, incoming) -> new CardCount(
                                existing.name(),
                                existing.numberOfCards() + incoming.numberOfCards(), existing.scryfallId())
                );
            }
        }
        return new ArrayList<>(cards.values());
    }

    private int parseQuantity(String value) {
        try {
            int quantity = Integer.parseInt(value.trim());
            return quantity > 0 ? quantity : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}