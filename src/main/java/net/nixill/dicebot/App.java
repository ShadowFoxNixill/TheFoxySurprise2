package net.nixill.dicebot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import net.nixill.dicebot.events.MessageListener;

public class App {
  public static void main(String[] args) throws IOException {
    String key = new String(Files.readAllBytes(Paths.get("key.txt")), StandardCharsets.UTF_8);

    final DiscordClient client = new DiscordClientBuilder(key).build();

    client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(new MessageListener());

    client.login().block();
  }
}
