package it.tzorzan;

import it.tzorzan.tbs.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin
@Controller
public class APIController {
    @Autowired
    private StateMachine<States, Events> stateMachine;

    @RequestMapping(value = "/status", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public Status getStatus() {
        Status status = new Status();
        status.state = stateMachine.getState().getId().toString();
        status.queue = Variables.getQueue(stateMachine);
        status.countdown = Optional.ofNullable(Variables.getCountdown(stateMachine)).orElse(0);
        return status;
    }

    @RequestMapping(value = "/queue", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void addToQueue(@RequestParam("name") String name) {
        Message<Events> message = MessageBuilder
                .withPayload(Events.queue)
                .setHeader(Headers.NAME.toString(), name)
                .build();
        if (!stateMachine.sendEvent(message)) {
            throw new EventNotAcceptedException();
        }
    }

    @RequestMapping(value = "/enter", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void enter() {
        if(!stateMachine.sendEvent(Events.enter)) {
            throw new EventNotAcceptedException();
        }
    }

    @RequestMapping(value = "/exit", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void exit() {
        if(!stateMachine.sendEvent(Events.exit)) {
            throw new EventNotAcceptedException();
        }
    }

    @ResponseStatus(value=HttpStatus.NOT_ACCEPTABLE, reason="Event can not be accepted.")  // 406
    public class EventNotAcceptedException extends RuntimeException {
    }
}
