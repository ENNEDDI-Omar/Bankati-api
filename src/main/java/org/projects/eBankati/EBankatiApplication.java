package org.projects.eBankati;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EBankatiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EBankatiApplication.class, args);
    }

}
