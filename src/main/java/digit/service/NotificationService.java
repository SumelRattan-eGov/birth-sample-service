package digit.service;

import digit.config.BTRConfiguration;
import digit.producer.Producer;
import digit.web.models.BirthRegistrationApplication;
import digit.web.models.BirthRegistrationRequest;
import digit.web.models.SMSRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class NotificationService {

    @Autowired
    private Producer producer;

    @Autowired
    private BTRConfiguration config;

    @Autowired
    private RestTemplate restTemplate;

    private static final String smsTemplate = "Dear {NAME}, your birth registration application has been successfully created on the system with application number - {APPNUMBER}.";

    public void prepareEventAndSend(BirthRegistrationRequest request){
        List<SMSRequest> smsRequestList = new ArrayList<>();
        request.getBirthRegistrationApplications().forEach(application -> {
            SMSRequest smsRequest = SMSRequest.builder().mobileNumber(application.getFatherMobileNumber()).message(getCustomMessage(smsTemplate, application)).build();
            smsRequestList.add(smsRequest);
        });
        for (SMSRequest smsRequest : smsRequestList) {
            producer.push(config.getSmsNotificationTopic(), smsRequest);
            log.info("Messages: " + smsRequest.getMessage());
        }
    }

    private String getCustomMessage(String template, BirthRegistrationApplication application) {
        template = template.replace("{APPNUMBER}", application.getApplicationNumber());
        template = template.replace("{NAME}", application.getFather().getName());
        return template;
    }

}