package be.stijnhooft.arquillian.example.jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.DataSource;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class GreetingServiceWithDBUnitTest {

    @Deployment
    public static WebArchive createDeployment() {
        File[] mavenDependencies = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .asFile();

        return ShrinkWrap.create(WebArchive.class)
                .addClass(Greeting.class)
                .addClass(GreetingService.class)
                .addClass(GreetingRepository.class)
                .addAsResource("META-INF/persistence.xml")
                .addAsLibraries(mavenDependencies);
    }

    @EJB
    private GreetingService greetingService;

    @Test
    @Transactional
    @UsingDataSet("datasets/jpa/greeting.yml")
    @DataSource("java:jboss/datasources/arquillianTest")
    public void getGreeting() {
        assertEquals("Hello, aliens!", greetingService.greet("aliens"));
    }
}