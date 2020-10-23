package dp.weather.bulletin.data;

import dp.weather.bulletin.model.City;
import dp.weather.bulletin.model.Forecast;
import dp.weather.bulletin.model.ListMetadata;
import dp.weather.bulletin.model.WorkSchedule;
import dp.weather.bulletin.services.EventManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class DataStorage {

    private static final String DEFAULT_SCHEDULE = "*DEFAULT_SCHEDULE*";
    private final ConcurrentHashMap<String, City> cityMap = new ConcurrentHashMap();
    private final ConcurrentHashMap<String, WorkSchedule> scheduleMap = new ConcurrentHashMap();
    private final ConcurrentHashMap<String, Forecast> forecastMap = new ConcurrentHashMap();
    private final Lock lock = new ReentrantLock();

    @Autowired
    public DataStorage(InitialData initialData) {
        Map<String, City> map = Optional.ofNullable(initialData.getCities())
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(City::getId, city -> city));
        cityMap.putAll(map);

        scheduleMap.put(DEFAULT_SCHEDULE, initialData.getDefaultSchedule());
    }

    public ConcurrentHashMap<String, City> getCityMap() {
        return cityMap;
    }

    public ConcurrentHashMap<String, WorkSchedule> getScheduleMap() {
        return scheduleMap;
    }

    public ConcurrentHashMap<String, Forecast> getForecastMap() {
        return forecastMap;
    }

    public Pair<List<City>, ListMetadata> getCityPagedList() {
        List<City> cities = List.copyOf(cityMap.values());
        int size = cities.size();
        ListMetadata metadata = new ListMetadata();
        metadata.setPage(0);
        metadata.setPageCount(1);
        metadata.setPerPage(size);
        metadata.setTotalCount(size);

        return Pair.of(cities, metadata);
    }

    public List<City> getAllCities() {
        return List.copyOf(cityMap.values());
    }

    public City addCity(City city) {
        lock.lock();
        try {
            // silently avoid duplications
            Optional<City> optionalCity = cityMap.values().stream().filter(c -> StringUtils.equalsIgnoreCase(c.getCountry(), city.getCountry())
                    && StringUtils.equalsIgnoreCase(c.getRegion(), city.getRegion())
                    && StringUtils.equalsIgnoreCase(c.getName(), city.getName()))
                    .findFirst();
            if (!optionalCity.isEmpty()) {
                return null;
            }
            String id = cityMap.containsKey(city.getName())
                    ? cityMap.containsKey(city.getName() + "_" + city.getRegion())
                        ? city.getName() + "_" + city.getRegion() + "_" + city.getCountry()
                        : city.getName() + "_" + city.getRegion()
                    : city.getName();
            city.setId(id);
            cityMap.put(id, city);
        } finally {
            lock.unlock();
        }
        return city;
    }

    public City unregisterCity(String cityId) {
        return cityMap.remove(cityId);
    }

    public City getCityById(String cityId) {
        return cityMap.get(cityId);
    }

    public City updateCity(City city) {
        lock.lock();
        try {
            String id = city.getId();
            cityMap.put(id, city);
        } finally {
            lock.unlock();
        }
        return city;
    }

    public WorkSchedule getWorkScheduleByCityId(String cityId) {
        return scheduleMap.get(cityId);
    }

    public void saveForecast(String cityId, Forecast forecast) {
        forecastMap.put(cityId, forecast);
    }

    public WorkSchedule getDefaultWorkSchedule() {
        return scheduleMap.get(DEFAULT_SCHEDULE);
    }

    public void updateDefaultWorkSchedule(WorkSchedule workSchedule) {
        scheduleMap.put(DEFAULT_SCHEDULE, workSchedule);
    }

    public void saveOrUpdateWorkSchedule(WorkSchedule workSchedule, String cityId) {
        scheduleMap.put(cityId, workSchedule);
    }

    public WorkSchedule removeWorkSchedule(String cityId) {
        WorkSchedule workSchedule = scheduleMap.remove(cityId);
        return workSchedule;
    }

    public Forecast getForecastsByCity(String city) {
        return forecastMap.get(city);
    }

    public Pair<List<Forecast>, ListMetadata> getPagedForecastsByCity() {
        List<Forecast> forecastList = List.copyOf(forecastMap.values());
        int size = forecastList.size();
        ListMetadata metadata = new ListMetadata();
        metadata.setPage(0);
        metadata.setPageCount(1);
        metadata.setPerPage(size);
        metadata.setTotalCount(size);

        return Pair.of(forecastList, metadata);
    }

}
