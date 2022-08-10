package com.actimind.petri.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Выходная связь, от перехода к состоянию.
 * <p>
 * Так же определяет можно ли совершать переход, и если да - то как.
 * <p>
 * По-умолчанию добавляет в выходное состояние ещё один токен.
 */
@AllArgsConstructor
public class OutputArc extends DataHolder<OutputArc> {

    @NonNull
    @Getter
    private final Transition transition;
    @NonNull
    @Getter
    private final Place place;
    @Getter
    @Setter
    @NonNull
    private Behavior behavior;

    public OutputArc(Transition transition, Place place) {
        this(transition, place, new ProduceOneToken());
    }

    public boolean canProceed() {
        return behavior.canProceed(this);
    }

    public void proceed() {
        behavior.proceed(this);
    }


    public interface Behavior {
        boolean canProceed(OutputArc arc);

        void proceed(OutputArc arc);
    }

    public static class ProduceOneToken implements Behavior {
        @Override
        public boolean canProceed(OutputArc arc) {
            return arc.get(TwoDirectional.class) != null || arc.getPlace().canAcceptToken();
        }

        @Override
        public void proceed(OutputArc arc) {
            arc.getPlace().addToken();
        }
    }
}

