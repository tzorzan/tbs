package it.tzorzan;

import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import java.util.ArrayList;
import java.util.List;

public class Actions {

    @Bean
    public static Action<States, Events> clearAction() {
        return ((StateContext<States, Events> context) -> {
            context.getExtendedState().getVariables().put(Variables.TURN, "");
            context.getExtendedState().getVariables().put(Variables.QUEUE, new ArrayList<String>());
        });
    }

    private static class QueueAction implements Action<States, Events> {
        @Override
        public void execute(StateContext<States, Events> context) {
            System.out.println("Action called: " + this.getClass().toString());
            List<String> queue = context.getExtendedState().get(Variables.QUEUE, List.class);
            List<String> newqueue = new ArrayList<>(queue);
            newqueue.add((String) context.getMessageHeader(Headers.NAME));
            context.getExtendedState().getVariables().put(Variables.QUEUE, newqueue);
        }
    }

    private static class DequeueAction implements Action<States, Events> {
        @Override
        public void execute(StateContext<States, Events> context) {
            System.out.println("Action called: " + this.getClass().toString());
            List<String> queue = context.getExtendedState().get(Variables.QUEUE, List.class);
            List<String> newqueue = new ArrayList<>(queue);
            context.getExtendedState().getVariables().put(Variables.TURN, new String(newqueue.get(0)));
            newqueue.remove(0);
            context.getExtendedState().getVariables().put(Variables.QUEUE, newqueue);
        }
    }


    @Bean
    public static QueueAction queueAction() {
        return new QueueAction();
    }

    @Bean
    public static DequeueAction dequeueAction() {
        return new DequeueAction();
    }

}
