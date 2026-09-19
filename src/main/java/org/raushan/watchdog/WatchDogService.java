package org.raushan.watchdog;

public interface WatchDogService extends Disposable {
    int DEFAULT_DELAY_MS=1000;
    void add(Watchable watchable);
    void remove(Watchable watchable);

    static WatchDogService get() {
        return get(DEFAULT_DELAY_MS);
    }
    static WatchDogService get(int delay) {
        if(delay<=0) {
            throw new IllegalArgumentException("delay should be greater than 0");
        }
        return WatchDogServiceImpl.create(delay);
    }
}
