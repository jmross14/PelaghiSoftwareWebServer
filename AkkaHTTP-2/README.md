# Akka_HTTP
This is a simple project to demonstrate how to use Akka HTTP and Akka Actors to build a rest api. This project demonstrates how to use Slick to access a PostgresSql, as well as, some basic unit testing.

## Getting Started
Clone this project to your local system and use the following commands from the terminal.

Note: These commands were run from Ubuntu 19.10. They may change slightly depending on your system.

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
Next we will create the database
```
$ createdb <Name of your Database>
```
Next we will create the user's password and grant permissions to the database
```
$ psql
$ ALTER USER <your user's name> WITH PASSWORD '<your password>';
$ GRANT ALL PRIVILEGES ON DATABASE <database name> TO <your user's name>;
$ \q
```
Next download the project and change into AkkaHttp-2's directory.
```
$ git clone https://github.com/jmross14/PelaghiSoftwareWebServer.git
$ cd PelaghiSoftwareWebServer/AkkaHTTP-2
```
We will need to change the database information in src/main/resources/application.example. If you don't like vim, use your favorite text editor.
```
vim src/main/resources/application.example
```
```
slick-postgres {
    profile = "slick.jdbc.PostgresProfile$"
    db {
        dataSourceClass = "slick.jdbc.DriverDataSource"
        properties = {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://localhost/<your database name>"
            user = "<your database user name>"
            password = "<your password>"
        }
    }
}
```
Once done with that, rename the file application.conf
```
cp src/main/resources/application.example src/main/resources/application.conf
```
or
```
mv src/main/resources/application.example src/main/resources/application.conf
```
We should be good to run the project at this point.
```
$ mvn compile exec:exec
``` 
## Prerequisites
* OpenJDK 11
* Maven 3.6.3
* PostgreSQL 11.7
