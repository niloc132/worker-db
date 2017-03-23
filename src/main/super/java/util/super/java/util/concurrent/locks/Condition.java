package java.util.concurrent.locks;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public interface Condition {
    void 	await() throws InterruptedException;
    boolean 	await(long time, TimeUnit unit) throws InterruptedException;
    long 	awaitNanos(long nanosTimeout) throws InterruptedException;
    void 	awaitUninterruptibly();
    boolean 	awaitUntil(Date deadline) throws InterruptedException;
    void 	signal();
    void 	signalAll();
}