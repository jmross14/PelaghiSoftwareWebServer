# Akka_HTTP
This is a simple project to demonstrate how to use Akka HTTP and Akka Actors to build a rest api. This project demonstrates how to use Slick to access a PostgresSql, as well as, some basic unit testing.

## Getting Started
Clone this project to your local system and use the following Maven command from the terminal in the AkkaHTTP-2 directory.

Note: These commands were run from Ubuntu 19.11. They may change slightly depending on your system.

First we will create the user for the database
```
$ sudo su -u postgres
$ createuser --interactive
```
This will bring you to some prompts to create the user. Here for the simplicity of the project use the same name as the Database you will be using.
```
Output
Enter name of role to add: <Name of Database you will add>
Shall the new role be a superuser? (y/n) y
```
Next we will create the user's password
```
$ psql
$ ALTER USER <your user's name> WITH PASSWORD 'your password';
$ \q
```
Next we will create the database
```
$ createdb <Name of your Database>
```
Next download the project and change into AkkaHttp-2's directory.

```
$ git clone https://github.com/jmross14/PelaghiSoftwareWebServer.git
$ cd PelaghiSoftwareWebServer/AkkaHTTP-2
```
We will need to change the database information in src/main/resources/example.hibernate.cfg.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
    <session-factory>
        <!-- Database connection settings -->
        <!-- Update databaseName, user, and password -->
        <property name="connection.driver_class">org.postgresql.Driver</property>
        <property name="connection.url">jdbc:postgresql://localhost/databaseName</property>
        <property name="connection.username">user</property>
        <property name="connection.password">password</property>
        <property name="show_sql">false</property>

        <property name="hibernate.connection.provider_class">org.hibernate.c3p0.internal.C3P0ConnectionProvider</property>
        <property name="hibernate.c3p0.min_size">5</property>
        <property name="hibernate.c3p0.max_size">20</property>
        <property name="hibernate.c3p0.timeout">120</property>

        <property name="hibernate.hbm2ddl.auto">update</property>

        <!-- If you change the package structure, make sure to update this so it matches your program. -->
        <mapping class="com.pelaghisoftware.data.entity.User" />

    </session-factory>
</hibernate-configuration>
```
Once done with that, rename the file hibernate.cfg.xml

We should be good to run the project at this point.
```
$ mvn compile exec:exec
``` 



## Prerequisites
* OpenJDK 11
* Maven 3.6.3
* PostgreSQL 11.7
