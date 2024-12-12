package pl.sg.db;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@EqualsAndHashCode
public class CacheEntry {

    @Id
    @Column(name = "entry_key")
    String key;

    @Column(name = "entry_value")
    @Setter
    String value;

    public CacheEntry() {
    }

    public CacheEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
