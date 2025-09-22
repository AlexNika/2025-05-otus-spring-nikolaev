package ru.otus.hw.gateway;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.model.BigTireSUV;
import ru.otus.hw.model.SUV;

@MessagingGateway
public interface TuningGateway {

    @Gateway(requestChannel = "tuningInputChannel")
    BigTireSUV tuneSUV(SUV suv);
}