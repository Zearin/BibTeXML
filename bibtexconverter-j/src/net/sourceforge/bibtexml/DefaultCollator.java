package net.sourceforge.bibtexml;
import java.text.RuleBasedCollator;

public class DefaultCollator extends RuleBasedCollator{
    public DefaultCollator() throws java.text.ParseException{
        super((
                (RuleBasedCollator)
                java.text.Collator.getInstance(java.util.Locale.US)
              ).getRules() +
            "& '|' < ' ' ");
        setStrength(PRIMARY);
    }

}
