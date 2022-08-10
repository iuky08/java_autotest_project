package com.actimind.petri.junit;

import com.actimind.petri.model.Network;
import com.actimind.petri.model.Place;
import com.actimind.petri.model.ThrowableRunnable;
import com.actimind.petri.visualizer.SimpleVisualizer;
import io.qameta.allure.Allure;
import lombok.SneakyThrows;
import org.opentest4j.MultipleFailuresError;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DSL {


    public static void addTokens(Place... places) {
        for (var place : places) {
            place.addToken();
        }
    }

    /**
     * Выполянет прогон сети петри.
     *
     * @param network сеть Петри
     * @param binding объект, у которого есть методы, отмеченные @Transition и @ValidatePlace
     */
    public static <E extends Network> void networkTest(E network, Object binding) {
        networkTest(network, binding, -1);
    }

    @SneakyThrows
    public static <E extends Network> void networkTest(E network, Object binding, long seed) {
        var walker = new TestWalker(network);
        if (seed != -1) {
            walker.withSeed(seed);
        }
        bindUsingReflection(walker, binding);
        Allure.description("Seed " + walker.getSeed());
        Allure.step("Validate initial state", walker::validate);
        while (!walker.isFinished()) {
            var nextTransition = walker.getNextTransition();
            Allure.step(nextTransition.getTitle(), () -> {
                try {
                    walker.proceedWithTransition(nextTransition);
                    Allure.addAttachment("network", "image/png",
                            new ByteArrayInputStream(toByteArray(
                                    new SimpleVisualizer().renderNetwork(
                                            network, Set.of(nextTransition),
                                            network.getTransitions().filter(com.actimind.petri.model.DSL::isVisited).collect(Collectors.toSet()),
                                            Collections.emptySet(), 600, 600
                                    ), "png")), "png"
                    );
                } catch (MultipleFailuresError e) {
                    Allure.addAttachment("network", "image/png",
                            new ByteArrayInputStream(toByteArray(
                                    new SimpleVisualizer().renderNetwork(
                                            network, Set.of(nextTransition),
                                            network.getTransitions().filter(com.actimind.petri.model.DSL::isVisited).collect(Collectors.toSet()),
                                            e.getFailures().stream()
                                                    .filter(it -> it instanceof TestWalker.PlaceValidationError)
                                                    .map(it -> ((TestWalker.PlaceValidationError) it).getPlace())
                                                    .collect(Collectors.toSet())
                                            , 600, 600
                                    ), "png")), "png"
                    );

                    throw e;
                } catch (Throwable e) {
                    Allure.addAttachment("network", "image/png",
                            new ByteArrayInputStream(toByteArray(
                                    new SimpleVisualizer().renderNetwork(
                                            network, Set.of(nextTransition),
                                            network.getTransitions().filter(com.actimind.petri.model.DSL::isVisited).collect(Collectors.toSet()),
                                            Collections.emptySet(), 600, 600
                                    ), "png")), "png"
                    );

                    throw e;
                }
            });
        }
    }


    private static void bindUsingReflection(NetworkSubscriber walker, Object object) {
        var networkTransitionsByTitle = walker.getNetwork().getTransitions()
                .collect(Collectors.toMap(com.actimind.petri.model.Transition::getTitle, Function.identity()));
        var networkPlacesByTitle = walker.getNetwork().getPlaces()
                .collect(Collectors.toMap(Place::getTitle, Function.identity()));

        for (var method : object.getClass().getDeclaredMethods()) {
            var transitionAnnotation = method.getAnnotation(Transition.class);
            if (transitionAnnotation != null) {
                for (var title : transitionAnnotation.value()) {
                    var transition = networkTransitionsByTitle.get(title);
                    if (transition == null) {
                        throw new IllegalStateException("Method '" + method.getName() + "' refers to unknown transition with title '" + title + "'.\n\nKnown are: " + String.join(",", networkTransitionsByTitle.keySet()));
                    }
                    walker.onTransition(transition, () -> unwrapReflectionException(() -> method.invoke(object)));
                }
            }
            var validatePlaceAnnotation = method.getAnnotation(ValidatePlace.class);
            if (validatePlaceAnnotation != null) {
                var place = networkPlacesByTitle.get(validatePlaceAnnotation.value());
                if (place == null)
                    throw new IllegalStateException("Method '" + method.getName() + "' refers to unknown place with title '" + validatePlaceAnnotation.value() + "'.\n\nKnown are: " + String.join(",", networkPlacesByTitle.keySet()));
                if (method.getParameterCount() != 1)
                    throw new IllegalStateException("Method '" + method.getName() + "' is marked with ValidatePlace annotation and must have 1 argument of type 'List<Token>'");
                walker.verifyPlace(place, tokensInPlace ->
                        unwrapReflectionException(() -> method.invoke(object, tokensInPlace))
                );
            }
        }

    }

    @SneakyThrows
    private static void unwrapReflectionException(ThrowableRunnable r) {
        try {
            r.run();
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private static byte[] toByteArray(BufferedImage bi, String format)
            throws IOException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bi, format, baos);
            return baos.toByteArray();
        }

    }


}
