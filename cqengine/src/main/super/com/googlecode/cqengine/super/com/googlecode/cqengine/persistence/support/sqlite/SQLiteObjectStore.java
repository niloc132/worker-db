package com.googlecode.cqengine.persistence.support.sqlite;

import com.googlecode.cqengine.index.sqlite.SQLiteIdentityIndex;
import com.googlecode.cqengine.index.support.CloseableIterator;
import com.googlecode.cqengine.persistence.support.ObjectStore;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Collection;

/*
No-op impl, so that CQE doesn't need to be edited or super sourced.
 */
public class SQLiteObjectStore<O, A extends Comparable<A>> implements ObjectStore<O> {

    public SQLiteIdentityIndex<A, O> getBackingIndex() {
        return null;
    }

    public int size(QueryOptions queryOptions) {
        return 0;
    }

    public boolean contains(Object o, QueryOptions queryOptions) {
        return false;
    }

    public CloseableIterator<O> iterator(QueryOptions queryOptions) {
        return null;
    }

    public boolean isEmpty(QueryOptions queryOptions) {
        return false;
    }

    public boolean add(O object, QueryOptions queryOptions) {
        return false;
    }

    public boolean remove(Object o, QueryOptions queryOptions) {
        return false;
    }

    public boolean containsAll(Collection<?> c, QueryOptions queryOptions) {
        return false;
    }

    public boolean addAll(Collection<? extends O> c, QueryOptions queryOptions) {
        return false;
    }

    public boolean retainAll(Collection<?> c, QueryOptions queryOptions) {
        return false;
    }

    public boolean removeAll(Collection<?> c, QueryOptions queryOptions) {
        return false;
    }

    public void clear(QueryOptions queryOptions) {

    }
}