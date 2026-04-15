package co.za.mrhdigital.wpcbroadsheet.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Converts Railway's DATABASE_URL (postgres://user:pass@host:port/db)
 * to a proper JDBC DataSource for Spring Boot.
 */
@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() throws Exception {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set. " +
                "Please configure it in your Railway Variables tab.");
        }
        URI dbUri = new URI(databaseUrl.replaceFirst("^postgres(?:ql)?://", "http://"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }
}
