/* $Id: PS_Debug.java,v 1.16 2011/04/12 16:37:20 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2009-2011 The Board of Trustees of the University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * 	   NCSA - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.etfw.feedback.perfsuite.util;

/**
 * A class to print debug messages, and get and set debug level.
 * <p>
 * The prefix string, which occurs at the beginning of the debug output,
 * is configurable through the environment variable
 * <code>PS_DEBUG_PREFIX</code>. It has a default value of "PS_DEBUG".
 * <p>
 * The separator string, which separates the prefix, debug level string,
 * and the given debug message string,
 * is configurable through the environment variable
 * <code>PS_DEBUG_SEPARATOR</code>. It has a default value of ":".
 *
 * @author Rui Liu
 */

public class PS_Debug {
    private static int level = 0;
    public  static String prefix = "PS_DEBUG";
    private static String separator = ":";
    public  static final int OFF     = 0;
    public  static final int FATAL   = 1;
    public  static final int WARNING = 2;
    public  static final int INFO    = 3;
    public  static final int VERBOSE = 4;
    private static int CALLER_SFN = 3;
    /* CALLER_SFN:
     *   The proper value of Caller's Stack Frame Number in the
     *   fileName(), methodName() and lineNumber() methods.
     *   Observations of values:
     *       Sun 1.6 JVM: 3,
     *       Sun 1.5 and IBM 1.6 JVMs: 4.
     */
    static {
        int lev = PS_Environment.getNonNegativeInteger ("PS_DEBUG");
        if (lev > 0) {
            // Temporarily raise it in this block,
            // so the messages in this block can be printed out using print().
            level = VERBOSE + 1;
            String var;
            boolean sfn_found_at_runtime = false;

            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            for (int i = 0; i < st.length; i++) {
                if (st[i].getClassName().equals ("org.eclipse.ptp.internal.etfw.feedback.perfsuite.util.PS_Debug")
                    && st[i].getMethodName().equals ("<clinit>")) {
                    CALLER_SFN = i + 2;
                    sfn_found_at_runtime = true;
                    break;
                }
                /* Use "<clinit>", since we are in the <clinit> method now.
                 * Use "+ 2", to account for the 2 calls to PS_Debug.
                 *     print(...), which then calls
                 *     fileName(), or methodName(), or lineNumber().
                 */
            }

            var = "PS_DEBUG_PREFIX";
            String pre = PS_Environment.getString (var);
            if (pre != null) {
                prefix = pre;
                print (PS_Debug.INFO, 
                       "Setting debug prefix to \"" + prefix +
                       "\" according to the environment variable " + var);
            }

            var = "PS_DEBUG_SEPARATOR";
            String sep = PS_Environment.getString (var);
            if (sep != null) {
                separator = sep;
                print (PS_Debug.INFO, 
                       "Setting debug separator to " + separator +
                       " according to the environment variable " + var);
            }

            var = "PS_DEBUG";
            print (PS_Debug.INFO, 
                   "Setting debug level to " + lev +
                   " according to the environment variable " + var);
            level = lev;

            if (sfn_found_at_runtime) {
                print (PS_Debug.VERBOSE, "CALLER_SFN = " + CALLER_SFN
                       + ", found at run time.");
            } else {
                print (PS_Debug.VERBOSE, "CALLER_SFN = " + CALLER_SFN
                       + ", using the pre-defined value.");
            }
        }
    }

    private static String levelString(int level) {
        String levelString = null;
        if (FATAL == level) {
            levelString = "FATAL";
        } else if (WARNING == level) {
            levelString = "WARNING";
        } else if (INFO == level) {
            levelString = "INFO";
        } else if (level >= VERBOSE) {
            levelString = "VERBOSE";
        }
        return levelString;
    }


    /**
     * Sets debug level to given value.
     */
    public static void setLevel(int lev) { level = lev; }

    /**
     * Returns the debug level as set by the environment variable
     * PS_DEBUG; 0 means no debug information, positive integer
     * means to print out debug information.
     */
    public static int getLevel() { return level; }

    public static String getPrefix () { return prefix; }
    public static void setPrefix (String pre) { prefix = pre; }

    /**
     * Prints given debug message to standard out
     * if the current debug level is no lower than the given level;
     * does not print if given level is 0 or negative.
     */
    public static void print (int lev, String msg) {
        if ( (level < lev) || (lev <= 0) ) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        System.out.println
            (sb.append (prefix).append (separator)
             .append (levelString(lev)).append (separator)
             .append (fileName()).append (separator)
             .append (methodName()).append (separator)
             .append (lineNumber()).append (separator)
             .append (msg)
             .toString());
    }

    /**
     * Prints given message to standard error,
     * and exit with code 1.
     *
     * Per Rick's good suggestion, print the debug info
     * (file name, method name, line number) only when the
     * debug level is set to "> OFF (0)" (i.e., FATAL and above).
     */
    public static void errorExit (String msg) {
        String separator = ":";
        StringBuilder sb = new StringBuilder();
        sb.append ("Error: ");
        if (level > OFF) {
            sb.append (fileName()).append (separator)
                .append (methodName()).append (separator)
                .append (lineNumber()).append (separator);
        }
        sb.append (msg)
            .append (".  Exiting...");
        System.err.println (sb.toString());
        System.exit (1);
    }

    private static String fileName() {
        return Thread.currentThread().getStackTrace()[CALLER_SFN]
            .getFileName();
    }
    private static String methodName() {
        return Thread.currentThread().getStackTrace()[CALLER_SFN]
            .getMethodName();
    }
    private static int lineNumber() {
        return Thread.currentThread().getStackTrace()[CALLER_SFN]
            .getLineNumber();
    }


}
