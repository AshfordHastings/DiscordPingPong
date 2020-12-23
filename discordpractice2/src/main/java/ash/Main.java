package ash;

import java.util.HashMap;
import java.util.Map;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Main {
	private static final Map<String, ash.Command> commands = new HashMap<String, Command>();
	
	static {
	    commands.put("ping", event -> event.getMessage().getChannel()
	        .flatMap(channel -> channel.createMessage("Pong!"))
	        .then());
	}	
				
	
	
	public static void main(String[] args) {
		GatewayDiscordClient client = DiscordClientBuilder.create(args[0])
				.build()
				.login()
				.block();
		
		client.getEventDispatcher().on(MessageCreateEvent.class)
	    // 3.1 Message.getContent() is a String
	    .flatMap(event -> Mono.just(event.getMessage().getContent())
	            .flatMap(content -> Flux.fromIterable(commands.entrySet())
	                // We will be using ! as our "prefix" to any command in the system.
	                .filter(entry -> content.startsWith('!' + entry.getKey()))
	                .flatMap(entry -> entry.getValue().execute(event))
	                .next()))
	    .subscribe();
		
		client.onDisconnect().block();
	}

}
