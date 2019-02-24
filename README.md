# TuxORM


I built an ORM

![cried 37](https://i.kingtux.me/embed/56LPGk)

## Maven
```xml
   <repository>
      <id>kingtux-repo</id>
      <url>http://repo.kingtux.me/repository/maven-public/</url>
    </repository>
       <dependency>
         <groupId>me.kingtux</groupId>
         <artifactId>tc-common</artifactId>
         <!---Make sure you use Latest Version!-->
         <version>1.0-SNAPSHOT</version>
         <scope>compile</scope>
       </dependency>
```
## Gradle
```
repositories {
  maven { url 'http://repo.kingtux.me/repository/maven-public/' }
}
dependencies {
   compile "me.kingtux:tc-common:1.0"
}
```