package com.example.EmployeePair.config;

import com.example.EmployeePair.model.PairRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MapConfig {
    @Bean
    public Map<String, ArrayList<PairRecord>> pairs() {
        return new HashMap<>();
    }

    @Bean
    public Map<String, Long> totalDaysOfPair() {
        return new HashMap<>();
    }

}
