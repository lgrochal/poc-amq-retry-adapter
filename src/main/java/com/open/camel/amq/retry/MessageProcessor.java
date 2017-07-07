package com.open.camel.amq.retry;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component("messageProcessor")
public class MessageProcessor implements Processor {

    private volatile int counter;

    @Override
    public void process(Exchange exchange) throws Exception {
        // use a processor to simulate error in the first 2 calls
        if (counter++ < 2) {
            throw new IOException("Forced");
        }
        exchange.getIn().setBody("Bye World");
    }
}
