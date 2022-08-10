package com.actimind.petri.model;

/**
 * Реализует ингибиторную связь (когда переход запрещён, если в состоянии есть токен)
 */
public class InhibitArcBehaviour implements InputArc.Behavior {

    @Override
    public void proceed(InputArc arc) {
        //do nothing
    }

    @Override
    public boolean canProceed(InputArc arc) {
        return arc.getPlace().getTokens().isEmpty();
    }
}
