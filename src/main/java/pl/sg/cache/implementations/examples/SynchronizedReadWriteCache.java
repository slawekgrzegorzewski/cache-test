package pl.sg.cache.implementations.examples;

import lombok.NonNull;
import pl.sg.cache.Cache;
import pl.sg.cache.CacheChain;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class SynchronizedReadWriteCache extends CacheChain {

    ReadWriteLock lock = new ReentrantReadWriteLock();

    public SynchronizedReadWriteCache() {
        super(null);
    }

    public SynchronizedReadWriteCache(Cache nextHandler) {
        super(nextHandler);
    }

    @Override
    public Optional<String> getValue(@NonNull String key) {
        return runWithWriteLock(() -> super.getValue(key));
    }

    @Override
    public void setValue(@NonNull String key, @NonNull String value) {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            super.setValue(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removeValue(@NonNull String key) {
        invokeWithWriteLock(() -> super.removeValue(key));
    }

    @Override
    public void reset() {
        invokeWithWriteLock(super::reset);
    }

    private <T> T runWithWriteLock(Supplier<T> method) {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            return method.get();
        } finally {
            writeLock.unlock();
        }
    }

    private void invokeWithWriteLock(Runnable method) {
        this.runWithWriteLock(() -> {
            method.run();
            return null;
        });
    }
}
