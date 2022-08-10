package com.actimind.petri.model;

/**
 * Определяет связь, которая убирает все токены из состояния при переходе.
 */
public class ResetArcBehaviour implements OutputArc.Behavior {

    @Override
    public void proceed(OutputArc arc) {
        arc.getPlace().removeAll();
    }

    @Override
    public boolean canProceed(OutputArc arc) {
        return true;
    }
}
