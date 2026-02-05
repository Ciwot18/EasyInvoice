package com.kernith.easyinvoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EasyInvoiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyInvoiceApplication.class, args);
    }

}