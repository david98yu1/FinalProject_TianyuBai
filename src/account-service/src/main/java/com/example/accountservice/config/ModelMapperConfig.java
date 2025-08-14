package com.example.accountservice.config;

import com.example.accountservice.dto.*;
import com.example.accountservice.entity.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();

        mm.createTypeMap(Address.class, AddressDto.class);
        mm.createTypeMap(PaymentMethod.class, PaymentMethodDto.class);
        mm.createTypeMap(Account.class, AccountResponse.class);

        return mm;
    }
}
