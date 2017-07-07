package com.open.camel.amq.retry;

import org.apache.activemq.ScheduledMessage;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CamelAmqRouteBuilder extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CamelAmqRouteBuilder.class);

    private final static String DIRECT_DESTINATION = "direct:newMessage";
   /* @Bean
    public ServletRegistrationBean camelServlet() {
        // use a @Bean to register the Camel servlet which we need to do
        // because we want to use the camel-servlet component for the Camel REST service
        ServletRegistrationBean mapping = new ServletRegistrationBean();
        mapping.setName("CamelServlet");
        mapping.setLoadOnStartup(1);
        // CamelHttpTransportServlet is the name of the Camel servlet to use
        mapping.setServlet(new CamelHttpTransportServlet());
        mapping.addUrlMappings("/message*//*");
        return mapping;
    }*/

    @Override
    public void configure() throws Exception {

        restConfiguration()
                .apiContextPath("/api-doc")
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .apiProperty("api.title", "Camel REST API")
                .apiProperty("api.version", "1.0")
                .apiProperty("cors", "true")
                .apiContextRouteId("doc-api");

        logger.info("Configure REST API");

        rest("/message").description("Message service")
                .consumes("application/json")
                .produces("application/json")
                .post()
                .type(BaseMessage.class)
                .description("Creates new message")
                .responseMessage().responseModel(BaseMessage.class).code(200)
                .endResponseMessage()
                .route().routeId("post-message")
                .to(DIRECT_DESTINATION).endRest();

        from(DIRECT_DESTINATION)
                .toD("activemq:camel.amq.queue?exchangePattern=InOnly");

        from("activemq:camel.amq.queue")
                .process("messageProcessor")
                .onException(IOException.class)
                .handled(true)
                .bean(MessageRetryTagger.class)
                .choice()
                    .when(header(MessageRetryTagger.ATTEMPT_HEADER).isLessThan(5))
                        .setHeader(ScheduledMessage.AMQ_SCHEDULED_DELAY, constant("5000"))
                        .setHeader("scheduleJobId", constant(null))
                        .to("activemq:activemq:camel.amq.queue")
                    .otherwise()
                        .removeHeader(ScheduledMessage.AMQ_SCHEDULED_DELAY)
                        .to("activemq:camel.amq.queue.dead")
                .to("log:sample");
    }

}
