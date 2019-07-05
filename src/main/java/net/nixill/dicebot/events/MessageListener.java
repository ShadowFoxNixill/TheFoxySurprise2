package net.nixill.dicebot.events;

import java.util.Optional;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import net.nixill.dice.objects.DCEntity;
import net.nixill.dice.objects.DCExpression;
import net.nixill.dice.parsing.ExpressionSplitter;
import reactor.core.publisher.Mono;

public class MessageListener implements Consumer<MessageCreateEvent> {
  public void accept(MessageCreateEvent t) {
    Message msg = t.getMessage();
    Optional<String> opt = msg.getContent();

    if (!opt.isPresent()) {
      return;
    }

    String contents = opt.get();

    msg.getChannel().flatMap((channel) -> {
      if (!(channel instanceof MessageChannel)) {
        return Mono.empty();
      }

      MessageChannel chan = (MessageChannel) channel;

      if (contents.startsWith("!r ")) {
        return chan.createMessage("The command has changed to `r!(your roll)` now.");
      }

      if (!contents.startsWith("r!")) {
        return Mono.empty();
      }

      String roll = contents.substring(2);

      if (roll.equals("help")) {
        return chan.createMessage("Coming soon!");
      }

      DCEntity rollEnt = ExpressionSplitter.parse(roll);
      Optional<User> usr = msg.getAuthor();
      String out = "";
      if (usr.isPresent()) {
        out = usr.get().getMention() + ": ";
      } else {
        out = "(no user): ";
      }

      out += "Input: `" + rollEnt.toString() + "`";
      if (rollEnt instanceof DCExpression) {
        out += " / Result: " + rollEnt.getValue().toString();
      }

      return chan.createMessage(out);
    }).subscribe();

  }
}