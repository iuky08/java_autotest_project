package com.actimind.petri.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor()
public class Transition extends DataHolder<Transition> {
    @NonNull
    @Getter
    final private String id;
    @NonNull
    @Getter
    final private String title;

    @Override
    public String toString() {
        return "[ " + title.toUpperCase() + " ]";
    }
}
