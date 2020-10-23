package dp.weather.bulletin.api;

import dp.weather.bulletin.configuration.ApplicationUser;
import dp.weather.bulletin.data.DataStorage;
import dp.weather.bulletin.model.Forecast;
import dp.weather.bulletin.model.ForecastsList;
import dp.weather.bulletin.model.ListMetadata;
import dp.weather.bulletin.services.CoreServices;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${openapi.base-path:/v1}")
public class ForecastsApiController implements ForecastsApi {

    private final NativeWebRequest request;

    private DataStorage dataStorage;
    private CoreServices coreServices;

    @Autowired
    public ForecastsApiController(NativeWebRequest request, DataStorage dataStorage, CoreServices coreServices) {
        this.request = request;
        this.dataStorage = dataStorage;
        this.coreServices = coreServices;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<ForecastsList> getForecasts() {
        Pair<List<Forecast>, ListMetadata> pair = dataStorage.getPagedForecastsByCity();
        if (CollectionUtils.isEmpty(pair.getLeft())) {
            // sync all
            coreServices.syncAll();
        }
        pair = dataStorage.getPagedForecastsByCity();

        if (CollectionUtils.isEmpty(pair.getLeft())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            ForecastsList forecastsList = new ForecastsList();
            forecastsList.setForecasts(pair.getLeft());
            forecastsList.setMetadata(pair.getRight());
            return ResponseEntity.ok(forecastsList);
        }
    }

    @Override
    public ResponseEntity<Forecast> getForecastsByCity(String city) {
        Forecast forecasts = dataStorage.getForecastsByCity(city);
        if (forecasts == null) {
            Pair<List<Forecast>, ListMetadata> pair = dataStorage.getPagedForecastsByCity();
            if (CollectionUtils.isEmpty(pair.getLeft())) {
                // sync all
                coreServices.syncAll();
            } else {
                coreServices.syncCityWeather(city);
            }
        }
        forecasts = dataStorage.getForecastsByCity(city);
        return (forecasts != null)
                ? ResponseEntity.ok(forecasts)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    private String getUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof ApplicationUser) {
            return ((ApplicationUser) principal).getUsername();
        }if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }else{
            return principal.toString();
        }

    }
}
