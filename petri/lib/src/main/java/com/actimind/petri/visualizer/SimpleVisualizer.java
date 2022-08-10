package com.actimind.petri.visualizer;

import com.actimind.petri.ExampleNetwork;
import com.actimind.petri.model.*;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Attributes.attrs;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

public class SimpleVisualizer {

    public void main(Network network) {
        var frame = new JFrame("Test");
        var walker = new SimpleWalker(network);
        frame.setSize(1400, 900);
        final BufferedImage[] image = {renderNetwork(network, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), 1200, 800)};
        var imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image[0], 0, 0, null);
            }
        };

        var doButton = new JButton("Step");

        doButton.addActionListener(e -> {
            var executed = walker.step();
            doButton.setEnabled(!walker.isFinished());
            image[0] = renderNetwork(network, Set.of(executed), network.getTransitions().filter(DSL::isVisited).collect(Collectors.toSet()), Collections.emptySet(), 1200, 800);
            frame.getContentPane().repaint();
        });

        frame.setLayout(new BorderLayout());
        frame.add(doButton, BorderLayout.SOUTH);
        frame.add(imagePanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public BufferedImage renderNetwork(Network network,
                                       Set<Transition> currentTransitions,
                                       Set<Transition> visitedTransitions,
                                       Set<Place> failedVerificationPlaces,
                                       int width, int height
    ) {
        var g = graph().directed()
                .toMutable();
        //todo: tokens as well
        var nodes = network.getPlaces()
                .collect(Collectors.toMap(
                        Place::getId,
                        place -> node(place.getTitle())
                                .with(Label.of(place.getTokens().stream().map(it -> "&#11044;").collect(Collectors.joining())))
                                .with(Attributes.attr("xlabel", place.getTitle()))
                                .with(Shape.CIRCLE)
                                .with(Style.FILLED)
                                .with(failedVerificationPlaces.contains(place) ? Color.RED3.fill() : Color.WHITE.fill())
                        //.with(Font.size(4))
                        //.with(Size.mode(Size.Mode.FIXED).size(0.2, 0.2))
                        //.with(place.getTokens().isEmpty() ? Color.WHITE.fill() : Color.BLACK.fill())
                ));
        var trNodes = network.getTransitions()
                .collect(Collectors.toMap(
                        Transition::getId,
                        transition -> node(transition.getTitle())
                                .with(Shape.RECTANGLE)
                                .with(
                                        currentTransitions.contains(transition)
                                                ? Color.GREEN1
                                                : (visitedTransitions.contains(transition) ? Color.BLUE1 : attrs())
                                )
                        //.with(Font.size(4))
                        //.with(Size.mode(Size.Mode.FIXED).size(1, 0.2))
                ));

        network.getTransitions()
                .forEach(transition -> {
                    Node transitionNode = trNodes.get(transition.getId());
                    network.getInputArcs(transition).forEach(arc -> g.add(
                            nodes.get(arc.getPlace().getId())
                                    .link(
                                            to(transitionNode)
                                                    .with(
                                                            (arc.getBehavior() instanceof InhibitArcBehaviour)
                                                                    ? List.of(Arrow.DOT.open())
                                                                    : Collections.emptyList()
                                                    )
                                                    .with(
                                                            arc.get(TwoDirectional.class) != null
                                                                    ? List.of(Arrow.NORMAL.dir(Arrow.DirType.BOTH))
                                                                    : Collections.emptyList()
                                                    )
                                    )
                    ));
                    network.getOutputArcs(transition)
                            .stream().filter(it -> it.get(TwoDirectional.class) == null)
                            .forEach(arc -> g.add(
                                    transitionNode
                                            .link(
                                                    to(nodes.get(arc.getPlace().getId()))
                                                            .with(
                                                                    (arc.getBehavior() instanceof ResetArcBehaviour)
                                                                            ? List.of(Style.DOTTED)
                                                                            : Collections.emptyList()
                                                            )
                                            )
                            ));

                });

        return Graphviz.fromGraph(g)
                .engine(Engine.DOT)
                .width(width).height(height)
                .render(Format.PNG).toImage();
    }

    public static void main(String[] args) {
        var network = new ExampleNetwork();
        new SimpleVisualizer().main(network);
    }

}
