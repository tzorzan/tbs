package it.tzorzan;

import it.tzorzan.tbs.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.*;
import java.util.stream.Collectors;

import static it.tzorzan.Variables.*;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Component
public class Actions {
    private static final Logger log = LoggerFactory
            .getLogger(TbsApplication.class);

    private static StateMachine<States, Events> stateMachine;
    private static SimpMessagingTemplate messageTemplate;

    @Autowired
    public Actions(StateMachine<States, Events> stateMachine, SimpMessagingTemplate template) {
        this.stateMachine = stateMachine;
        this.messageTemplate = template;
    }

    public static Integer TIMEOUT;

    @Value("${tbs.queue.timeout}")
    public void setQueueTimeout(Integer timeout) {
        TIMEOUT = timeout;
    }

    public static String UNKNOWN;

    @Value("${tbs.queue.unknown}")
    public void setQueueUnknown(String unknown) {
        UNKNOWN = unknown;
    }

    @Bean
    public static Action<States, Events> initvar() {
        return ( stateContext -> {
            setQueue(stateContext, new ArrayList<>());
            setCountdown(stateContext, 0);
        });
    }

    @Bean
    public static Action<States, Events> queue() {
        return ( stateContext -> {
            getQueue(stateContext).add((String) stateContext.getMessageHeader(Headers.NAME));
            updateStatus(stateContext.getStateMachine());
        });
    }

    @Bean
    public static Action<States, Events> queueUnknown() {
        return ( stateContext -> {
            getQueue(stateContext).add(UNKNOWN);
            updateStatus(stateContext.getStateMachine());
        });
    }

    @Bean
    public static Action<States, Events> dequeue() {
        return ( stateContext -> {
            List<String> queue = getQueue(stateContext);
            queue.remove(0);
            setQueue(stateContext, queue);
            updateStatus(stateContext.getStateMachine());
        });
    }

    @Bean
    public static Action<States, Events> dequeueName() {
        return ( stateContext -> {
            List<String> newqueue = getQueue(stateContext).stream()
                    .filter(s -> !s.equals( stateContext.getMessageHeader(Headers.NAME)))
                    .collect(Collectors.toList());
            setQueue(stateContext, newqueue);
            updateStatus(stateContext.getStateMachine());
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
            updateStatus(stateContext.getStateMachine());
        });
    }

    @Bean
    public static Action<States, Events> discardtimer() {
        return ( stateContext -> {
            discardTimers(stateContext.getStateMachine());
            updateStatus(stateContext.getStateMachine());
            });
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
            updateStatus(stateMachine);
            log.debug("COUNTDOWN="+ getCountdown(stateMachine));
        }
    }

    private static void discardTimers(StateMachine<States, Events> machine) {
        getTimer(machine).cancel();
        getTimer(machine).purge();
        getCountdownTimer(machine).cancel();
        getCountdownTimer(machine).purge();
        setCountdown(machine, 0);
    }

    private static Status updateStatus(StateMachine stateMachine) {
        Status status = new Status(
                stateMachine.getState().getId().toString(),
                Variables.getQueue(stateMachine),
                Optional.ofNullable(Variables.getCountdown(stateMachine)).orElse(0)
        );
        messageTemplate.convertAndSend("/topic/status", status);
        return status;
    }

    @EventListener
    public static void sessionConnectedHandler(SessionSubscribeEvent event) {
        String destination = event.getMessage().getHeaders().get("simpDestination").toString();
        if(destination.equals("/topic/status")) {
            updateStatus(stateMachine);
        }
    }
}
