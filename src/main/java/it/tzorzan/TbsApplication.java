package it.tzorzan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;

import java.util.List;

@SpringBootApplication
public class TbsApplication implements CommandLineRunner {

	@Autowired
	private StateMachine<States, Events> stateMachine;

	private static final Logger log = LoggerFactory
			.getLogger(TbsApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TbsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//Adding interceptor for logging
		stateMachine.getStateMachineAccessor()
				.withRegion().addStateMachineInterceptor(new StateMachineInterceptorAdapter<States, Events>() {

			@Override
			public StateContext<States, Events> postTransition(StateContext<States, Events> stateContext) {
				States s = stateContext.getStateMachine().getState().getId();
				List<String> queue = stateContext.getExtendedState().get(Variables.QUEUE, List.class);
				String turn = stateContext.getExtendedState().get(Variables.TURN, String.class);
				if(States.isPublicState(s)) {
					log.info("S=" + s + " T=" + turn + " Q=" + queue);
				}
				return stateContext;
			}
		});


	}
}
