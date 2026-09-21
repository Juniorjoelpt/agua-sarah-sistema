package com.aguasarah.config;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Permite que entidades JPA com relacionamentos LAZY sejam serializadas em JSON
// sem lancar erro de proxy do Hibernate, sem precisar anotar cada campo manualmente.
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate5JakartaModule hibernate5Module() {
        Hibernate5JakartaModule module = new Hibernate5JakartaModule();
        module.enable(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING);
        return module;
    }
}
