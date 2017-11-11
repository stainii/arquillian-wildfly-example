package be.stijnhooft.arquillian.example.jms;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.*;

@Stateless
public class JMSProducer {


    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory factory;

    public void putOnResponseQueue(String message, Queue queue) {

        try (Connection connection = factory.createConnection()) {
            connection.start();
            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            final TextMessage testMessage = session.createTextMessage();
            testMessage.setText(message);
            session.createProducer(queue).send(testMessage);

        } catch (final JMSException e) {
            throw new RuntimeException("[ Error when sending message. ]", e);

        }
    }


}
