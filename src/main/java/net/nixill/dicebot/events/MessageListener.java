package net.nixill.dicebot.events;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import net.nixill.dicebot.App;
import net.nixill.dicebot.commands.RollCommand;
import reactor.core.publisher.Mono;

public class MessageListener implements Consumer<MessageCreateEvent> {
  private static Pattern ptnRoll = Pattern.compile(
      "(?:(f(?:ull)?)|(d(?:ebug)?))?(s(?:eed(?:ed)?)?)?(?:r(?:oll)?|dice)");
  
  public void accept(MessageCreateEvent t) {
    Message msg = t.getMessage();
    Optional<String> opt = msg.getContent();
    
    if (!opt.isPresent()) {
      return;
    }
    
    String contents = opt.get();
    
    msg.getChannel().flatMap((channel) -> {
      Matcher mtc = Pattern
          .compile(
              "((?:<@!?\\d+> )*<@!?\\d+>)? ?!([a-zA-Z0-9-_]+)(?: *(.+))?")
          .matcher(contents);
      
      if (!mtc.matches()) {
        return Mono.empty();
      }
      
      boolean mention = false;
      String mentions = mtc.group(1);
      String command = mtc.group(2);
      String args = mtc.group(3);
      
      // If the command makes any mentions, we must be included.
      // If there are mentions but not us, return nothing.
      // Also, some commands require mentions, so also return whether or
      // not we are.
      if (mentions != null) {
        if (mentions.contains("<@" + App.getOurId().asString() + ">")
            || mentions
                .contains("<@!" + App.getOurId().asString() + ">")) {
          mention = true;
        } else {
          return Mono.empty();
        }
      }
      
      // Now start checking what command was used
      
      // Was it !r(oll)?
      Matcher rollMatcher = ptnRoll.matcher(command);
      
      if (rollMatcher.matches()) {
        return channel.createMessage(
            RollCommand.of(msg, channel, rollMatcher, args));
      }
      
      return Mono.empty();
    }).subscribe();
    
  }
}