package be.stijnhooft.arquillian.example.jpa;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class GreetingRepository {

    @PersistenceContext
    private EntityManager em;

    public Greeting findGreetingById(long id) {
        return em.find(Greeting.class, id);
    }
}
