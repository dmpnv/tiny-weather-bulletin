package dp.weather.bulletin.services;

import dp.weather.bulletin.model.City;
import dp.weather.bulletin.ws.WeatherBulletinApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = WeatherBulletinApplication.class)
class CoreServicesIntegrationTest {

    @Autowired
    private CoreServices coreServices;

    @Test
    void testCoreServicesSyncAll() {
        coreServices.syncAll();
    }
}
