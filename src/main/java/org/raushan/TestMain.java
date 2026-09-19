package org.raushan;

import org.raushan.watchdog.WatchDogService;
import org.raushan.watchdog.Watchable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class TestMain {
    private final static Logger logger = LoggerFactory.getLogger(TestMain.class);
     static void main()  {
         WatchDogService watchDogService = WatchDogService.get();
        try  {

            TestWatcher watchable1 = new TestWatcher("First");
            TestWatcher watchable2 = new TestWatcher("Second");
            TestWatcher watchable3 = new TestWatcher("Third");

            watchDogService.add(watchable1);
            watchDogService.add(watchable2);

            Thread.sleep(20000);

            watchDogService.add(watchable3);

            Thread.sleep(20000);

            watchDogService.remove(watchable1);
            watchDogService.remove(watchable2);

            Thread.sleep(20000);

        } catch (Exception e) {

            logger.error("Error: {}",e.getMessage());
        } finally {
            logger.info("WatchDogService closed");
            watchDogService.dispose();
        }
    }
}

class TestWatcher implements Watchable {
    private final static Logger LOGGER = LoggerFactory.getLogger(TestWatcher.class);
    private final String name;
    private final AtomicInteger counter = new AtomicInteger(0);
    TestWatcher(String name) {
        this.name = name;
    }

    @Override
    public void onWatch() {
        LOGGER.info("OnWatch for {} and counter is {}", name, counter.incrementAndGet());
    }
}
