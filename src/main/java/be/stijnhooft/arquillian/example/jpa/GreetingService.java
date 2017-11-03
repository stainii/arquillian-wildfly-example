package be.stijnhooft.arquillian.example.jpa;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class GreetingService {

    @EJB
    private GreetingRepository greetingRepository;

    public String greet(String name) {
        Greeting greeting = greetingRepository.findGreetingById(1);
        return greeting.getPartBeforeName() + name + greeting.getPartAfterName();
    }
}