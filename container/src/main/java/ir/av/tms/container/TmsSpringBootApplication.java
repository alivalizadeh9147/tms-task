package ir.av.tms.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "ir.av.tms")
@ComponentScan(basePackages = "ir.av.tms")
@EntityScan(basePackages = "ir.av.tms")
public class TmsSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmsSpringBootApplication.class, args);
    }
}
