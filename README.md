# Arquillian WildFly Example
This project is a fork of [tolis-e/arquillian-wildfly-example](https://github.com/tolis-e/arquillian-wildfly-example).
The purpose is to **experiment with the Arquillian test framework**, to see what it can and cannot do.

## Test Execution
Run the tests with Maven:

    mvn test

This will download and start an embedded Wildfly server, on which the tests are deployed and run.

For more information about running on a remote server, check out the [original project](https://github.com/tolis-e/arquillian-wildfly-example).

## The road to learning Arquillian
This project is a fork of [tolis-e/arquillian-wildfly-example](https://github.com/tolis-e/arquillian-wildfly-example).
Tolis-e has created a very simple example test, on which the following test is based.



### Test 1: let's start with a simple test case

> Goal: Inject a very simple EJB in a test. If this works, we have correctly set up Arquillian.

#### The tested object

The bean that we are going to test is the [be.stijnhooft.arquillian.example.simple.Greeter](src/main/java/be/stijnhooft/arquillian/example/simple/Greeter.java) bean.
This is a simple EJB which takes an argument and returns a String.

```java
public String createGreeting(String name) {
    return "Hello, " + name + "!";
}
```

#### What are we going to test
We want to make sure that our parameter is correctly inserted into the returned String.

#### Arquillian configuration

##### Maven dependencies
First, we need the right dependencies. Arquillian has been split up in several modules, so we need to pick what we like.

A good starting point is to add JUnit and the BOM of Arquillian to our dependency management. 
```xml
<dependencyManagement>
    <dependencies>

        <!-- Arquillian BOM (Bill Of Materials). -->
        <dependency>
            <groupId>org.jboss.arquillian</groupId>
            <artifactId>arquillian-bom</artifactId>
            <version>${version.org.jboss.arquillian}</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
        
        <!-- JUnit regression testing framework. -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${version.junit}</version>
            <scope>test</scope>
        </dependency>

    </dependencies>
</dependencyManagement>
```

Arquillian does not automatically integrate in JUnit, unless we add the following dependencies *(right in the root of the pom, not in dependencyManagement)*:

```xml
<dependencies>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- JUnit Container Implementation for the Arquillian Project -->
    <dependency>
        <groupId>org.jboss.arquillian.junit</groupId>
        <artifactId>arquillian-junit-container</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.jboss.arquillian.protocol</groupId>
        <artifactId>arquillian-protocol-servlet</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

We are deploying in an embedded Wildlfy. That means we need to use the following module:

```xml
<dependencies>
     ...
    <dependency>
        <groupId>org.wildfly</groupId>
        <artifactId>wildfly-arquillian-container-managed</artifactId>
        <version>${version.org.wildfly}</version>
        <scope>test</scope>
    </dependency>
    ...
</dependencies>
```

But where will that embedded server come from? With these build settings, Maven will download and unpack Wildfly 8 in the target folder:

```xml
<build>
    <testResources>
        <testResource>
            <directory>src/test/resources</directory>
        </testResource>
    </testResources>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <executions>
                <execution>
                    <id>unpack</id>
                    <phase>process-test-classes</phase>
                    <goals>
                        <goal>unpack</goal>
                    </goals>
                    <configuration>
                        <artifactItems>
                            <artifactItem>
                                <groupId>org.wildfly</groupId>
                                <artifactId>wildfly-dist</artifactId>
                                <version>${version.org.wildfly}</version>
                                <type>zip</type>
                                <overWrite>false</overWrite>
                                <outputDirectory>${project.build.directory}</outputDirectory>
                            </artifactItem>
                        </artifactItems>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Since we are using the @EJB annotation, we need to have this annotation on the class path.
```xml
<dependencies>
    ...
    <dependency>
        <groupId>javax</groupId>
        <artifactId>javaee-api</artifactId>
        <version>7.0</version>
    </dependency>
</dependencies>
```

##### arquillian.xml
Arquillian can't always guess what to do. We will need to tell him certain things. These things can be configured in **arquillian.xml**. This file is placed in *src/test/resources/*.

For our very basic test, we need to tell Arquillian
* that we are using EJB 3
* where our Wildfly instance can be found.

```xml
<arquillian xmlns="http://jboss.org/schema/arquillian" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://jboss.org/schema/arquillian
        http://jboss.org/schema/arquillian/arquillian_1_0.xsd">

    <!-- Sets the protocol which is how Arquillian talks and executes the tests inside the container -->
    <defaultProtocol type="Servlet 3.0" />

    <!-- Configuration to be used when the WildFly managed profile is active -->
    <container qualifier="widlfly-managed" default="true">
        <configuration>
            <property name="jbossHome">${jbossHome:target/wildfly-8.1.0.Final}</property>
        </configuration>
    </container>
</arquillian>
```

#### The test class
##### Arquillian runner
It's time to write the test. First, tell JUnit to wrap Arquillian around it. Annotate the test class with 

```java
@RunWith(Arquillian.class)
public class GreeterTest {
```

##### Creating the deployment
Arquillian expects a method, which **returns an archive that can be deployed on the server**. It's in this archive that the tests will be deployed, so that it can **use the features of a managed environment**.
This method needs to be annotated with `@Deployment`.

What needs to be in this archive?
* All classes that you use
* All dependencies that are used by these classes
* All configuration files that have impact on the classes, like a *persistence.xml* or *beans.xml*

In our case, it's very simple: we only need the Greeter class. Phew.

```java
@Deployment
public static WebArchive createDeployment() {
    return ShrinkWrap
            .create(WebArchive.class)
            .addClass(Greeter.class);
}
```

This method will create a webarchive, or `war`, containing the Greeter class.

##### Write the test
Now that Arquillian knows what to deploy, let's write the test!

And yes... we can already start using features of the managed container, like dependency injection. 

```java
@EJB
private Greeter greeter;

@Test
public void createGreeting() {
    assertEquals("Hello, aliens!", greeter.createGreeting("aliens"));
}
```

[Have a look at the whole test class.](src/test/java/be/stijnhooft/arquillian/example/simple/GreeterTest.java)

##### Run the test
Now, the only thing left to do, is running the test with `mvn test`!



### Test 2: let's add a database
> Goal: Add a persistence unit for an in-memory database. See if we can use JPA, both in our EJB and in our test.

Let's increase the complexity. I want to be able to change the greeting, by persisting and changing the greeting in the database.

In order to do that, I've created 
* a [Greeting entity](https://github.com/stainii/arquillian-wildfly-example/blob/master/src/main/java/be/stijnhooft/arquillian/example/jpa/Greeting.java)
* a [GreetingRepository](https://github.com/stainii/arquillian-wildfly-example/blob/master/src/main/java/be/stijnhooft/arquillian/example/jpa/GreetingRepository.java)
* a [GreetingService](https://github.com/stainii/arquillian-wildfly-example/blob/master/src/main/java/be/stijnhooft/arquillian/example/jpa/GreetingService.java)
* a [persistence.xml configuration file](https://github.com/stainii/arquillian-wildfly-example/blob/master/src/main/resources/META-INF/persistence.xml)

Also, I've added a dependency to an in-memory database, which will start up at deploy time.

```xml
<dependency>
    <groupId>org.hsqldb</groupId>
    <artifactId>hsqldb</artifactId>
    <version>2.4.0</version>
</dependency>
```

#### The deployment archive
This time, we have more to pack in the deployment archive:

* the service, repository and entity
* the persistence.xml
* the hsqldb dependency

In order to retrieve the Maven dependencies, we're going to add a handy library to our dependencies.

```xml
<dependencies>
    ...
    <dependency>
        <groupId>org.jboss.shrinkwrap.resolver</groupId>
        <artifactId>shrinkwrap-resolver-impl-maven</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

In the `@Deployment` method in our test class, we can then write this:

```java
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
``` 

#### Let's test!
Ok, now we're up and ready to test right?

Let's say 
1. we start by persisting a Greeting.
1. Then, we see if the newly persisted greeting comes out of the GreetingService.
1. Finally, we should remove the greeting back from the database, to let the next test start from a clean slate.

Persisting and removing the test data ourselves is a bit foolish: we assume that the database is empty from the start. There are frameworks more suited for inserting and removing test data, but let's be foolish and keep things simple for now.

```java
@EJB
private GreetingService greetingService;

@PersistenceContext
private EntityManager em;

@Test
@Transactional
public void getGreeting() {
    //data set
    Greeting greeting = new Greeting("Hello, ", "!");
    em.persist(greeting);

    try {
        assertEquals("Hello, aliens!", greetingService.greet("aliens"));
    } finally {
        // clean up data set
        em.remove(greeting);
    }
}
```

What do we get when we run the test?

#### No active transaction
> javax.persistence.TransactionRequiredException: JBAS011469: Transaction is required to perform this operation (either use a transaction or extended persistence context)

Hmm... something's up. While I have annotated the test with `@Transactional`, Wildfly still has **not started a transaction in the test**. Without a transaction, we cannot make changes to the database. What now?

A quick Google search tells us to add another Arquillian module as a dependency.

```xml
<dependencyManagement>
    ...
    <!-- extension to start transactions in tests -->
    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-transaction-bom</artifactId>
        <version>1.0.5</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>

<dependencies>
    ...
    <!-- extension to start transactions in tests -->
    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-transaction-jta</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```
And in our *arquillian.xml* we should add:

```xml
<extension qualifier="transaction">
    <property name="manager">java:jboss/UserTransaction</property>
</extension>
```

Running again... no luck again. 

#### Be careful with the @Transactional annotation
After a long search I've found another mistake.

The @Transactional annotation **in the test** should come from `import org.jboss.arquillian.transaction.api.annotation.Transactional`, while the @Trancational annotations in production code come from `javax.transaction.Transactional`;

Knowing this, [the test](src/test/java/be/stijnhooft/arquillian/example/jpa/GreetingServiceTest.java) succeeds!
 
## Supplementary Documentation

* [The original project's README](https://github.com/tolis-e/arquillian-wildfly-example/blob/master/README.md)
* [Arquillian Guides](http://arquillian.org/guides/)
