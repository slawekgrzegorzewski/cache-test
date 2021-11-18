package pl.sg.cache;

import lombok.NonNull;

import java.util.Optional;

public abstract class CacheChain implements Cache {

    private final Cache nextHandler;

    public CacheChain() {
        this(null);
    }

    public CacheChain(Cache nextHandler) {
        this.nextHandler = nextHandler;
    }

    public Optional<String> getValue(@NonNull String key) {
        return Optional.ofNullable(nextHandler).flatMap(nh -> nh.getValue(key));
    }

    public void setValue(@NonNull String key, @NonNull String value) {
        Optional.ofNullable(nextHandler).ifPresent(nh -> nh.setValue(key, value));
    }

    @Override
    public void removeValue(@NonNull String key) {
        Optional.ofNullable(nextHandler).ifPresent(nh -> nh.removeValue(key));
    }

    @Override
    public void reset() {
        Optional.ofNullable(nextHandler).ifPresent(Cache::reset);
    }
}
