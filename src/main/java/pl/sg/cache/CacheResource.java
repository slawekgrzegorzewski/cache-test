package pl.sg.cache;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sg.cache.service.CacheRequestHandler;

@RequestMapping(path = "/cache/", produces = MediaType.TEXT_PLAIN_VALUE)
@RestController
public class CacheResource {

    private final CacheRequestHandler cache;

    public CacheResource(CacheRequestHandler cache) {
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
        //TODO validate user input?
        cache.setValue(key, value);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
