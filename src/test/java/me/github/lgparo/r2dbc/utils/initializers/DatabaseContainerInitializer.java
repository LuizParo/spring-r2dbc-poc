package me.github.lgparo.r2dbc.utils.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer;

import java.util.Map;

import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

public class DatabaseContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String POSTGRES_IMAGE = "postgres:13.2";

    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("r2dbc_demo")
            .withInitScript("scripts/setup_local_db.sql");

    static {
        POSTGRES_CONTAINER.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        final var options = PostgreSQLR2DBCDatabaseContainer.getOptions(POSTGRES_CONTAINER);

        final var url = String.format(
                "r2dbc:%s://%s:%s/%s",
                options.getRequiredValue(DRIVER),
                options.getRequiredValue(HOST),
                options.getRequiredValue(PORT),
                options.getRequiredValue(DATABASE)
        );

        final var properties = new MapPropertySource(
                "DatabaseContainerInitializer",
                Map.of(
                        "spring.r2dbc.url", url,
                        "spring.r2dbc.username", options.getRequiredValue(USER),
                        "spring.r2dbc.password", options.getRequiredValue(PASSWORD)
                )
        );

        applicationContext
                .getEnvironment()
                .getPropertySources()
                .addFirst(properties);
    }
}
