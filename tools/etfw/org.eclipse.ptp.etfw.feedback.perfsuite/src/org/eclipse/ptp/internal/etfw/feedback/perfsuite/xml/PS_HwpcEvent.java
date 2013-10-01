/* $Id: PS_HwpcEvent.java,v 1.9 2009/03/25 15:17:46 ruiliu Exp $ */

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
 * Class encapsulating hardware performance counter events
 * information contained in a PerfSuite hardware
 * performance report XML document gathered in &quot;counting&quot; mode.
 *
 * @author Rui Liu
 */
public class PS_HwpcEvent {
    private final String name;  // event name
    private final String type;  // "preset" or "native"
    private final boolean derived; // "yes" or "no"
    private long count;  // count of this event
    private final String domain; // user, kernel, all
    private final String className;
    // name: PAPI, itimer, profil, perfmon, null      @@ itimer and profil?
    private final String classVersion;

    // used in PS_HwpcCountingReport
    PS_HwpcEvent (String name, String type, String derived) {
	this.name = name;
	this.type = type;
	this.derived = (derived.equals ("yes"));
        this.domain = null;
        this.className = null;
        this.classVersion = null;
    }
    void setCount (long count) { this.count = count; }

    // used in PS_HwpcProfileReport
    PS_HwpcEvent (String name, String className, String domain,
                  String classVersion, String type) {
	this.name = name;
        this.className = className;
        this.domain = domain;
        this.classVersion = classVersion;
	this.type = type;
        this.derived = false;
    }

    /** Returns the name of this event. */
    public String  getName() { return name; }

    /** Returns the type of this event,
     * one of &quot;preset&quot; or &quot;native&quot;.
     */
    public String  getType() { return type; }

    /** Returns a flag indicating whether this event is a derived event. */
    public boolean getDerived() { return derived; }

    /** Returns the count for the occurrance of this event
     * during the program execution.
     */
    public long    getCount() { return count; }

    /**
     * Returns the name of the general &quot;class&quot;
     * of the measurement, one of: &quot;null&quot;,
     * &quot;PAPI&quot;, or &quot;perfmon&quot;.
     * This identifies the underlying performance software support.
     */
    public String getClassName() { 
        return className;
    }

    /**
     * Returns the version of the underlying performance software;
     * returns <code>null</code>
     * if the version string is absent in the input XML file.
     */
    public String getClassVersion() {
        return classVersion;
    }

    /**
     * Returns the event domain in effect during the measurement;
     * one of &quot;user&quot;, &quot;kernel&quot; or &quot;all&quot;;
     * returns <code>null</code>
     * if the domain string is absent in the input XML file.
     */
    public String getEventDomain() {
        return domain;
    }

    /** Returns a string representation of this event. */
    public String toString() {
	StringBuilder res = new StringBuilder();
	res.append ("Event: " + name);
        if (className != null)
            res.append (", Class name: " + className);
        if (classVersion != null)
            res.append (", Class version: " + classVersion);
        if (domain != null)
            res.append (", Event domain: " + domain);
        if (type != null)
            res.append (", Type: " + type);
        if (count != 0)
            res.append (", Count: " + count);
        res.append (", Derived: " + derived);
	return res.toString();
    }
}
