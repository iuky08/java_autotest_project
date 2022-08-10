package com.actimind.petri;

import com.actimind.petri.junit.Transition;
import com.actimind.petri.junit.ValidatePlace;
import com.actimind.petri.model.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.List;

import static com.actimind.petri.junit.DSL.networkTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExampleJUnitTest {

    @RepeatedTest(value = 2, name = "{displayName}: Pass {currentRepetition}/{totalRepetitions}")
    @DisplayName("Test 1")
    public void test1() {
        var network = new ExampleNetwork();
        networkTest(network, this);
    }

    private int notesCount = 0;
    private int usersCount = 1;

    public ExampleJUnitTest() {
        System.out.println("CREATED");
    }

    @Transition("create user")
    public void createUser() {
        usersCount++;
    }

    @Transition("destroy user")
    public void destroyUser() {
        usersCount--;
        notesCount = 0;
    }

    @Transition("create note")
    public void createNote() {
        notesCount++;
    }

    @Transition("destroy note")
    public void destroyNote() {
        notesCount--;
    }

    @ValidatePlace("user not created")
    public void testUserNotCreated(List<Token> tokens) {
        assertEquals(!tokens.isEmpty(), usersCount == 0);
    }

    @ValidatePlace("user created")
    public void testUserCreated(List<Token> tokens) {
        assertEquals(tokens.size(), usersCount);
    }

    @ValidatePlace("note created")
    public void testNoteCreated(List<Token> tokens) {
        assertEquals(tokens.size(), notesCount, "Count of notes created in system does not match expected");
    }

}
