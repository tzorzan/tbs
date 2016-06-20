package it.tzorzan;

import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import java.util.List;

public class Guards {
    private static class EmptyQueueGuard implements Guard<States, Events> {
        @Override
        public boolean evaluate(StateContext<States, Events> context) {
            List<String> queue = context.getExtendedState().get(Variables.QUEUE, List.class);
            return queue.isEmpty();
        }
    }

    @Bean
    public static EmptyQueueGuard emptyQueueGuard() {
        return new EmptyQueueGuard();
    }

}
