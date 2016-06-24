package it.tzorzan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static it.tzorzan.Variables.*;

public class Actions {
    private static final Logger log = LoggerFactory
            .getLogger(TbsApplication.class);

    public static final Integer TIMEOUT = 30000;

    @Bean
    public static Action<States, Events> initvar() {
        return ( stateContext -> {
            setQueue(stateContext, new ArrayList<>());
            setTurn(stateContext, "");
            setCountdown(stateContext, 0);
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
            StateMachine<States, Events> stateMachine = stateContext.getStateMachine();
            Timer t = new Timer();
            Timer c = new Timer();
            t.schedule(new TimeoutTask(stateMachine), TIMEOUT);
            c.scheduleAtFixedRate(new CountdownTask(stateMachine), 1000, 1000);
            setTimer(stateContext, t);
            setCountdownTimer(stateContext, c);
            setCountdown(stateMachine, TIMEOUT/1000);
        });
    }

    @Bean
    public static Action<States, Events> discardtimer() {
        return ( stateContext -> discardTimers(stateContext.getStateMachine()) );
    }

    private static class TimeoutTask extends TimerTask {
        private StateMachine<States, Events> stateMachine;

        TimeoutTask(StateMachine<States, Events> sm) {
            this.stateMachine = sm;
        }

        @Override
        public void run() {
            discardTimers(stateMachine);
            stateMachine.sendEvent(Events.timeout);
        }
    }

    private static class CountdownTask extends TimerTask {
        private StateMachine<States, Events> stateMachine;

        CountdownTask(StateMachine<States, Events> machine) {
            this.stateMachine = machine;
        }

        @Override
        public void run() {
            setCountdown(stateMachine, getCountdown(stateMachine) - 1);
            //TODO: emit websocket event
            log.info("COUNTDOWN="+ getCountdown(stateMachine));
        }
    }

    private static void discardTimers(StateMachine<States, Events> machine) {
        getTimer(machine).cancel();
        getTimer(machine).purge();
        getCountdownTimer(machine).cancel();
        getCountdownTimer(machine).purge();
        setCountdown(machine, 0);
    }

}
