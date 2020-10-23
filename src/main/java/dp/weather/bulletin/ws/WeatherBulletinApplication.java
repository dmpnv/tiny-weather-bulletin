package dp.weather.bulletin.ws;

import com.fasterxml.jackson.databind.Module;
import dp.weather.bulletin.data.DataStorage;
import dp.weather.bulletin.data.InitialData;
import dp.weather.bulletin.owm.api.CurrentWeatherDataApi;
import dp.weather.bulletin.owm.client.ApiClient;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
@EnableScheduling
@PropertySource("file:/opt/twb.properties")
@ComponentScan(basePackages = {"dp.weather.bulletin.client",
							   "dp.weather.bulletin.api",
							   "dp.weather.bulletin.model",
							   "dp.weather.bulletin.data",
							   "dp.weather.bulletin.controller",
							   "dp.weather.bulletin.services",
		                       "dp.weather.bulletin.configuration",
		                       "dp.weather.bulletin.owm.client",
		                       "dp.weather.bulletin.owm.api",
		                       "dp.weather.bulletin.owm.client.auth",
		                       "dp.weather.bulletin.owm.model"})
@EnableConfigurationProperties(InitialData.class)
public class WeatherBulletinApplication {

	@Value("${owm.apikey}")
	private String apiKey;

	public static void main(String[] args) {
		SpringApplication.run(WeatherBulletinApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer webConfigurer() {
		return new WebMvcConfigurer() {
            /*@Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("Content-Type");
            }*/
		};
	}

	@Bean
	@Scope("singleton")
	public BlockingQueue<String> taskQueue() {
		return new LinkedBlockingQueue<>();
	}

	@Bean
	public Module jsonNullableModule() {
		return new JsonNullableModule();
	}

	/*@Bean
	public CurrentWeatherDataApi currentWeatherDataApi(ApiClient apiClient) {
		return new CurrentWeatherDataApi(apiClient);
	}*/

	@Bean
	public ApiClient apiClient(RestTemplate restTemplate) {
		ApiClient apiClient = new ApiClient(restTemplate);
		apiClient.setApiKey(apiKey);
		return apiClient;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
