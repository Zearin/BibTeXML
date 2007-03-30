package net.sourceforge.bibtexml;

import javax.swing.*;
import java.awt.*;

public class SchemaSelection{
    public enum DataTypes { Strict, Lax };
    public enum Fields { Core, User, Arbitrary };
    public enum Structure { Flat, Container };
    
    private final ButtonGroup datatypes = new ButtonGroup();
    private final ButtonGroup fields = new ButtonGroup();
    private final ButtonGroup structure = new ButtonGroup();
    
    private final Container cp;
    
    public SchemaSelection(){
        cp = Box.createVerticalBox();
        
        Container c = Box.createHorizontalBox(); 
        c.add(new JLabel("BibTeX Fields", JLabel.LEFT));
        c.add(Box.createHorizontalGlue());
        cp.add(c);
        
        c = new JPanel(new GridLayout(4, 4));
        JRadioButton button;
        
        c.add(new JPanel());
        JLabel label;
        label = new JLabel("Required");
        label.setBorder(BorderFactory.createLineBorder(Color.black));
        c.add(label);
        label = new JLabel("Optional");
        label.setBorder(BorderFactory.createLineBorder(Color.black));
        c.add(label);
        label = new JLabel("User-Defined");
        label.setBorder(BorderFactory.createLineBorder(Color.black));
        c.add(label);
            
        button = new JRadioButton();
        button.setActionCommand(Fields.Core.name());
        button.setHorizontalAlignment(JRadioButton.CENTER);
        button.setBorder(BorderFactory.createLineBorder(Color.black));
        datatypes.add(button);
        c.add(button);
        c.add(new JLabel("X", JLabel.CENTER));
        c.add(new JLabel("?", JLabel.CENTER));
        c.add(new JLabel("-", JLabel.CENTER));
        
        button = new JRadioButton();
        button.setActionCommand(Fields.User.name());
        button.setHorizontalAlignment(JRadioButton.CENTER);
        button.setBorder(BorderFactory.createLineBorder(Color.black));
        datatypes.add(button);
        c.add(button);
        c.add(new JLabel("X", JLabel.CENTER));
        c.add(new JLabel("?", JLabel.CENTER));
        c.add(new JLabel("?", JLabel.CENTER));
        
        button = new JRadioButton();
        button.setActionCommand(Fields.Arbitrary.name());
        button.setHorizontalAlignment(JRadioButton.CENTER);
        button.setBorder(BorderFactory.createLineBorder(Color.black));
        datatypes.add(button);
        c.add(button);
        c.add(new JLabel("?", JLabel.CENTER));
        c.add(new JLabel("?", JLabel.CENTER));
        c.add(new JLabel("?", JLabel.CENTER));
        cp.add(c);
        
        c = Box.createHorizontalBox();
        Container c2 = Box.createVerticalBox();
        c2.add(new JLabel("X : must be present", JLabel.LEFT));
        c2.add(new JLabel("? : can be present", JLabel.LEFT));
        c2.add(new JLabel("- : must not be present", JLabel.LEFT));
        c.add(c2);
        c.add(Box.createHorizontalGlue());
        cp.add(c);
        
    }
    
    public Fields showDialog(Component parent){
        int res = JOptionPane.showConfirmDialog(
            parent, cp, "Validation options", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE, null);
        Fields result = null;
        if(res == JOptionPane.OK_OPTION){
            ButtonModel model = datatypes.getSelection();
            if(model != null){
                result = Enum.valueOf(Fields.class, model.getActionCommand());
            }
        }
        return result;
    }
    
    public static void main(String[] argv){
        SchemaSelection s = new SchemaSelection();
        System.out.println(s.showDialog(null));
    }
    
}
