package me.kingtux.tuxorm.exceptions;

public class WeAreFuckedException extends RuntimeException {
    public WeAreFuckedException(String moreDetails){
        super("Fuck: "+moreDetails);
    }
    public WeAreFuckedException(){
        super("Just Fuck!");
    }
}
