package dp.weather.bulletin.ws;

import com.fasterxml.jackson.databind.Module;
import dp.weather.bulletin.data.DataStorage;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
//@PropertySource("file:/opt/x.properties")
@ComponentScan(basePackages = {"dp.weather.bulletin.client",
							   "dp.weather.bulletin.api",
							   "dp.weather.bulletin.model",
							   "dp.weather.bulletin.data",
							   "dp.weather.bulletin.controller",
							   "dp.weather.bulletin.services",
		                       "dp.weather.bulletin.configuration"})
@EnableConfigurationProperties(DataStorage.class)
public class WeatherBulletinApplication {

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
	public Module jsonNullableModule() {
		return new JsonNullableModule();
	}


}
