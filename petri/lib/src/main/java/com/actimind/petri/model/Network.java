package com.actimind.petri.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Сеть Петри.
 * <p>
 * Свои сети проще всего делать через наследование, см примеры.
 */
public class Network {

    private final Map<String, Place> places = new HashMap<>();
    private final Map<String, Transition> transitions = new HashMap<>();

    private final Map<String, List<InputArc>> inputArcs = new HashMap<>();
    private final Map<String, List<OutputArc>> outputArcs = new HashMap<>();

    private int id = 1;

    private String genId() {
        return "" + id++;
    }

    /**
     * Создаёт узел состояния ("место") с указанным именем в текущей сети
     */
    public Place place(String title) {
        Place place = new Place(genId(), title);
        places.put(place.getId(), place);
        return place;
    }

    /**
     * Создает узел перехода с указанным именем.
     * Возвращает TransitionBuilder, который помогает установить входные и выходыне связи.
     */
    public TransitionBuilder transition(String title) {
        Transition transition = new Transition(genId(), title);
        transitions.put(transition.getId(), transition);
        return new TransitionBuilder(transition);
    }

    public Token token() {
        return new Token();
    }

    public OutputArc connect(Transition transition, Place place) {
        var arc = new OutputArc(transition, place);
        outputArcs.computeIfAbsent(place.getId(), s -> new ArrayList<>()).add(arc);
        outputArcs.computeIfAbsent(transition.getId(), s -> new ArrayList<>()).add(arc);
        getInputArcs(transition).stream()
                .filter(it -> it.getPlace() == place)
                .findAny()
                .ifPresent(inputArc -> {
                    inputArc.add(new TwoDirectional());
                    arc.add(new TwoDirectional());
                });
        return arc;
    }

    public InputArc connect(Place place, Transition transition) {
        var arc = new InputArc(place, transition);
        inputArcs.computeIfAbsent(place.getId(), s -> new ArrayList<>()).add(arc);
        inputArcs.computeIfAbsent(transition.getId(), s -> new ArrayList<>()).add(arc);
        getOutputArcs(transition).stream()
                .filter(it -> it.getPlace() == place)
                .findAny()
                .ifPresent(outputArc -> {
                    outputArc.add(new TwoDirectional());
                    arc.add(new TwoDirectional());
                });
        return arc;
    }

    public boolean canProceedWith(Transition transition) {
        return getInputArcs(transition)
                .stream()
                .allMatch(InputArc::canProceed)

                && getOutputArcs(transition)
                .stream()
                .allMatch(OutputArc::canProceed);
    }


    @SneakyThrows
    public Transition proceed(Transition transition) {
        getInputArcs(transition).forEach(InputArc::proceed);
        getOutputArcs(transition).forEach(OutputArc::proceed);
        return transition;
    }

    public List<InputArc> getInputArcs(Transition transition) {
        return inputArcs
                .getOrDefault(transition.getId(), Collections.emptyList());
    }


    public List<OutputArc> getOutputArcs(Transition transition) {
        return outputArcs.getOrDefault(transition.getId(), Collections.emptyList());
    }

    public Stream<Transition> getTransitions() {
        return transitions.values().stream();
    }

    public Stream<Place> getPlaces() {
        return places.values().stream();
    }

    @AllArgsConstructor
    @Getter
    public class TransitionBuilder {
        final private Transition transition;

        /**
         * Добавляет входнгую связь от указанных "мест" к этому переходу
         */
        public TransitionBuilder from(Place... places) {
            for (var place : places) {
                connect(place, transition);
            }
            return this;
        }

        /**
         * Добавляет входнгую связь от места к этому перехода
         * с особым поведением
         */
        public TransitionBuilder from(Place place, InputArc.Behavior behavior) {
            connect(place, transition).setBehavior(behavior);
            return this;
        }

        /**
         * Добавляет двустороннюю связь из указанных "мест" в них же
         */
        public TransitionBuilder fromAndTo(Place... places) {
            for (var place : places) {
                connect(place, transition);
                connect(transition, place);
            }
            return this;
        }

        /**
         * Добавляет выходные связи от перехода к указанным "местам"
         */
        public TransitionBuilder to(Place... places) {
            for (var place : places) {
                connect(transition, place);
            }
            return this;
        }

        /**
         * Добавляет выходную связь с указанным поведением
         */
        public TransitionBuilder to(Place place, OutputArc.Behavior behavior) {
            connect(transition, place).setBehavior(behavior);
            return this;
        }

        /**
         * При переходе сбрасывает указанные "места" (удаляет токены)
         */
        public TransitionBuilder reset(Place... places) {
            for (var place : places) {
                connect(transition, place).setBehavior(new ResetArcBehaviour());
            }
            return this;
        }

        /**
         * Запрещает переход, если в укеазаннных "местах" есть токены
         */
        public TransitionBuilder inhibitFrom(Place... places) {
            for (var place : places) {
                connect(place, transition).setBehavior(new InhibitArcBehaviour());
            }
            return this;
        }


        public Transition build() {
            return transition;
        }
    }

    public String toString() {
        return this.transitions.values()
                .stream()
                .map(transition -> {
                    var inputPlaces = getInputArcs(transition)
                            .stream()
                            .map(InputArc::getPlace)
                            .map(Place::toString)
                            .collect(Collectors.joining(", "));
                    var outputPlaces = getOutputArcs(transition)
                            .stream()
                            .map(OutputArc::getPlace)
                            .map(Place::toString)
                            .collect(Collectors.joining(", "));

                    return inputPlaces + " -> " + transition + " -> " + outputPlaces;

                })
                .collect(Collectors.joining("\n"));
    }

}
