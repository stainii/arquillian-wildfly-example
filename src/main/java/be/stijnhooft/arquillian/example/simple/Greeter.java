package be.stijnhooft.arquillian.example.simple;

import javax.ejb.Stateless;

@Stateless
public class Greeter {

    public String createGreeting(String name) {
        return "Hello, " + name + "!";
    }
}