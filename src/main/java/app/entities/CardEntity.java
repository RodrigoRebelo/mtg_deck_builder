package app.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "CARDS")
public class CardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "NUMBER_OF_CARDS", nullable = false)
    private Long numberOfCards;

    @Column(name = "SCRYFALL_ID", nullable = false)
    private String scryfallId;

    // Card colors using Scryfall's letters: W, U, B, R, G. Multicolor cards combine them
    // (e.g. "RG"), colorless cards are an empty string, and null means not loaded yet.
    @Column(name = "MANA_TYPE")
    private String manaType;

    // Exigido pelo JPA (protected para não ser usado diretamente no código)
    protected CardEntity() {
    }

    public CardEntity(String name, Long numberOfCards, String scryfallId) {
        this(name, numberOfCards, scryfallId, null);
    }

    public CardEntity(String name, Long numberOfCards, String scryfallId, String manaType) {
        this.name = name;
        this.numberOfCards = numberOfCards;
        this.scryfallId = scryfallId;
        this.manaType = manaType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNumberOfCards() {
        return numberOfCards;
    }

    public void setNumberOfCards(Long numberOfCards) {
        this.numberOfCards = numberOfCards;
    }

    public String getScryfallId() {
        return scryfallId;
    }

    public void setScryfallId(String scryfallId) {
        this.scryfallId = scryfallId;
    }

    public String getManaType() {
        return manaType;
    }

    public void setManaType(String manaType) {
        this.manaType = manaType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }

    @Override
    public String toString() {
        return "CardEntity{id=" + id + ", name=" + name + ", numberOfCards=" + numberOfCards
                + ", manaType=" + manaType + "}";
    }
}