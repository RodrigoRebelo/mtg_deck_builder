package app.entities;

import app.enums.DeckSection;
import jakarta.persistence.*;

@Entity
@Table(name = "DECK_CARDS",
        uniqueConstraints = @UniqueConstraint(columnNames = {"DECK_ID", "NAME", "SECTION"}))
public class DeckCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DECK_ID", nullable = false)
    private DeckEntity deck;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "SECTION", nullable = false)
    private DeckSection section;

    // Filled in later, when cards are resolved through Scryfall
    @Column(name = "SCRYFALL_ID")
    private String scryfallId;

    protected DeckCardEntity() {
    }

    public DeckCardEntity(DeckEntity deck, String name, int quantity, DeckSection section) {
        this.deck = deck;
        this.name = name;
        this.quantity = quantity;
        this.section = section;
    }

    public Long getId() { return id; }
    public DeckEntity getDeck() { return deck; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public DeckSection getSection() { return section; }
    public String getScryfallId() { return scryfallId; }
    public void setScryfallId(String scryfallId) { this.scryfallId = scryfallId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeckCardEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}