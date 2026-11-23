package com.futmaneger.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "futmaneger.calendario")
public class CalendarioProperties {

    private int maxEventosPorAvanco = 100;

    public int getMaxEventosPorAvanco() {
        return Math.max(1, maxEventosPorAvanco);
    }

    public void setMaxEventosPorAvanco(int maxEventosPorAvanco) {
        this.maxEventosPorAvanco = maxEventosPorAvanco;
    }
}
