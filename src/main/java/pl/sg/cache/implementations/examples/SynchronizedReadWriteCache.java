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

        Lock readLock = lock.readLock();
        try {
            readLock.lock();
            return super.getValue(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void setValue(@NonNull String key, @NonNull String value) {
        runWithWriteLock(() -> super.setValue(key, value));
    }

    @Override
    public RemovalStatus removeValue(@NonNull String key) {
        return invokeWithWriteLock(() -> super.removeValue(key));
    }

    @Override
    public void reset() {
        runWithWriteLock(super::reset);
    }

    private <T> T invokeWithWriteLock(Supplier<T> method) {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            return method.get();
        } finally {
            writeLock.unlock();
        }
    }

    private void runWithWriteLock(Runnable method) {
        invokeWithWriteLock(() -> {
            method.run();
            return null;
        });
    }
}
