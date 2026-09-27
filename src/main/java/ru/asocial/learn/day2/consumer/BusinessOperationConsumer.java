package ru.asocial.learn.day2.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.BusinessOperationDTO;
import ru.asocial.learn.day2.dto.CreateBusinessOperationDTO;
import ru.asocial.learn.day2.service.BusinessOperationService;
import tools.jackson.databind.ObjectMapper;

@Component
public class BusinessOperationConsumer {

    private static Logger log = LoggerFactory.getLogger(BusinessOperationConsumer.class);

    @Autowired
    private BusinessOperationService businessOperationService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(groupId = "business-operation-group", topics = "business-operation")
    public void handleMessage(ConsumerRecord<String, String> record) {
        String value = record.value();
        CreateBusinessOperationDTO businessOperationDTO =  objectMapper.readValue(value, CreateBusinessOperationDTO.class);
        businessOperationService.createAndProcess(businessOperationDTO);
        log.info("processed " + objectMapper.writeValueAsString(businessOperationDTO));
    }

}
