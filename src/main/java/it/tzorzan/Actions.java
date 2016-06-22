package it.tzorzan;

import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Actions {

    @Bean
    public static Action<States, Events> initvar() {
        return ( stateContext -> {
            setQueue(stateContext, new ArrayList<>());
            setTurn(stateContext, "");
            setTimer(stateContext, new Timer());
        });
    }

    @Bean
    public static Action<States, Events> queue() {
        return ( stateContext -> getQueue(stateContext).add((String) stateContext.getMessageHeader(Headers.NAME)) );
    }

    @Bean
    public static Action<States, Events> turn() {
        return ( stateContext -> setTurn(stateContext, getQueue(stateContext).stream().findFirst().orElse("")) );
    }

    @Bean
    public static Action<States, Events> dequeue() {
        return ( stateContext -> {
            List<String> queue = getQueue(stateContext);
            queue.remove(0);
            setQueue(stateContext, queue);
            setTurn(stateContext, "");
        });
    }

    @Bean
    public static Action<States, Events> startimer() {
        return (stateContext -> {
            getTimer(stateContext).schedule(new TimeoutTask(stateContext.getStateMachine()), 30000);
        });
    }

    @Bean
    public static Action<States, Events> discardtimer() {
        return (stateContext -> {
            getTimer(stateContext).cancel();
            getTimer(stateContext).purge();
        });
    }

    private static class TimeoutTask extends TimerTask {
        private StateMachine<States, Events> stateMachine;

        TimeoutTask(StateMachine<States, Events> sm) {
            this.stateMachine = sm;
        }

        @Override
        public void run() {
            this.stateMachine.sendEvent(Events.timeout);
        }
    }

    public static List<String> getQueue(StateContext<States, Events> context) {
        return context.getExtendedState().get(Variables.QUEUE, List.class);
    }

    private static void setQueue(StateContext<States, Events> context, List<String> list) {
        context.getExtendedState().getVariables().put(Variables.QUEUE, list);
        return;
    }

    private static void setTurn(StateContext<States, Events> context, String name) {
        context.getExtendedState().getVariables().put(Variables.TURN, name);
        return;
    }

    private static Timer getTimer(StateContext<States, Events> context) {
        return context.getExtendedState().get(Variables.TIMER, Timer.class);
    }

    private static void setTimer(StateContext<States, Events> context, Timer timer) {
        context.getExtendedState().getVariables().put(Variables.TIMER, timer);
        return;
    }
}
