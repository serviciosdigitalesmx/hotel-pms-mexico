package com.hotelpms.frontdesk.stays.config;

import com.hotelpms.frontdesk.stays.repository.AlloggiatiComuneRepository;
import com.hotelpms.frontdesk.stays.repository.AlloggiatiStatoRepository;
import com.hotelpms.frontdesk.stays.repository.AlloggiatiTipdocRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AlloggiatiLookupDataLoaderIsolationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(TestRepositoryConfiguration.class);

    @Test
    void loaderIsNotCreatedByDefault() {
        contextRunner.run(context ->
                assertThat(context.getBeansOfType(AlloggiatiLookupDataLoader.class)).isEmpty());
    }

    @Test
    void loaderIsNotCreatedWhenFlagIsFalse() {
        contextRunner
                .withPropertyValues("hotel-pms.alloggiati.enabled=false")
                .run(context ->
                        assertThat(context.getBeansOfType(AlloggiatiLookupDataLoader.class)).isEmpty());
    }

    @Test
    void loaderCanBeExplicitlyEnabledForIsolatedItalianRuntime() {
        contextRunner
                .withPropertyValues("hotel-pms.alloggiati.enabled=true")
                .run(context ->
                        assertThat(context.getBeansOfType(AlloggiatiLookupDataLoader.class)).hasSize(1));
    }

    @Configuration
    @Import(AlloggiatiLookupDataLoader.class)
    static class TestRepositoryConfiguration {

        @Bean
        AlloggiatiStatoRepository statoRepository() {
            return mock(AlloggiatiStatoRepository.class);
        }

        @Bean
        AlloggiatiComuneRepository comuneRepository() {
            return mock(AlloggiatiComuneRepository.class);
        }

        @Bean
        AlloggiatiTipdocRepository tipdocRepository() {
            return mock(AlloggiatiTipdocRepository.class);
        }
    }
}
