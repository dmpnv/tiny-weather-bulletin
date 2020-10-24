package dp.weather.bulletin.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

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
        taskQueue.add(ALL_CITIES);
    }

    @Scheduled(initialDelay = 1000L, fixedDelay = 1L)
    public void eventHandler() {
        try {
            processQueue();
        } catch (InterruptedException ignored) {}
    }

    public void processQueue() throws InterruptedException {
        String cityId = taskQueue.take();
        if (!StringUtils.isEmpty(cityId)) {
            if (ALL_CITIES.equals(cityId)) {
                coreServices.syncAll();
                taskQueue.clear();
            }else {
                coreServices.syncCityWeather(cityId);
            }
        }

    }
}
