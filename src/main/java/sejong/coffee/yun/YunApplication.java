package sejong.coffee.yun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class YunApplication {

	public static void main(String[] args) {
		SpringApplication.run(YunApplication.class, args);
	}

}
