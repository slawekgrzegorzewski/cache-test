package pl.sg.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sg.cache.Cache;

@RequestMapping(path = "/cache/", produces = MediaType.TEXT_PLAIN_VALUE)
@RestController
public class CacheResource {

    private final Cache cache;

    public CacheResource(@Qualifier("inMemory") Cache cache) {
        this.cache = cache;
    }

    @GetMapping(path = "/{key}")
    public ResponseEntity<String> getValue(@PathVariable String key) {
        return cache.getValue(key)
                .map((r) -> new ResponseEntity<>(r, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/{key}")
    public ResponseEntity<Void> setValue(@PathVariable String key, @RequestBody String value) {
        cache.setValue(key, value);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(path = "/{key}")
    public ResponseEntity<Void> removeValue(@PathVariable String key) {
        Cache.RemovalStatus removalStatus = cache.removeValue(key);
        return new ResponseEntity<>(removalStatus == Cache.RemovalStatus.REMOVED ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND);
    }
}
