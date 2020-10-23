package dp.weather.bulletin.services;

import dp.weather.bulletin.model.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

@Service
public class EventManager {

    public static final String ALL_CITIES = "*ALL_CITIES*";

    private BlockingQueue<String> taskQueue;

    @Autowired
    public EventManager(BlockingQueue<String> taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void notifyCityUpdated(String cityId) {
        taskQueue.add(cityId);
    }

    public void notifyDefaultScheduleUpdated() {
        taskQueue.add(ALL_CITIES);
    }

    public void notifySyncAllRequested() {
        taskQueue.add(ALL_CITIES);
    }
}
