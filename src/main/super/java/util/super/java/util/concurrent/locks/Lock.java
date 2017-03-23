package java.util.concurrent.locks;

import java.util.concurrent.TimeUnit;

public interface Lock {
    void 	lock();
    void 	lockInterruptibly() throws InterruptedException;
    Condition 	newCondition();
    boolean 	tryLock();
    boolean 	tryLock(long time, TimeUnit unit) throws InterruptedException;
    void 	unlock();
}