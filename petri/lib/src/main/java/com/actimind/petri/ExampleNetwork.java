package com.actimind.petri;

import com.actimind.petri.model.Network;
import com.actimind.petri.model.Place;
import com.actimind.petri.model.Transition;

public class ExampleNetwork extends Network {

    public Place userNotCreated = place("user not created").addToken();
    public Place userCreated = place("user created");
    public Place noteCreated = place("note created");//.oneTokenAllowed();

    public Transition createUser = transition("create user")
            .from(userNotCreated)
            .to(userCreated)
            .build();
    public Transition destroyUser = transition("destroy user")
            .from(userCreated)
            .to(userNotCreated)
            //.reset(noteCreated)
            .inhibitFrom(noteCreated)
            .build();
    public Transition createNote = transition("create note")
            .fromAndTo(userCreated)
            .to(noteCreated)
            .build();
    public Transition destroyNote = transition("destroy note")
            .from(noteCreated)
            .build();

}
