package dp.weather.bulletin.data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.locks.ReentrantLock;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "initial-data")
@Scope("singleton")
public class DataStorage {

    private ReentrantLock lock = new ReentrantLock();

}
