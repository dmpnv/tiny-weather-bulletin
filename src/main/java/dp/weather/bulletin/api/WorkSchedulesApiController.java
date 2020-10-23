package dp.weather.bulletin.api;

import dp.weather.bulletin.data.DataStorage;
import dp.weather.bulletin.model.Result;
import dp.weather.bulletin.model.WorkSchedule;
import dp.weather.bulletin.services.EventManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("${openapi.base-path:/v1}")
public class WorkSchedulesApiController implements WorkSchedulesApi {

    private final NativeWebRequest request;
    private DataStorage dataStorage;
    private EventManager eventManager;

    @Autowired
    public WorkSchedulesApiController(NativeWebRequest request, DataStorage dataStorage, EventManager eventManager) {
        this.request = request;
        this.dataStorage = dataStorage;
        this.eventManager = eventManager;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<WorkSchedule> getDefaultWorkSchedule() {
        WorkSchedule workschedule = dataStorage.getDefaultWorkSchedule();
        return workschedule != null
                ? ResponseEntity.ok(workschedule)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Override
    public ResponseEntity<Result> updateDefaultWorkSchedule(@Valid WorkSchedule workSchedule) {
        dataStorage.updateDefaultWorkSchedule(workSchedule);
        eventManager.notifySyncAllRequested();
        Result result = new Result();
        result.setCode("ok");
        return ResponseEntity.ok(result);
    }
}
