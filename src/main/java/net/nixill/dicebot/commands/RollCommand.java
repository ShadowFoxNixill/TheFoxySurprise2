package net.nixill.dicebot.commands;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.GuildMessageChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import net.nixill.dice.exception.DiceCalcException;
import net.nixill.dice.exception.UserInputException;
import net.nixill.dice.objects.DCEntity;
import net.nixill.dice.objects.DCExpression;
import net.nixill.dice.objects.DCFunction;
import net.nixill.dice.objects.DCList;
import net.nixill.dice.objects.DCListExpression;
import net.nixill.dice.objects.DCOperation;
import net.nixill.dice.objects.DCValue;
import net.nixill.dice.objects.Randomizer;
import net.nixill.dice.operations.FunctionHistory;
import net.nixill.dice.operations.FunctionHistory.HistoryEntry;
import net.nixill.dice.operations.Functions;
import net.nixill.dice.parsing.ExpressionSplitter;
import net.nixill.dicebot.Config;
import net.nixill.dicebot.WrappedException;

public class RollCommand {
  private static Pattern ptnSeed = Pattern.compile("(\\d{1,19}) (.+)");
  
  public static String of(Message msg, Channel channel,
      Matcher rollMatcher, String args) {
    String command = rollMatcher.group();
    
    // See whether the roll is a "full" or "debug" roll, or neither.
    DebugLevel lev = DebugLevel.NONE;
    if (rollMatcher.group(1) != null) {
      lev = DebugLevel.ROLLS;
    } else if (rollMatcher.group(2) != null) {
      lev = DebugLevel.ALL;
    }
    
    // Check whether there's a seed to be provided.
    boolean seeded = false;
    if (rollMatcher.group(3) != null) {
      seeded = true;
    }
    
    // Also make sure args were actually provided.
    // This is after the seeded check so that we return the right usage.
    if (args == null) {
      String out = "Usage: !" + command + " ";
      if (seeded) {
        out += "<seed> ";
      }
      return out + "<roll>";
    }
    
    // Now get the seed
    long seed;
    if (seeded) {
      Matcher seedMatcher = ptnSeed.matcher(args);
      if (!seedMatcher.matches()) {
        return "Usage: !" + command + " <seed> <roll>";
      }
      
      seed = Long.parseLong(seedMatcher.group(1));
      args = seedMatcher.group(2);
    } else {
      seed = msg.getId().asLong();
    }
    
    Randomizer.setSeed(seed);
    
    // Who did it?
    Optional<User> usr = msg.getAuthor();
    String out = "";
    
    if (usr.isPresent()) {
      out = usr.get().getMention() + ": ";
      Functions.setThread("user", usr.get().getId().toString());
    } else {
      out = "(no user): ";
      Functions.setThread("user", "");
    }
    
    // Where was it?
    String locationId = null;
    MessageChannel chan = msg.getChannel().block();
    if (chan instanceof GuildMessageChannel) {
      GuildMessageChannel ch = (GuildMessageChannel) chan;
      locationId = ch.getGuildId().asString();
    } else {
      locationId = chan.getId().asString();
    }
    Functions.setThread("server", locationId);
    
    // Let's actually work this out
    try {
      DCEntity rollEnt = ExpressionSplitter.parse(args);
      DCEntity result = rollEnt;
      out += "Input: ";
      
      // If the user made a calculation, give it to them as we saw it
      // Which should be pretty similar to how they entered it
      if (result instanceof DCExpression) {
        out += "`" + result.toCode() + "` / Result: ";
        result = rollEnt.getValue();
      }
      
      // Now give them the result
      // Let's bold the actual number cause it looks nicer
      if (result instanceof DCList) {
        String res = result.toString(2);
        int space = res.indexOf(' ');
        out += "**" + res.substring(0, space) + "**"
            + res.substring(space);
      } else {
        out += "**" + result.toString() + "**";
      }
      
      // Let's add history if it was requested
      if (lev != DebugLevel.NONE) {
        ArrayList<HistoryEntry> list = FunctionHistory.getList();
        for (HistoryEntry entry : list) {
          if (entry.level == 1 || lev == DebugLevel.ALL) {
            out += "\n" + entry.text;
          }
        }
      }
      
      // We should also save the results
      if (!containsLast(rollEnt)) {
        Functions.save2("_ans", result);
        Functions.save2("_last", rollEnt);
      }
    } catch (UserInputException ex) {
      out += "Error: " + ex.getMessage() + "\nAt position: "
          + ex.getPosition();
      if (Config.isDebug()) {
        ex.printStackTrace();
      }
    } catch (DiceCalcException ex) {
      out += "Error: " + ex.getMessage();
      if (Config.isDebug()) {
        ex.printStackTrace();
      }
    } catch (Throwable ex) {
      if (ex instanceof WrappedException) {
        ex = ex.getCause();
      }
      
      out += "Error: " + ex.getMessage()
          + "\nError reporting is not enabled yet. However, you can still raise"
          + " an issue at <https://github.com/ShadowFoxNixill/TheFoxySurprise2/issues>.";
      if (Config.isDebug()) {
        ex.printStackTrace();
      }
    }
    
    return out;
  }
  
  public static enum DebugLevel {
    NONE,
    ROLLS,
    ALL;
  }
  
  private static boolean containsLast(DCEntity ent) {
    if (ent == null) {
      return false;
    }
    
    if (ent instanceof DCValue) {
      return false;
    }
    
    if (ent instanceof DCFunction) {
      DCFunction func = (DCFunction) ent;
      if (func.getName().equals("_last")
          || func.getName().equals("_ans")) {
        return true;
      } else {
        for (DCEntity param : func.getParams()) {
          if (containsLast(param)) {
            return true;
          }
        }
      }
    } else if (ent instanceof DCListExpression) {
      DCListExpression list = (DCListExpression) ent;
      for (DCEntity item : list.getItems()) {
        if (containsLast(item)) {
          return true;
        }
      }
    } else if (ent instanceof DCOperation) {
      DCOperation oper = (DCOperation) ent;
      return containsLast(oper.getLeft()) || containsLast(oper.getRight());
    }
    
    return false;
  }
}