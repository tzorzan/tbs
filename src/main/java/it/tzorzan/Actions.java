package it.tzorzan;

import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import java.util.ArrayList;
import java.util.List;

public class Actions {

    @Bean
    public static Action<States, Events> initvar() {
        return ( context -> {
            setQueue(context, new ArrayList<>());
            setTurn(context, "");
        });
    }

    @Bean
    public static Action<States, Events> queue() {
        return ( context -> getQueue(context).add((String) context.getMessageHeader(Headers.NAME)) );
    }

    @Bean
    public static Action<States, Events> turn() {
        return ( context -> setTurn(context, getQueue(context).stream().findFirst().orElse("")) );
    }

    @Bean
    public static Action<States, Events> dequeue() {
        return ( context -> {
            List<String> queue = getQueue(context);
            queue.remove(0);
            setQueue(context, queue);
            setTurn(context, "");
        });
    }

    private static List<String> getQueue(StateContext<States, Events> context) {
        return context.getExtendedState().get(Variables.QUEUE, List.class);
    }

    private static void setQueue(StateContext<States, Events> context, List<String> list) {
        context.getExtendedState().getVariables().put(Variables.QUEUE, list);
        return;
    }

    private static void setTurn(StateContext<States, Events> context, String name) {
        context.getExtendedState().getVariables().put(Variables.TURN, name);
    }

}
