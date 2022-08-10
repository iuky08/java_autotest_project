package com.actimind.petri.junit;

import com.actimind.petri.model.Transition;
import com.actimind.petri.model.*;

import java.util.List;

interface NetworkSubscriber {
    Network getNetwork();

    NetworkSubscriber onTransition(Transition transition, ThrowableRunnable reaction);

    NetworkSubscriber verifyPlace(Place place, PlaceVerifier verification);

    @FunctionalInterface
    interface PlaceVerifier {
        void verify(List<Token> tokensInPlace) throws Exception;
    }


}
