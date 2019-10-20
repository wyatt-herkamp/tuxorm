# TuxORM
TuxORM is a simple to use orm. That uses a [Dao](https://en.wikipedia.org/wiki/Data_access_object). 
I wrote this with the database tool [TuxJSQL](https://tuxjsql.dev/). I use a lot of magical code. 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.kingtux/tuxorm/badge.svg)](https://mvnrepository.com/artifact/me.kingtux/tuxorm)


### Getting Started

To start using TuxORM you will need to learn how to use
[TuxJSQL](https://tuxjsql.dev/) You can learn how to use that here
[https://tuxjsql.dev/](https://tuxjsql.dev/). After you have learned how
to create a TuxJSQL all you have to do is to 

`TOConnection connection = new TOConnecton(tuxjsql);`

### Creating an Object
```java
import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

@DBTable(name="overallclasses")
public class OverallClass {
    @TableColumn(primary = true, autoIncrement = true)
    private int id;
    @TableColumn
    private String name;
}
``` 
and to get the [Dao](https://en.wikipedia.org/wiki/Data_access_object)
you run is `connection.createDao(OverallClass.class)`

### Supported Datatypes.
All basic Java DataTypes ex. String, int, long, and others.

It also supports Lists, Maps, and Files. Using our BuiltIn Serializers.
You can also add support to more by
[creating your own serializer](https://kingtux.dev/tuxorm/serializers.html)