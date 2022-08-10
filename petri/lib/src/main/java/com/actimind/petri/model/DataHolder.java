package com.actimind.petri.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// вспомогательгный класс-"помойка", для хранения разных доп свойств состояний, арок и пр
public class DataHolder<E extends DataHolder<E>> {

    final protected List<Object> data;

    public DataHolder() {
        data = new ArrayList<>();
    }

    public DataHolder(List<Object> data) {
        this.data = new ArrayList<>(data);
    }

    public E add(Object item) {
        data.add(item);
        return (E) this;
    }

    public <T> T get(Class<T> klass, T newValue) {
        return data.stream()
                .filter(klass::isInstance)
                .findFirst()
                .map(klass::cast)
                .orElseGet(() -> {
                    data.add(newValue);
                    return newValue;
                });

    }

    public <T> T get(Class<T> klass) {
        return data.stream()
                .filter(klass::isInstance)
                .findFirst()
                .map(klass::cast)
                .orElse(null);
    }

    public <T> List<T> getAll(Class<T> klass) {
        return data.stream()
                .filter(klass::isInstance)
                .map(klass::cast)
                .collect(Collectors.toList());
    }
}
