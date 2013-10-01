/* $Id: MachineInfo.java,v 1.30 2012/01/13 20:49:17 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2008-2009, 2011-2012 The Board of Trustees of
 * the University of Illinois.
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

import java.util.Set;
import java.util.TreeSet;
import org.xml.sax.Attributes;

/**
 * Class containing information describing characteristics of a particular
 * computer, or machine.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public class MachineInfo {

    // data fields
    private CPUInfo cpuinfo;
    private int     cpucount;
    private float   memorysize;
    private float   syspagesize;

    // xml parsing helper fields
    private String tmpValue = "";
    private String tmpElementName;

    private static final String CPUCOUNTTAG    = "cpucount";
    private static final String MEMORYSIZETAG  = "memoryinfo";
    private static final String SYSPAGESIZETAG = "syspagesize";

    static private final Set<String> keySet;

    static {
	keySet = new TreeSet<String>();
	keySet.add (CPUCOUNTTAG);
	keySet.add (MEMORYSIZETAG);
	keySet.add (SYSPAGESIZETAG);
    }

    MachineInfo () {
	cpuinfo = new CPUInfo();
    }

    static boolean containsKey (String str) {
	return (keySet.contains (str) || CPUInfo.containsKey (str));
    }

    void startElement(String uri, String localName, String qName,
                      Attributes atts) {
	tmpElementName = qName;

	if (CPUInfo.containsKey (qName)) {
	    cpuinfo.startElement (uri, localName, qName, atts);
	}

	tmpValue = "";
    }

    void characters(char[] ch, int start, int length) {
	if (CPUInfo.containsKey (tmpElementName)) {
	    cpuinfo.characters (ch, start, length);
	} else {
	    String tmpString = new String (ch, start, length);
	    if (tmpString.length() != 0) {
		if (tmpValue.length() == 0) {
		    tmpValue = tmpString;
		} else {
		    tmpValue = tmpValue.concat (tmpString);
		}
	    }
	}
    }

    void endElement(String uri, String localName, String qName) {

        tmpValue = tmpValue.trim();
	if (CPUInfo.containsKey (qName)) {
	    cpuinfo.endElement (uri, localName, qName);
	} else {
	    if (qName.equals (CPUCOUNTTAG)) {
		cpucount = Integer.parseInt (tmpValue);
	    } else if (qName.equals (MEMORYSIZETAG)) {
		memorysize = Float.parseFloat (tmpValue);
	    } else if (qName.equals (SYSPAGESIZETAG)) {
		syspagesize = Float.parseFloat (tmpValue);
	    }
	}

	tmpValue = "";
    }

    /**
     * Returns a string representation of this machine information object,
     * including the number of CPUs, total memory size, system page size,
     * and the detailed information of the CPUs.
     */
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append ("Machine Information:\n");
	res.append ("  [CPUs:        " + cpucount + "]\n" +
		    "  [Memory:      " + memorysize + " (MB)]\n" +
		    "  [Page:        " + syspagesize + " (KB)]\n");
        return res.toString() + cpuinfo.toString();
    }

    /**
     * Returns a <code>CPUInfo</code> object
     * describing the characteristics of the CPUs on this machine.
     */
    public CPUInfo getCPUInfo() { return cpuinfo; }

    /**
     * Returns the number of CPUs on this machine.
     */
    public int getCPUCount() { return cpucount; }

    /**
     * Returns the amount of installed memory on this machine,
     * in units of megabytes (MB).
     */
    public float getMemorySize() { return memorysize; }

    /**
     * Returns the operating system (OS) page size, in units
     * of kilobytes (KB).
     */
    public float getSysPageSize() { return syspagesize; }

}
