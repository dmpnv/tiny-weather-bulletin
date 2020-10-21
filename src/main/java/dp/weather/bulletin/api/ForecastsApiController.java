package dp.weather.bulletin.api;

import dp.weather.bulletin.configuration.ApplicationUser;
import dp.weather.bulletin.data.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

@RestController
@RequestMapping("${openapi.base-path:/v1}")
public class ForecastsApiController implements ForecastsApi {

    private final NativeWebRequest request;

    private DataStorage dataStorage;

    @Autowired
    public ForecastsApiController(NativeWebRequest request, DataStorage dataStorage) {
        this.request = request;
        this.dataStorage = dataStorage;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
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
