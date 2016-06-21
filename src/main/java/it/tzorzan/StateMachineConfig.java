package it.tzorzan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {
        states
                .withStates()
                .initial(States.FREE, Actions.initialAction())
                .state(States.TURN, Actions.turnAction(), null)
                .choice(States.CHECK)
                .states(EnumSet.allOf(States.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        transitions
                .withExternal().source(States.FREE).target(States.TURN).event(Events.queue).action(Actions.queueAction())
                .and()
                .withExternal().source(States.TURN).target(States.WAITING)
                .and()
                .withInternal().source(States.TURN).event(Events.queue).action(Actions.queueAction())
                .and()
                .withInternal().source(States.WAITING).event(Events.queue).action(Actions.queueAction())
                .and()
                .withInternal().source(States.OCCUPIED).event(Events.queue).action(Actions.queueAction())
                .and()
                .withExternal().source(States.WAITING).target(States.OCCUPIED).event(Events.enter)
                .and()
                .withExternal().source(States.FREE).target(States.OCCUPIED).event(Events.enter)
                .and()
                .withExternal().source(States.OCCUPIED).target(States.CHECK).event(Events.exit).action(Actions.dequeueAction())
                .and()
                .withExternal().source(States.WAITING).target(States.CHECK).timer(30000).action(Actions.dequeueAction())
                .and()
                .withChoice().source(States.CHECK).first(States.FREE, Guards.emptyQueueGuard()).last(States.TURN);
    }

    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<States, Events>() {
            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                if(from != null) {
                    System.out.println("State change " + from.getId() + " -> " + to.getId());
                } else {
                    System.out.println("State change to: " + to.getId());
                }
            }

            @Override
            public void eventNotAccepted(Message<Events> event) {
                System.out.println("Event not accepted: " + event.toString());
            }

            @Override
            public void extendedStateChanged(Object key, Object value) {
                System.out.println("Extended state: " + key.toString() + " -> " + value.toString());
            }
        };
    }
}
