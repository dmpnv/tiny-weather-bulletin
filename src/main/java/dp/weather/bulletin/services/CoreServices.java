package dp.weather.bulletin.services;

import dp.weather.bulletin.data.DataStorage;
import dp.weather.bulletin.model.City;
import dp.weather.bulletin.model.Forecast;
import dp.weather.bulletin.model.WorkSchedule;
import dp.weather.bulletin.owm.api.CurrentWeatherDataApi;
import dp.weather.bulletin.owm.model.Onecall;
import dp.weather.bulletin.owm.model.Weather;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Service
public class CoreServices {

    private CurrentWeatherDataApi currentWeatherDataApi;

    private DataStorage dataStorage;

    @Autowired
    public CoreServices(CurrentWeatherDataApi currentWeatherDataApi, DataStorage dataStorage) {
        this.currentWeatherDataApi = currentWeatherDataApi;
        this.dataStorage = dataStorage;
    }

    public City syncCityCoord(City city) {
        if (StringUtils.isEmpty(city.getLat()) || StringUtils.isEmpty(city.getLon())) {
            String q = city.getName() +
                    (StringUtils.isEmpty(city.getCountry()) ? "" : "," + city.getCountry());
            Weather weather = currentWeatherDataApi.currentWeatherData(q);
            city.setLon(weather.getCoord().getLon());
            city.setLat(weather.getCoord().getLat());
            city.setTimezone(weather.getTimezone());
            return dataStorage.updateCity(city);
        }

        return city;
    }

    public void syncAll() {
        Flux<City> cityFlux = Flux.fromStream(dataStorage.getAllCities().stream());
        syncCityWeather(cityFlux).blockLast();
    }

    public void syncCityWeather(String cityId) {
        City city = dataStorage.getCityById(cityId);
        if (city == null) {
            return;
        }
        syncCityWeather(Flux.just(city)).blockLast();
    }

        public Flux<?> syncCityWeather(Flux<City> cityFlux) {
        return cityFlux.map(city -> syncCityCoord(city))
                .doOnNext(city -> {
                    Onecall onecall =
                            currentWeatherDataApi.onecallGet(city.getLon(),
                                                             city.getLat(),
                                                            "minutely,daily,alerts",
                                                            "metric");
                    WorkSchedule workSchedule = Optional.ofNullable(dataStorage.getWorkScheduleByCityId(city.getId()))
                                                        .orElse(dataStorage.getDefaultWorkSchedule());
                    Forecast forecast = new ForecastBuilder(city).workSchedule(workSchedule)
                            .onecall(onecall)
                            .build();
                    dataStorage.saveForecast(city.getId(), forecast);
                });
    }


}
