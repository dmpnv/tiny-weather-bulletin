package dp.weather.bulletin.data;

import dp.weather.bulletin.model.City;
import dp.weather.bulletin.model.WorkSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DataStorageUnitTest {

    private DataStorage dataStorage;

    @Mock
    private InitialData initialData;

    @BeforeEach
    public void init() {
        WorkSchedule workSchedule = new WorkSchedule();
        Mockito.doReturn(workSchedule).when(initialData).getDefaultSchedule();
        dataStorage = new DataStorage(initialData);
    }

    @Test
    void addCity() {
        // given
        City city = new City();
        city.setName("City");
        city.setRegion("Region");
        city.setCountry("Country");
        // when
        City result = dataStorage.addCity(city);
        // then
        assertEquals(result.getId(), "City");

        //given
        City cityCity = new City();
        cityCity.setName("City");
        cityCity.setRegion("Region");
        cityCity.setCountry("CountryCountry");
        // when
        result = dataStorage.addCity(cityCity);
        // then
        assertEquals(result.getId(), "City_Region");
    }
}
