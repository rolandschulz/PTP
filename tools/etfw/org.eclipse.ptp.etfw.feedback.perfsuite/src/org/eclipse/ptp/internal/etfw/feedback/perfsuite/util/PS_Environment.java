/* $Id: PS_Environment.java,v 1.6 2009/11/05 22:16:20 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2009 The Board of Trustees of the University of Illinois.
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
 * Class to parse and set parameters from environment variables.
 *
 * @author Rui Liu
 */
public class PS_Environment {

    /**
     * Returns the non-nagative integer value
     * from a given environment variable;
     * returns -1 if there is no such variable defined,
     * the value is not an integer, or the value is negative.
     */
    public static int getNonNegativeInteger (String environmentVariable) {
        int ret = -1;
        String envVar = System.getenv (environmentVariable);
        if (envVar != null) {
            try {
                int parsedValue = Integer.parseInt (envVar);
                if (parsedValue >= 0) {
                    ret = parsedValue;
                } else {
                    String errstr = environmentVariable + "=\"" + envVar +
                        "\". Wrong value. Should be >=0.";
                    PS_Debug.print (PS_Debug.WARNING, errstr);
                }
            } catch (NumberFormatException e) {
                String errstr = environmentVariable + "=\"" + envVar +
                    "\". Wrong format. Should be an integer.";
                PS_Debug.print (PS_Debug.WARNING, errstr);
            }
        }
        return ret;
    }

    /**
     * Returns the string value from a given environment variable;
     * returns null if there is no such variable defined.
     */
    public static String getString (String environmentVariable) {
        return System.getenv (environmentVariable);
    }

}
