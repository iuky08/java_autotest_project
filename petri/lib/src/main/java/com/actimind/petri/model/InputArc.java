package com.actimind.petri.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Соединение между состоянием и переходом. Определяет можно ли совершить переход и как это будет происходить.
 * <p>
 * По-умолчанию - переход можно произвести если в состоянии хотя бы 1 токен, один из токенов изымается.
 */
@AllArgsConstructor
public class InputArc extends DataHolder<InputArc> {

    @NonNull
    @Getter
    private final Place place;
    @NonNull
    @Getter
    private final Transition transition;
    @Getter
    @Setter
    @NonNull
    private Behavior behavior;

    public InputArc(@NonNull Place place, @NonNull Transition transition) {
        this(place, transition, new ConsumeOneToken());
    }

    public boolean canProceed() {
        return behavior.canProceed(this);
    }

    public void proceed() {
        behavior.proceed(this);
    }

    public interface Behavior {
        boolean canProceed(InputArc arc);

        void proceed(InputArc arc);
    }

    public static class ConsumeOneToken implements Behavior {
        @Override
        public boolean canProceed(InputArc arc) {
            return !arc.getPlace().getTokens().isEmpty();
        }

        @Override
        public void proceed(InputArc arc) {
            arc.getPlace().getTokens().remove(0);
        }
    }
}
