package be.stijnhooft.arquillian.example.jms;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.*;
import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class GreetingRequestMDBTest {

    @EJB
    private JMSProducer jmsProducer;

    @Resource(mappedName = Queues.REQUEST_QUEUE)
    private Queue requestQueue;

    @Resource(mappedName = Queues.RESPONSE_QUEUE)
    private Queue responseQueue;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory factory;


    @Deployment
    public static WebArchive createDeployment() {
        File[] mavenDependencies = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .asFile();

        return ShrinkWrap.create(WebArchive.class)
                .addPackage("be.stijnhooft.arquillian.example.jms")
                .addPackage("be.stijnhooft.arquillian.example.jpa")
                .addAsResource("META-INF/persistence.xml")
                .addAsLibraries(mavenDependencies);
    }

    @Test
    @Transactional
    @UsingDataSet("datasets/jpa/greeting.yml")
    public void onMessage() throws Exception {
        jmsProducer.putOnResponseQueue("Stijn", requestQueue);

        try (Connection connection = factory.createConnection()) {
            connection.start();
            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            final MessageConsumer consumer = session.createConsumer(responseQueue);
            final TextMessage message = (TextMessage) consumer.receive(10000L);
            assertEquals("Hello, Stijn!", message.getText());
        }
    }

}