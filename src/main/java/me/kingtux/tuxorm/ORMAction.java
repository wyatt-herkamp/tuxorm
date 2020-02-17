package me.kingtux.tuxorm;

import dev.tuxjsql.core.TuxJSQL;
import dev.tuxjsql.core.response.DBAction;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class ORMAction<T> {
    private Callable task;
    private TuxORM tuxORM;

    public T complete() throws InterruptedException {
        return null;
    }

    public T completeHere() {
        try {
            return (T) task.call();
        } catch (Exception e) {
            TuxORM.LOGGER.error("Unable to complete Callable");
        }
        return null;
    }


    public T complete(long time, TimeUnit unit) throws TimeoutException, InterruptedException {
        return null;
    }

    public void queue() {
    }

    public void queue(Consumer<T> handler) {
    }


}
