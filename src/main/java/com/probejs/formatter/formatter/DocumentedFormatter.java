package com.probejs.formatter.formatter;

public abstract class DocumentedFormatter<T> implements IDocumented<T> {

    protected T document;

    @Override
    public void addDocument(T document) {
        this.document = document;
    }
}
