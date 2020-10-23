package dp.weather.bulletin.data;

import dp.weather.bulletin.model.City;
import dp.weather.bulletin.model.WorkSchedule;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "initial-data")
public class InitialData {

    private List<City> cities;
    private WorkSchedule defaultSchedule;

    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }

    public WorkSchedule getDefaultSchedule() {
        return defaultSchedule;
    }

    public void setDefaultSchedule(WorkSchedule defaultSchedule) {
        this.defaultSchedule = defaultSchedule;
    }
}
