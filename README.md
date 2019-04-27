# TuxORM
TuxORM is a simple to use orm. That uses a [Dao](https://en.wikipedia.org/wiki/Data_access_object). 
I wrote this with the database tool [TuxJSQL](https://github.com/wherkamp/tuxjsql). I use a lot of magical code. 
##### Current Version `1.0`
# Before use!
Read [this](https://github.com/wherkamp/tuxjsql/wiki/Creating-your-first-TuxJSQL-SQLBuilder)

# How to use 
Read [this](https://github.com/wherkamp/tuxorm/wiki/How-to-use-TuxORM)

[Javadocs](https://docs.kingtux.me/tuxorm/)


## Maven
```xml
   <repository>
      <id>kingtux-repo</id>
      <url>https://repo.kingtux.me/repository/maven-public/</url>
    </repository>
    <dependency>
       <groupId>me.kingtux</groupId>
       <artifactId>tuxorm</artifactId>
       <version>1.0-SNAPSHOT</version>   
    </dependency>
```
## Gradle
```
repositories {
  maven { url 'https://repo.kingtux.me/repository/maven-public/' }
}
dependencies {
   compile "me.kingtux:tuxorm:1.0-SNAPSHOT"
}
```
