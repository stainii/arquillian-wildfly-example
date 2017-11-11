package be.stijnhooft.arquillian.example.jms;


import be.stijnhooft.arquillian.example.jpa.GreetingService;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.*;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = Queues.REQUEST_QUEUE),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
public class GreetingRequestMDB implements MessageListener {

    @EJB
    private GreetingService greetingService;

    @EJB
    private JMSProducer jmsProducer;

    @Resource(mappedName = Queues.RESPONSE_QUEUE)
    private Queue responseQueue;

    @Override
    public void onMessage(Message message) {
        try {
            String name = ((TextMessage) message).getText();
            String greeting = greetingService.greet(name);
            jmsProducer.putOnResponseQueue(greeting, responseQueue);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
