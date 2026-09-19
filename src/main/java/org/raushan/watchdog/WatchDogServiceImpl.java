package org.raushan.watchdog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class WatchDogServiceImpl implements WatchDogService {

    private static final Logger LOGGER  = LoggerFactory.getLogger(WatchDogServiceImpl.class);

    private ScheduledExecutorService scheduledExecutorService;
    private final long deltaTime;
    private final Set<Watchable> Watchable_SET = new HashSet<>();
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();


    private static final Map<Long,WatchDogService> INSTANCES = new HashMap<>();
    private static final Object lock = new Object();

    private WatchDogServiceImpl(long deltaTime) {
        LOGGER.error("delay time can not be less than zero");
        if(deltaTime<=0) throw new IllegalArgumentException("delta time can't be less than 1");
        this.deltaTime = deltaTime;
        init();
    }

    @Override
    public void add(Watchable watchable) {
        Optional.ofNullable(watchable).ifPresent(Watchable_SET::add);
    }

    @Override
    public void remove(Watchable watchable) {
        Optional.ofNullable(watchable).ifPresent(Watchable_SET::remove);
    }

    private void init() {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        this.scheduledExecutorService.scheduleWithFixedDelay(this::schedule,0,deltaTime, TimeUnit.MILLISECONDS);
        LOGGER.info("WatchDog service has been initialized");
    }

    private void schedule() {
        Watchable_SET.stream()
                .parallel()
                .map(Worker::new)
                .forEach(executorService::execute);
    }

    @Override
    public boolean equals(Object obj) {
        if(Objects.isNull(obj)) return false;

        return obj instanceof WatchDogServiceImpl &&
                ((WatchDogServiceImpl) obj).deltaTime == this.deltaTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deltaTime);
    }

    @Override
    public void dispose()  {
        this.scheduledExecutorService.shutdown();
        this.Watchable_SET.clear();
        this.executorService.shutdown();
        INSTANCES.remove(deltaTime,this);
    }

    public static  WatchDogService create(long deltaTime) {
        WatchDogService watchDogService = INSTANCES.get(deltaTime);
        if (watchDogService == null) {
            synchronized (lock) {
                if(Objects.isNull(watchDogService)){
                    watchDogService = new WatchDogServiceImpl(deltaTime);
                    INSTANCES.put(deltaTime, watchDogService);
                }
            }
        }
        return watchDogService;
    }

    private class Worker implements Runnable {
        private final Watchable watchable;
        public Worker(Watchable watchable) {
            this.watchable = watchable;
        }

        @Override
        public void run() {
            try {
                watchable.onWatch();
            } catch (Exception e) {
                LOGGER.error("Error while executing onWatch : {}",e.getMessage());
            }
        }
    }
}