package net.nixill.dicebot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;
import net.nixill.dice.operations.Functions;
import net.nixill.dicebot.events.MessageListener;
import net.nixill.dicebot.variables.DicebotLoader;

public class App {
  private static Snowflake us = null;
  
  public static void main(String[] args) throws IOException, SQLException {
    String key = new String(Files.readAllBytes(Paths.get("key.txt")),
        StandardCharsets.UTF_8);
    
    final DiscordClient client = new DiscordClientBuilder(key).build();
    
    // Update the relevant config files
    Updater.init();
    
    // Create a function loader
    Functions.setLoader(new DicebotLoader(Updater.getConnection()));
    
    // Create a config handler
    Config.init(Updater.getConnection());
    
    // Subscribe to events
    client.getEventDispatcher().on(MessageCreateEvent.class)
        .subscribe(new MessageListener());
    client.getEventDispatcher().on(ReadyEvent.class).subscribe((event) -> {
      us = event.getSelf().getId();
    });
    
    // And login!
    client.login().block();
  }
  
  public static Snowflake getOurId() {
    return us;
  }
  
  public static boolean isDebug() {
    // TODO read from a config
    return true;
  }
}
