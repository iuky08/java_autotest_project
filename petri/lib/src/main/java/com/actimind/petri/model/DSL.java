package com.actimind.petri.model;

import com.actimind.petri.visualizer.SimpleVisualizer;

public class DSL {

    public static void markVisited(DataHolder<?> dataHolder) {
        if (dataHolder.get(Visited.class) == null) {
            dataHolder.add(new Visited());
        }
    }

    public static boolean isVisited(DataHolder<?> dataHolder) {
        return dataHolder.get(Visited.class) != null;
    }

    public static void showUI(Network network) {
        new SimpleVisualizer().main(network);
    }
}
