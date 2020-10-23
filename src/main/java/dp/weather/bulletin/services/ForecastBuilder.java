package dp.weather.bulletin.services;

import dp.weather.bulletin.model.*;
import dp.weather.bulletin.model.Period;
import dp.weather.bulletin.owm.model.Onecall;
import dp.weather.bulletin.owm.model.WeatherData;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ForecastBuilder {

    private static final int START_OF_DAY = 6;
    private static final int END_OF_DAY = 22;
    private static final int NOON = 12;

    private City city;
    private WorkSchedule workSchedule;
    private Onecall onecall;

    private TreeMap<ZonedDateTime, WeatherData> weatherMap;

    public ForecastBuilder(City city) {
        this.city = city;
    }

    public ForecastBuilder workSchedule(WorkSchedule workSchedule) {
        this.workSchedule = workSchedule;
        return this;
    }

    public ForecastBuilder onecall(Onecall onecall) {
        this.onecall = onecall;
        return this;
    }

    public Forecast build() {
        assert city != null;
        assert workSchedule != null;
        assert onecall != null;

        Forecast forecast = new Forecast();
        ZoneId zoneId = ZoneId.of(onecall.getTimezone());

        weatherMap = onecall.getHourly().stream()
                            .filter(weatherData -> weatherData.getDt() != null)
                            .collect(
                                Collectors.toMap(weatherData ->
                                        ZonedDateTime.ofInstant(Instant.ofEpochSecond(weatherData.getDt()), zoneId),
                                Function.identity(),
                                (o1, o2) -> o1,
                                TreeMap::new)
                            );
        if (weatherMap == null || weatherMap.isEmpty()) {
            return null;
        }
        ZonedDateTime day = weatherMap.descendingKeySet().last();
        day = day.with(LocalTime.of(0, 1));
        ZonedDateTime upto = weatherMap.descendingKeySet().first();
        upto = upto.with(LocalTime.of(0, 1));
        while(day.compareTo(upto) <= 0)
        {
            forecast.addDailyItem(createDailyForecast(day));
            day = day.plusDays(1);
        }
        forecast.setCity(city.getId());
        forecast.setTimezone(zoneId.getId());
        return forecast;
    }

    private DailyForecast createDailyForecast(ZonedDateTime day) {
        DailyForecast dailyForecast = new DailyForecast();
        dailyForecast.setDate(day.toLocalDate());
        Hours workingHours = getWorkingHours(day.getDayOfWeek());
        Pair<ZonedDateTime, ZonedDateTime> morning = getMorning(day, workingHours);
        Pair<ZonedDateTime, ZonedDateTime> evening = getEvening(day, workingHours);
        dailyForecast.setMorning(calcForecastValues(morning));
        dailyForecast.setEvening(calcForecastValues(evening));
        if (workingHours != null) {
            Pair<ZonedDateTime, ZonedDateTime> worktime = getWorktime(day, workingHours);
            dailyForecast.setWorktime(calcForecastValues(worktime));
        }
        return dailyForecast;
    }

    private ForecastData calcForecastValues(Pair<ZonedDateTime, ZonedDateTime> period) {
        SortedMap<ZonedDateTime, WeatherData> subMap = weatherMap.subMap(period.getLeft(), period.getRight());
        if (subMap.isEmpty()) {
            return null;
        }
        ForecastData result = new ForecastData();
        double average = 0, max = 0, min = 0;
        int humidity = 0;
        int n = 0;
        for (Map.Entry<ZonedDateTime, WeatherData> entry : subMap.entrySet()) {
            WeatherData weatherData = entry.getValue();
            if (n == 0) {
                max = weatherData.getTemp();
                min = weatherData.getTemp();
                average = max;
                humidity = weatherData.getHumidity();
            } else {
                max = Math.max(max, weatherData.getTemp());
                min = Math.min(min, weatherData.getTemp());
                average = (max+min)/2.0;
            }
            n++;
        }
        average = BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP).doubleValue();
        result.setMax(max);
        result.setMin(min);
        result.setAverage(average);
        result.setHumidity(humidity);

        result.setPeriod(createForecastPeriod(period));
        return result;
    }

    private Period createForecastPeriod(Pair<ZonedDateTime, ZonedDateTime> period) {
        Period forecastPeriod = new Period();
        forecastPeriod.setStart(period.getLeft().toLocalTime().toString());
        forecastPeriod.setFinish(period.getRight().toLocalTime().toString());
        return forecastPeriod;
    }

    private Pair<ZonedDateTime, ZonedDateTime> getMorning(ZonedDateTime day, Hours workingHours) {
        if (workingHours != null) {
            ZonedDateTime startOfDay = day.with(LocalTime.of(START_OF_DAY, 0));
            LocalTime workStartLT = LocalTime.parse(workingHours.getStart());
            ZonedDateTime workStart = day.with(workStartLT);
            return Pair.of(startOfDay, workStart);
        } else{
            ZonedDateTime startOfDay = day.with(LocalTime.of(START_OF_DAY, 0));
            ZonedDateTime endOfPeriod = day.with(LocalTime.of(NOON, 0));
            return Pair.of(startOfDay, endOfPeriod);
        }
    }

    private Pair<ZonedDateTime, ZonedDateTime> getEvening(ZonedDateTime day, Hours workingHours) {
        if (workingHours != null) {
            LocalTime workFinishLT = LocalTime.parse(workingHours.getFinish());
            ZonedDateTime workFinish = day.with(workFinishLT);
            ZonedDateTime endOfDay = day.with(LocalTime.of(END_OF_DAY, 0));
            return Pair.of(workFinish, endOfDay);
        } else{
            ZonedDateTime startOfPeriod = day.with(LocalTime.of(NOON, 0));
            ZonedDateTime endOfDay = day.with(LocalTime.of(END_OF_DAY, 0));
            return Pair.of(startOfPeriod, endOfDay);
        }
    }

    private Pair<ZonedDateTime, ZonedDateTime> getWorktime(ZonedDateTime day, Hours workingHours) {
        if (workingHours != null) {
            LocalTime workStartLT = LocalTime.parse(workingHours.getStart());
            ZonedDateTime workStart = day.with(workStartLT);
            LocalTime workFinishLT = LocalTime.parse(workingHours.getFinish());
            ZonedDateTime workFinish = day.with(workFinishLT);
            return Pair.of(workStart, workFinish);
        } else{
            return null;
        }
    }

    private Hours getWorkingHours(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return workSchedule.getMon();
            case TUESDAY:
                return workSchedule.getTue();
            case WEDNESDAY:
                return workSchedule.getWed();
            case THURSDAY:
                return workSchedule.getThu();
            case FRIDAY:
                return workSchedule.getFri();
            case SATURDAY:
                return workSchedule.getSat();
            case SUNDAY:
                return workSchedule.getSun();
            default: return null;
        }
    }
}
