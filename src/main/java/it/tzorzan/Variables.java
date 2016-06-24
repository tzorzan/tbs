package it.tzorzan;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;

import java.util.List;
import java.util.Timer;

public enum Variables {
    QUEUE,
    TURN,
    TIMER,
    COUNTDOWN_TIMER,
    COUNTDOWN;

    public static List<String> getQueue(StateMachine<States, Events> machine) {
        return machine.getExtendedState().get(Variables.QUEUE, List.class);
    }

    public static List<String> getQueue(StateContext<States, Events> context) {
        return getQueue(context.getStateMachine());
    }

    public static void setQueue(StateContext<States, Events> context, List<String> list) {
        context.getExtendedState().getVariables().put(Variables.QUEUE, list);
        return;
    }

    public static String getTurn(StateMachine<States, Events> machine) {
        return machine.getExtendedState().get(Variables.TURN, String.class);
    }

    public static void setTurn(StateContext<States, Events> context, String name) {
        context.getExtendedState().getVariables().put(Variables.TURN, name);
        return;
    }

    public static Timer getTimer(StateMachine<States, Events> machine) {
        return machine.getExtendedState().get(Variables.TIMER, Timer.class);
    }

    public static void setTimer(StateContext<States, Events> context, Timer timer) {
        context.getExtendedState().getVariables().put(Variables.TIMER, timer);
        return;
    }

    public static Timer getCountdownTimer(StateMachine<States, Events> machine) {
        return machine.getExtendedState().get(Variables.COUNTDOWN_TIMER, Timer.class);
    }

    public static void setCountdownTimer(StateContext<States, Events> context, Timer timer) {
        context.getExtendedState().getVariables().put(Variables.COUNTDOWN_TIMER, timer);
        return;
    }

    public static Integer getCountdown(StateMachine<States, Events> machine) {
        return machine.getExtendedState().get(Variables.COUNTDOWN, Integer.class);
    }

    public static void setCountdown(StateMachine<States, Events> machine, Integer countdown) {
        machine.getExtendedState().getVariables().put(Variables.COUNTDOWN, countdown);
        return;
    }

}