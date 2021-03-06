package com.googlecode.cqengine.index.sqlite;

import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.persistence.support.ObjectSet;
import com.googlecode.cqengine.persistence.support.ObjectStore;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;

/*
No-op impl, so that CQE doesn't need to be edited or super sourced.
 */
public class SQLiteIdentityIndex<A extends Comparable<A>, O> implements Index<O> {

    public boolean isMutable() {
        return false;
    }

    public boolean supportsQuery(Query<O> query, QueryOptions queryOptions) {
        return false;
    }

    public boolean isQuantized() {
        return false;
    }

    public ResultSet<O> retrieve(Query<O> query, QueryOptions queryOptions) {
        return null;
    }

    public Index<O> getEffectiveIndex() {
        return null;
    }

    public boolean addAll(ObjectSet<O> objectSet, QueryOptions queryOptions) {
        return true;
    }

    public boolean removeAll(ObjectSet<O> objectSet, QueryOptions queryOptions) {
        return true;
    }

    public void clear(QueryOptions queryOptions) {

    }

    public void init(ObjectStore<O> objectStore, QueryOptions queryOptions) {

    }
}
