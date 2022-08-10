package com.actimind.petri.junit;

import com.actimind.petri.model.Transition;
import com.actimind.petri.model.*;
import io.qameta.allure.Allure;
import lombok.SneakyThrows;
import org.opentest4j.AssertionFailedError;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Расширенный обходчик сети Петри, который ещё выполянет валидацию и вызывает какие-то внешние методы
 * при переходах.
 */
public class TestWalker extends SimpleWalker implements NetworkSubscriber {
    private Map<Transition, ThrowableRunnable> transitionsReactions = new HashMap<>();
    private Map<Place, PlaceVerifier> placeVerifications = new HashMap<>();

    private List<Transition> history = new ArrayList<>();

    public TestWalker(Network network) {
        super(network);
    }

    @Override
    public NetworkSubscriber onTransition(Transition transition, ThrowableRunnable reaction) {
        transitionsReactions.put(transition, reaction);
        return this;
    }

    @Override
    public NetworkSubscriber verifyPlace(Place place, PlaceVerifier verification) {
        placeVerifications.put(place, verification);
        return this;
    }

    @SneakyThrows
    public void validate() {
        assertAll(
                "Running transitions " + history.stream().map(Transition::toString).collect(Collectors.joining(" -> ")) + " failed",
                getNetwork().getPlaces()
                        .map(place -> () -> {
                            var verify = placeVerifications.get(place);
                            if (verify == null) {
                                fail("Please add verification method for place '" + place.getTitle() + "', like this: \n" +
                                        "@ValidatePlace(\"" + place.getTitle() + "\")\n" +
                                        "public void doValidate() { assertTrue(false); }");
                            } else {
                                try {
                                    verify.verify(place.getTokens());
                                } catch (AssertionFailedError e) {
                                    throw new PlaceValidationError(
                                            place,
                                            place + " validation failed: " + e.getMessage(),
                                            e.getExpected(), e.getActual(), e
                                    );
                                } catch (AssertionError e) { //thrown by rest assure
                                    throw new PlaceValidationError(
                                            place,
                                            place + " validation failed: " + e.getMessage(),
                                            null, null, e
                                    );
                                }
                            }
                        })
        );
    }

    @Override
    @SneakyThrows
    protected void postTransition(Transition selected) {

        var reaction = transitionsReactions.get(selected);
        if (reaction == null) {
            if (transitionsReactions.isEmpty()) {
                printHelp();
            }

            fail("Please add method for transition '" + selected.getTitle() + "', like this: \n" +
                    "@Transition(\"" + selected.getTitle() + "\")\n" +
                    "public void doSomething() { /**/ }");
        }

        history.add(selected);
        Allure.description("Probability is " + selected.get(ProbabilityWeight.class).weight);
        assertDoesNotThrow(reaction::run,
                "Running transitions " + history.stream().map(Transition::toString).collect(Collectors.joining(" -> ")) + " failed"
        );
        Allure.step("Validate after transition", this::validate);

    }

    private String javaize(String str, String prefix) {
        return prefix + Arrays.stream(str.replaceAll("^[a-zA-Z0-9_ ]", "").split("\\s+"))
                .map(it -> it.substring(0, 1).toUpperCase() + it.substring(1))
                .collect(Collectors.joining());
    }

    private void printHelp() {
        System.out.println("You may copy following code template into your test");
        getNetwork().getTransitions().forEach(transition -> {
            System.out.println("   @Transition(\"" + transition.getTitle() + "\")");
            System.out.println("   public void " + javaize(transition.getTitle(), "do") + "() {}");
            System.out.println();
        });
        getNetwork().getPlaces().forEach(place -> {
            System.out.println("   @ValidatePlace(\"" + place.getTitle() + "\")");
            System.out.println("   public void " + javaize(place.getTitle(), "validate") + "(List<Token> tokens) {}");
            System.out.println();
        });
    }

    public static class PlaceValidationError extends AssertionFailedError {
        private final Place place;

        public PlaceValidationError(Place place, String message, Object expected, Object actual, Throwable cause) {
            super(message, expected, actual, cause);
            this.place = place;
        }

        public Place getPlace() {
            return place;
        }
    }
}
