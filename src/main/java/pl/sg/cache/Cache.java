package pl.sg.cache;

import lombok.NonNull;

import java.util.Optional;

public interface Cache {

    Optional<String> getValue(@NonNull String key);

    void setValue(@NonNull String key, @NonNull String value);

    void removeValue(@NonNull String key);

    void reset();
}
