package test.metric.drakklord.teststudio.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by DrakkLord on 2016. 11. 07..
 */
public class BasicRules {

    private Object baz;
    private static String ip = "127.0.0.1"; // This is a really bad idea !

    static {
        // empty
    }

    Boolean bar = new Boolean("true"); // just do a Boolean bar = Boolean.TRUE;
    Boolean buz = Boolean.valueOf(false); // just do a Boolean buz = Boolean.FALSE;

    public void emptyCatchAndTryAndFinallyBlock() {
        try {
        } catch (Exception e) {
        } finally {
        }
    }

    public void emptyIfBlock(boolean param1) {
        if (param1) {
        }
    }

    public void jumbledIncrementer() {
        for (int i = 0; i < 10; i++) {
            for (int k = 0; k < 20; i++) {
                System.out.println("Hello");
            }
        }
    }

    public void emptyWhile(int a, int b) {
        while (a == b) {
            // empty!
        }
    }

    public void emptySwitch() {
        int x = 2;
        switch (x) {
        }
    }

    public void forLoopShouldByWhileLoop() {
        for (;true;) {
        }
    }

    public String unnecesaryConversion(int x) {
        String foo = new Integer(x).toString();
        return Integer.toString(x);
    }

    // OverrideBothEqualsAndHashcode
    public boolean equals(Object other) {
        return false;
    }

    public int hashCode() {
        return 0;
    }

    public Object doubleCheckedLocking() {
        if(baz == null) { //baz may be non-null yet not fully created
            synchronized(this){
                if(baz == null){
                    baz = new Object();
                }
            }
        }
        return baz;
    }

    public String returnFromFinallyBlock() {
        try {
            throw new Exception( "My Exception" );
        } catch (Exception e) {
            throw e;
        } finally {
            return "A. O. K."; // Very bad.
        }
    }

    public void emptySynchronizedBlock() {
        synchronized (this) {
            // empty!
        }
    }

    public void unnecessaryReturn() {
        return;
    }

    public void unconditionalIfStatement() {
        if (true) {
            // ...
        }
    }

    public void extraSemicolon() {
        // this is probably not what you meant to do
        ;
        // the extra semicolon here this is not necessary
        System.out.println("look at the extra semicolon");;
        ;
    }

    private final void unnecessaryFinal() {
    }

    public void collapsibleIfStatements(boolean x, boolean y) {
        if (x) {
            if (y) {
                // do stuff
            }
        }
    }

    public void miplacedNullCheck(Object a) {
        if (a.equals(baz) && a != null) {

        }
    }

    public String brokenNullCheck(String string) {
        // should be &&
        if (string!=null || !string.equals(""))
            return string;
        // should be ||
        if (string==null && string.equals(""))
            return string;
        return null;
    }
}
