package app.entities;

import app.enums.DeckFormat;
import app.enums.DeckSection;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DECKS")
public class DeckEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "FORMAT", nullable = false)
    private DeckFormat format;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeckCardEntity> cards = new ArrayList<>();

    protected DeckEntity() {
    }

    public DeckEntity(String name, DeckFormat format) {
        this.name = name;
        this.format = format;
    }

    public void addCard(String cardName, int quantity, DeckSection section) {
        cards.add(new DeckCardEntity(this, cardName, quantity, section));
    }

    public int countOf(DeckSection section) {
        return cards.stream()
                .filter(c -> c.getSection() == section)
                .mapToInt(DeckCardEntity::getQuantity)
                .sum();
    }

    public int getCommanderCount() { return countOf(DeckSection.COMMANDER); }
    public int getMainboardCount() { return countOf(DeckSection.MAINBOARD); }
    public int getSideboardCount() { return countOf(DeckSection.SIDEBOARD); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DeckFormat getFormat() { return format; }
    public List<DeckCardEntity> getCards() { return cards; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeckEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}