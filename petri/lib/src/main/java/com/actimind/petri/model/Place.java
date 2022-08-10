package com.actimind.petri.model;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Узел состояния сети Петри ("место"). Тут могут быть токены.
 */
public class Place extends DataHolder<Place> {
    @Getter
    @NonNull
    private final String id;
    @Getter
    @NonNull
    private final String title;

    private final List<Token> tokens = new ArrayList<>();
    private int maxTokensLimit = Integer.MAX_VALUE;

    public Place(@NonNull String id, @NonNull String title) {
        this.id = id;
        this.title = title;
    }

    public boolean canAcceptToken() {
        return tokens.size() < maxTokensLimit;
    }

    public Place oneTokenAllowed() {
        maxTokensLimit = 1;
        return this;
    }

    public Place add(Token token) {
        this.tokens.add(token);
        return this;
    }

    public Place addToken() {
        return this.add(new Token());
    }

    public Place remove(Token token) {
        this.tokens.remove(token);
        return this;
    }

    public Place addAll(List<Token> tokens) {
        this.tokens.addAll(tokens);
        return this;
    }

    public Place removeAll(List<Token> tokens) {
        this.tokens.removeAll(tokens);
        return this;
    }

    public Place removeAll() {
        this.tokens.clear();
        return this;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public String toString() {
        var tokensAsStr = tokens.stream().map(Token::toString).collect(Collectors.joining(""));
        return "(" + title + "  " + tokensAsStr + ")";
    }
}
