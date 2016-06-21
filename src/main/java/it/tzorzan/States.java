package it.tzorzan;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum States {
    FREE,
    WAITING,
    OCCUPIED,
    CHECK,
    TURN;

    public static List<States> publicStates() {
        return Stream.of(States.FREE, States.WAITING, States.OCCUPIED).collect(Collectors.toList());
    }

    public static Boolean isPublicState(States s) {
        return publicStates().contains(s);
    }

}
