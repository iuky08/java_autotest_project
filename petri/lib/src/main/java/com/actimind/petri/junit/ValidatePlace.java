package com.actimind.petri.junit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Нужен чтобы помечать методы, которые выполняют соответствие состояния системы ожидаемому состоянию
 * сети Петри
 * <p>
 * В метод передаётся список токенов, находящихся в узле состояния сети.
 * Если узел пуст, список будет пустой.
 *
 * @ValidatePlace("customer exists")
 * public void checkCustomerExists(List<Token> tokens) {
 * //...
 * }
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidatePlace {
    String value();
}
