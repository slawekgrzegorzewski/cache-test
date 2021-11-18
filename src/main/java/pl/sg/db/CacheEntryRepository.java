package pl.sg.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CacheEntryRepository extends JpaRepository<CacheEntry, String> {
}
