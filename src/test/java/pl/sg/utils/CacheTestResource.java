package pl.sg.utils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sg.cache.Cache;

@RequestMapping(path = "/cache-test/", produces = MediaType.TEXT_PLAIN_VALUE)
@RestController
@Profile("test")
public class CacheTestResource {

    private final Cache cache;

    public CacheTestResource(@Qualifier("inMemory") Cache cache) {
        this.cache = cache;
    }

    @GetMapping(path = "/{key}")
    public ResponseEntity<String> getValue(@PathVariable String key) {
        return cache.getValue(key)
                .map((r) -> new ResponseEntity<>(r, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(path = "/{key}")
    public ResponseEntity setValue(@PathVariable String key, @RequestBody String value) {
        cache.setValue(key, value);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = "/{key}")
    public ResponseEntity removeValue(@PathVariable String key) {
        cache.removeValue(key);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
