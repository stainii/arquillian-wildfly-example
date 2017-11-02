package org.arquillian.wildfly.example;

import javax.ejb.Stateless;

@Stateless
public class Greeter {

    public String createGreeting(String name) {
        return "Hello, " + name + "!";
    }
}