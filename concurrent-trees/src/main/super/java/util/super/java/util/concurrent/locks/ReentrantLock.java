package java.util.concurrent.locks;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class ReentrantLock
        extends Object
        implements Lock, Serializable {

    public ReentrantLock() {

    }

    public ReentrantLock(boolean fair) {

    }

    public int getHoldCount() {
        throw new UnsupportedOperationException();

    }
    protected Thread getOwner() {
        throw new UnsupportedOperationException();

    }
    protected Collection<Thread> getQueuedThreads() {
        throw new UnsupportedOperationException();

    }
    int getQueueLength() {
        throw new UnsupportedOperationException();

    }
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        throw new UnsupportedOperationException();

    }
    public int getWaitQueueLength(Condition condition) {
        throw new UnsupportedOperationException();

    }
    public boolean hasQueuedThread(Thread thread) {
        throw new UnsupportedOperationException();

    }
    public boolean hasQueuedThreads() {
        throw new UnsupportedOperationException();

    }
    public boolean hasWaiters(Condition condition) {
        throw new UnsupportedOperationException();

    }
    public boolean isFair() {
        throw new UnsupportedOperationException();

    }
    public boolean isHeldByCurrentThread() {
        throw new UnsupportedOperationException();

    }
    public boolean isLocked() {
        throw new UnsupportedOperationException();

    }
    public void lock() {
        throw new UnsupportedOperationException();

    }
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();

    }
    public Condition newCondition() {
        throw new UnsupportedOperationException();

    }
    public String toString() {
        throw new UnsupportedOperationException();

    }
    public boolean tryLock() {
        throw new UnsupportedOperationException();

    }
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();

    }
    public void unlock() {
        throw new UnsupportedOperationException();

    }
}