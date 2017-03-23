package com.googlecode.cqengine.index.sqlite;

import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.AttributeIndex;

/*
The interface itself, exclude/include rules are annoying to write.
 */
public interface IdentityAttributeIndex<A, O> extends AttributeIndex<A, O> {
    SimpleAttribute<A, O> getForeignKeyAttribute();
}
