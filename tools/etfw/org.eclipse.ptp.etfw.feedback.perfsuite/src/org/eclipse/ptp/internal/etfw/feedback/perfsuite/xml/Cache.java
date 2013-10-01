/* $Id: Cache.java,v 1.33 2012/01/13 20:49:17 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2008-2012 The Board of Trustees of the University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * 	   NCSA - initial API and implementation
 *******************************************************************************/

/*
 * This file is part of PerfSuite.
 */

package org.eclipse.ptp.internal.etfw.feedback.perfsuite.xml;

import java.util.Set;
import java.util.TreeSet;
import org.xml.sax.Attributes;

/**
 * Class representing a cache within a CPU.
 * <p>
 * <code>Cache</code> objects are exposed through a collection contained in
 * a <code>CPUInfo</code> object.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public class Cache {
    private String type;       // instruction, data, unified
    private int level; 
    private int size;          // kilobytes or kuops
    private int linesize;
    private int associativity;
    private String sizeUnits;         // "KB" or "KuOps"
    private String associativityType; // "set" or "full"

    private static final String CACHETAG     = "cache";
    private static final String SIZETAG      = "size";
    private static final String LINESIZETAG  = "linesize";
    private static final String ASSOCTAG     = "associativity";
    private static final String TYPEATT      = "type";
    private static final String LEVELATT     = "level";
    private static final String UNITSATT     = "units";

    // strings for types of cache
    private static final String DCACHE       = "data";
    private static final String ICACHE       = "instruction";
    private static final String ITRACECACHE  = "instruction trace";
    private static final String UCACHE       = "unified";


    static private final Set<String> keySet;

    private String tmpValue = "";

    static {
	keySet = new TreeSet<String>();
	keySet.add (CACHETAG);
	keySet.add (SIZETAG);
	keySet.add (LINESIZETAG);
	keySet.add (ASSOCTAG);
    }

    static boolean containsKey (String str) {
	return keySet.contains (str);
    }

    static Cache newInstance(String type) {
	if (type == null) {
	    return new Cache();
	} else if (type.equalsIgnoreCase (ICACHE)) {
	    return new InstructionCache();
	} else if (type.equalsIgnoreCase (DCACHE)) {
	    return new DataCache();
	} else if (type.equalsIgnoreCase (UCACHE)) {
	    return new UnifiedCache();
	} else if (type.equalsIgnoreCase (ITRACECACHE)) {
	    return new InstructionTraceCache();
	} else {
	    return new Cache();
	}
    }

    Cache () {
    }

    void startElement(String uri, String localName, String qName,
                      Attributes atts) {
	if (qName.equals (CACHETAG)) {
	    type = atts.getValue (TYPEATT);
            String levelStr = atts.getValue (LEVELATT);
            level = Integer.parseInt (levelStr);
            if (level <= 0) {
                String str = "Wrong cache level value '" + levelStr +
                    "' . Should be > 0.";;
                throw new RuntimeException (str);
            }
	} else if (qName.equals (SIZETAG)) {
	    sizeUnits = atts.getValue (UNITSATT);
	} else if (qName.equals (ASSOCTAG)) {
	    associativityType = atts.getValue (TYPEATT);
	}

	tmpValue = "";
    }

    void characters(char[] ch, int start, int length) {
	String tmpString = new String (ch, start, length);
	if (tmpString.length() != 0) {
	    if (tmpValue.length() == 0) {
		tmpValue = tmpString;
	    } else {
		tmpValue = tmpValue.concat (tmpString);
	    }
	}
    }

    void endElement(String uri, String localName, String qName) {

        tmpValue = tmpValue.trim();
	if (qName.equals (SIZETAG)) {
	    size = Integer.parseInt (tmpValue);
            if (size <= 0) {
                String str = "Wrong cache size value '" + tmpValue +
                    "' . Should be > 0.";;
                throw new RuntimeException (str);
            }
	} else if (qName.equals (LINESIZETAG)) {
	    linesize = Integer.parseInt (tmpValue);
            if (linesize <= 0) {
                String str = "Wrong cache linesize value '" + tmpValue +
                    "' . Should be > 0.";;
                throw new RuntimeException (str);
            }
	} else if (qName.equals (ASSOCTAG)) {
	    associativity = Integer.parseInt (tmpValue);
            if (associativity < 0) {
                String str = "Wrong cache associativity value '" + tmpValue +
                    "' . Should be >= 0.";;
                throw new RuntimeException (str);
            }
	}

	tmpValue = "";
    }

    /**
     * Returns a string representation of this cache.
     */
    public String toString() {
	StringBuilder res = new StringBuilder();
	res.append ("size: " + size + " (" + sizeUnits + ")");
	res.append (", associativity: " + associativity +
		    ", associativity type: \"" + associativityType + "\"");
	return res.toString();
    }

    /**
     * Returns the type (&quot;instruction&quot;, &quot;data&quot;,
     * &quot;unified&quot;) within the cache hierarchy of this cache.
     */
    public String getType() { return type; }

    /**
     * Returns the level within the cache hierarchy of this cache.
     */
    public int getLevel() { return level; }

    /**
     * Returns the size of this cache;
     * the units could be either kilobytes or KuOps,
     * the <code>getSizeUnits</code> method can be used to find out the units.
     */
    public int getSize() { return size; }

    /**
     * Returns the units of the size of this cache,
     * one of &quot;KB&quot; or &quot;KuOps&quot;.
     */
    public String getSizeUnits() { return sizeUnits; }

    /**
     * Returns the line size in bytes of this cache.
     */
    // needed by metrics calculation (bandwidth).
    public int getLineSize() { return linesize; }

    /**
     * Returns the degree of associativity of this cache.
     */
    public int getAssociativity() { return associativity; }

    /**
     * Returns the type of associativity
     * (&quot;set&quot; or &quot;full&quot;) of this cache.
     */
    public String getAssociativityType() { return associativityType; }

}
