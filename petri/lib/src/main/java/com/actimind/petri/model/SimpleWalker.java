package com.actimind.petri.model;

//todo: rename

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.stream.Collectors;

import static com.actimind.petri.model.DSL.markVisited;

/**
 * Умеет обходить сеть Петри рандомно, помечая переходы, в которых побывал.
 * Завершает обход когда все переходы посещены.
 */
public class SimpleWalker {


    private Network network;
    private long seed = System.nanoTime();
    private Random random = new Random(seed);
    private Logger logger = LoggerFactory.getLogger(getClass());

    public SimpleWalker(Network network) {
        this.network = network;
    }

    public Network getNetwork() {
        return network;
    }

    public SimpleWalker withSeed(long seed) {
        this.seed = seed;
        this.random.setSeed(seed);
        return this;
    }


    @SneakyThrows
    public Transition step() {
        Transition selected = getNextTransition();
        return proceedWithTransition(selected);
    }

    public Transition getNextTransition() {
        var allowedTransitions = network.getTransitions()
                .filter(network::canProceedWith)
                .collect(Collectors.toList());

        if (allowedTransitions.size() == 0) throw new IllegalStateException("Cannot proceed"); //todo

        double sum = allowedTransitions.stream()
                .map(t -> t.get(ProbabilityWeight.class, new ProbabilityWeight(1)))
                .mapToDouble(t -> t.weight)
                .sum();

        double random = this.random.nextDouble() * sum;
        double accum = 0;
        //0.25, 0.5, 0.75, 1
        Transition selected = null;
        for (var t : allowedTransitions) {
            var prop = t.get(ProbabilityWeight.class).weight;
            accum += prop;
            logger.debug("Random = " + random + ", weight = " + prop);
            if (random <= accum) {
                //this is it
                selected = t;
                break;
            }
        }
        logger.debug("Going to proceed with transition: " + selected);
        return selected;
    }

    public Transition proceedWithTransition(Transition selected) {
        assert selected != null;
        selected.get(ProbabilityWeight.class).weight /= 2;
        network.proceed(selected);

        logger.debug("State after transition: ");
        logger.debug("\n" + network.toString());
        logger.debug("----------------------------------");

        markVisited(selected);
        postTransition(selected);

        return selected;
    }

    public boolean isFinished() {
        return network.getTransitions().allMatch(DSL::isVisited);
    }

    public void run() {
        logger.debug("Start walking. Seed = " + seed + ". Initial state: ");
        logger.debug("\n" + network.toString());
        logger.debug("----------------------------------");
        while (!isFinished()) {
            step();
            var visited = network.getTransitions().filter(DSL::isVisited).count();
            var total = network.getTransitions().count();
            logger.debug(" Visited " + visited + "/" + total + " transitions");
        }
        logger.debug("Stop walking. Final state: ");
        logger.debug("\n" + network.toString());
        logger.debug("----------------------------------");
    }


    protected void postTransition(Transition selected) {
    }


    @AllArgsConstructor
    public static class ProbabilityWeight {
        public float weight;
    }

    public long getSeed() {
        return seed;
    }


}
