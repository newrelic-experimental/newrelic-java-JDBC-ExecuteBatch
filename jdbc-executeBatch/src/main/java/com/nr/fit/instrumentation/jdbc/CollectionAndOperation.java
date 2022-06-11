package com.nr.fit.instrumentation.jdbc;

import java.util.Objects;

public class CollectionAndOperation {
    private final String collection;
    private final String operation;

    public CollectionAndOperation(String collection, String operation) {
        this.collection = collection;
        this.operation = operation;
    }

    public String getCollection() {
        return collection;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        CollectionAndOperation that = (CollectionAndOperation) o;
        return collection.equals(that.collection) && operation.equals(that.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collection, operation);
    }
}
