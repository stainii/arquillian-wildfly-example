package be.stijnhooft.arquillian.example.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Greeting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String partBeforeName;
    private String partAfterName;

    public Greeting() {
    }

    public Greeting(long id, String partBeforeName, String partAfterName) {
        this(partBeforeName, partAfterName);
        this.id = id;
    }

    public Greeting(String partBeforeName, String partAfterName) {
        this.partBeforeName = partBeforeName;
        this.partAfterName = partAfterName;
    }

    public long getId() {
        return id;
    }

    public String getPartBeforeName() {
        return partBeforeName;
    }

    public void setPartBeforeName(String partBeforeName) {
        this.partBeforeName = partBeforeName;
    }

    public String getPartAfterName() {
        return partAfterName;
    }

    public void setPartAfterName(String partAfterName) {
        this.partAfterName = partAfterName;
    }
}
