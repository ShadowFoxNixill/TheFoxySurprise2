package net.nixill.dicebot.commands;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import net.nixill.dice.exception.DiceCalcException;
import net.nixill.dice.exception.UserInputException;
import net.nixill.dice.objects.DCEntity;
import net.nixill.dice.objects.DCExpression;
import net.nixill.dice.objects.DCList;
import net.nixill.dice.parsing.ExpressionSplitter;
import net.nixill.dicebot.App;

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
    
    // Who did it?
    Optional<User> usr = msg.getAuthor();
    String out = "";
    
    if (usr.isPresent()) {
      out = usr.get().getMention() + ": ";
    } else {
      out = "(no user): ";
    }
    
    // Let's actually work this out
    try {
      DCEntity rollEnt = ExpressionSplitter.parse(args);
      DCEntity next = rollEnt;
      out += "Input: ";
      
      // If the user made a calculation, give it to them as we saw it
      // Which should be pretty similar to how they entered it
      if (next instanceof DCExpression) {
        out += "`" + next.toCode() + "` / Result: ";
        next = rollEnt.getValue();
      }
      
      // Now give them the result
      // Let's bold the actual number cause it looks nicer
      if (next instanceof DCList) {
        String res = next.toString(2);
        int space = res.indexOf(' ');
        out += "**" + res.substring(0, space) + "**"
            + res.substring(space);
      } else {
        out += "**" + next.toString() + "**";
      }
    } catch (UserInputException ex) {
      out += "Error: " + ex.getMessage() + "\nAt position: "
          + ex.getPosition();
      if (App.isDebug()) {
        ex.printStackTrace();
      }
    } catch (DiceCalcException ex) {
      out += "Error: " + ex.getMessage();
      if (App.isDebug()) {
        ex.printStackTrace();
      }
    } catch (Exception ex) {
      out += "Error: " + ex.getMessage()
          + "\nError reporting is not enabled yet. However, you can still raise"
          + " an issue at <https://github.com/ShadowFoxNixill/TheFoxySurprise2/issues>.";
      if (App.isDebug()) {
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
}