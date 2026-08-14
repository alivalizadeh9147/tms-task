package ir.av.tms.adapters.secondary.persistence.account.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan
@EnableJpaRepositories(
        basePackages = "ir.av.tms"
)
public class PersistenceDataSourceConfig {
}