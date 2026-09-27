package ru.asocial.learn.day2.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.CreateCurrencyDto;
import ru.asocial.learn.day2.dto.CurrencyDto;
import ru.asocial.learn.day2.service.CurrencyService;
import tools.jackson.databind.ObjectMapper;

@Component
public class CurrencyCreateConsumer {

    private static Logger log = LoggerFactory.getLogger(CurrencyCreateConsumer.class);

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(groupId = "currency-create-group", topics = "currency-create")
    public void handleCurrencyCreate(ConsumerRecord<String, String> record) {
        String value = record.value();
        CreateCurrencyDto createCurrencyDto =  objectMapper.readValue(value, CreateCurrencyDto.class);
        CurrencyDto dto = currencyService.createCurrency(createCurrencyDto);
        log.info("created currency: " + objectMapper.writeValueAsString(dto));
    }
}
