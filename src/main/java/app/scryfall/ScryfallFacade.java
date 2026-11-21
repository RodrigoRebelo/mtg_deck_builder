package app.scryfall;

import app.ApiConstants;
import app.dtos.Card;
import app.mappers.ScryfallCardMapper;
import app.utils.Encoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;


@Service
public class ScryfallFacade {

    private final RestClient restClient = RestClient.builder()
            .defaultHeader(HttpHeaders.USER_AGENT, "MtgApp/1.0")
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .build();

    public Card getCard(String name) {

        URI uri = URI.create(ApiConstants.CARD_GETTER + Encoder.encodeUrl(name));

        try {
            String json = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            return ScryfallCardMapper.fromJson(json);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found: " + name);
        }

    }

}
