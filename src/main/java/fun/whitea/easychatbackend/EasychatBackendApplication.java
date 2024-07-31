package fun.whitea.easychatbackend;

import jdk.jfr.Enabled;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("fun.whitea.easychatbackend.mapper")
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class EasychatBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasychatBackendApplication.class, args);
	}


}
