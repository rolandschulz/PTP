/* $Id: CPUTime.java,v 1.12 2009/03/25 15:17:46 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2008-2009 The Board of Trustees of the University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * 	   NCSA - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.etfw.feedback.perfsuite.xml;

/**
 * Class representing a measurement of CPU time, broken down into execution
 * time in user mode and in system mode.
 *
 * @author Rick Kufrin
 */

public class CPUTime {
    private float user;
    private float system;

    CPUTime(float u, float s) {
        user = u;
        system = s;
    }

    /** Returns the user time component in seconds. */
    public float getUserTime() { return user; }

    /** Returns the system time component in seconds. */
    public float getSystemTime() { return system; }

    /** Returns the sum of user and system time in seconds. */
    public float getTotalTime() { return user + system; }

    /**
     * Returns a string representation of this CPU time,
     * including user time and system time.
     */
    public String toString() {
        return "  [CPU time:    user=" + user + " system=" + system +
            " (seconds)]\n";
    }
}

