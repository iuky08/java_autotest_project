package com.actimind.petri.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Token extends DataHolder<Token> {

    public String toString() {
        return "⬤";
    }
}
