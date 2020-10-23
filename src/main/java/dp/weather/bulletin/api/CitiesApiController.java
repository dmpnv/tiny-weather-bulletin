package dp.weather.bulletin.api;

import dp.weather.bulletin.data.DataStorage;
import dp.weather.bulletin.model.*;
import dp.weather.bulletin.services.EventManager;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${openapi.base-path:/v1}")
public class CitiesApiController implements CitiesApi {

    private final NativeWebRequest request;

    private DataStorage dataStorage;
    private EventManager eventManager;

    @Autowired
    public CitiesApiController(NativeWebRequest request, DataStorage dataStorage, EventManager eventManager) {
        this.request = request;
        this.dataStorage = dataStorage;
        this.eventManager = eventManager;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<CityList> getCities() {
        Pair<List<City>, ListMetadata> pair = dataStorage.getCityPagedList();
        CityList result = new CityList();
        result.setCities(pair.getLeft());
        result.setMetadata(pair.getRight());
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> addCity(@Valid City city) {
        Result result = new Result();
        City registeredCity = dataStorage.addCity(city);
        if ( registeredCity != null ) {
            notifyCityUpdated(registeredCity.getId());
            result.setCode("ok");
            result.setEntityId(registeredCity.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }else{
            result.setCode("duplicated");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result);
        }
    }

    @Override
    public ResponseEntity<Result> unregisterCity(String cityId) {
        City removedCity = dataStorage.unregisterCity(cityId);
        if ( removedCity != null ) {
            eventManager.notifySyncAllRequested();
            Result result = new Result();
            result.setCode("ok");
            result.setEntityId(cityId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Override
    public ResponseEntity<WorkSchedule> getWorkScheduleByCity(String cityId) {
        WorkSchedule workschedule = dataStorage.getWorkScheduleByCityId(cityId);
        return workschedule != null
                ? ResponseEntity.ok(workschedule)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Override
    public ResponseEntity<Result> updateWorkSchedule(String cityId, @Valid WorkSchedule workSchedule) {
        dataStorage.saveOrUpdateWorkSchedule(workSchedule, cityId);
        notifyCityUpdated(cityId);
        Result result = new Result();
        result.setCode("ok");
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> removeWorkSchedule(String cityId) {
        WorkSchedule workSchedule = dataStorage.removeWorkSchedule(cityId);
        Result result = new Result();
        if ( workSchedule != null ) {
            notifyCityUpdated(cityId);
            result.setCode("ok");
            result.setEntityId(cityId);
            return ResponseEntity.ok(result);
        }else {
            result.setCode("not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    private void notifyCityUpdated(String cityId) {
        if (dataStorage.getForecastMap().isEmpty()) {
            eventManager.notifySyncAllRequested();
        }else {
            eventManager.notifyCityUpdated(cityId);
        }
    }
}
