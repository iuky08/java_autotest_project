package com.actimind.petri.junit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Нужен чтобы помечать методы, которые выполняют переход в сети Петри
 *
 * @Transition("create customer")
 * public void createCustomer() {
 * //...
 * }
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Transition {
    String[] value();
}
