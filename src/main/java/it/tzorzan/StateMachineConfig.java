package it.tzorzan;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true);
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {
        states
                .withStates()
                .initial(States.FREE, Actions.initvar())
                .state(States.WAITING, Actions.startimer(), null)
                .choice(States.CHECK)
                .state(States.FREE, Actions.updateStatus(), null)
                .state(States.OCCUPIED, Actions.updateStatus(), null)
                .state(States.CHECK)
                .state(States.TURN);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        transitions
                .withExternal().source(States.FREE).target(States.TURN).event(Events.queue).action(Actions.queue())
                .and()
                .withExternal().source(States.TURN).target(States.WAITING)
                .and()
                .withInternal().source(States.TURN).event(Events.queue).action(Actions.queue())
                .and()
                .withInternal().source(States.WAITING).event(Events.queue).action(Actions.queue())
                .and()
                .withInternal().source(States.OCCUPIED).event(Events.queue).action(Actions.queue())
                .and()
                .withInternal().source(States.TURN).event(Events.dequeue).action(Actions.dequeueName())
                .and()
                .withInternal().source(States.WAITING).event(Events.dequeue).action(Actions.dequeueName())
                .and()
                .withInternal().source(States.OCCUPIED).event(Events.dequeue).action(Actions.dequeueName())
                .and()
                .withExternal().source(States.WAITING).target(States.OCCUPIED).event(Events.enter).action(Actions.discardtimer())
                .and()
                .withExternal().source(States.FREE).target(States.OCCUPIED).event(Events.enter).action(Actions.queueUnknown())
                .and()
                .withExternal().source(States.OCCUPIED).target(States.CHECK).event(Events.exit).action(Actions.dequeue())
                .and()
                .withExternal().source(States.WAITING).target(States.CHECK).event(Events.timeout).action(Actions.dequeue())
                .and()
                .withChoice().source(States.CHECK).first(States.FREE, Guards.emptyQueueGuard()).last(States.TURN);
    }
}
