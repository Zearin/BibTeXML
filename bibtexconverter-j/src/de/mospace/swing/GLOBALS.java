package de.mospace.swing;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;

/** Provides static constants for the application. */
public final class GLOBALS{
    private static ResourceBundle bundle = ResourceBundle.getBundle("de.mospace.swing.DefaultResourceBundle");
    private static Collection<String> bundleKeys = Collections.list(bundle.getKeys());

    private GLOBALS(){
        // explicit default constructor.
    }

    public static String getMessage(String key, Object arg){
        return MessageFormat.format(GLOBALS.getString(key), new Object[]{arg});
    }

    public static String getMessage(String key, Object[] args){
        return MessageFormat.format(GLOBALS.getString(key), args);
    }

    public static void setResourceBundle(ResourceBundle rb){
        bundle = rb;
        bundleKeys = Collections.list(rb.getKeys());
    }

    public static Object get(String s){
        return bundleKeys.contains(s)? bundle.getString(s) : s;
    }

    public static String getString(String s){
        return (String) get(s);
    }

    public static int getKey(String s){
        return getString(s).charAt(0);
    }

    public static Font getSuitableFont(Font f, char[] characters){
        /* try the provided font */
        if(canDisplay(f, characters)){
            return f;
        }

        /* then try Lucida Sans Regular which is
        bundled with Sun JREs since 1.4 */
        Font myFont = new Font("Lucida Sans Regular",
        Font.PLAIN, f.getSize());

        /* test if the font we got (if Lucida Sans Regular
        is not installed it will be another font) can
        really display the characters */
        if(!canDisplay(myFont, characters)){

            /* if not we try all available fonts
            one after the other */
            Font[] ff = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getAllFonts();

            int i = 0;
            for(; i<ff.length; i++){

                /* if we have found a suitable font
                we use it. */
                if(canDisplay(ff[i], characters)){
                    myFont = ff[i].deriveFont(f.getSize());
                }
            }

            /* if we have not found a suitable font
            we use the default font */
            if(i == ff.length){
                myFont = f;
            }
        }
        return myFont;
    }

    private static boolean canDisplay(Font f, char[] characters){
        boolean result = true;
        for(int i=0; i<characters.length; i++){
            if(!f.canDisplay(characters[i])){
                result = false;
                break;
            }
        }
        return result;
    }
}
