package dp.weather.bulletin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import static dp.weather.bulletin.services.EventManager.ALL_CITIES;

@Component
public class CoreScheduler {

    private BlockingQueue<String> taskQueue;

    private CoreServices coreServices;

    @Autowired
    public CoreScheduler(BlockingQueue<String> taskQueue, CoreServices coreServices) {
        this.taskQueue = taskQueue;
        this.coreServices = coreServices;
    }

    @Scheduled(initialDelay = 10000L, fixedRate = 60*60*1000L)
    public void hourly() {
        coreServices.syncAll();
    }

    @Scheduled(initialDelay = 0L, fixedRate = 60*1000L)
    public void minutely() {
        try {
            processQueue();
        } catch (InterruptedException ignored) {}
    }

    public void processQueue() throws InterruptedException {
        String cityId;

        while((cityId = taskQueue.poll(59L, TimeUnit.SECONDS)) != null) {
            if (ALL_CITIES.equals(cityId)) {
                taskQueue.clear();
                coreServices.syncAll();
            }else {
                coreServices.syncCityWeather(cityId);
            }
        }

    }
}
