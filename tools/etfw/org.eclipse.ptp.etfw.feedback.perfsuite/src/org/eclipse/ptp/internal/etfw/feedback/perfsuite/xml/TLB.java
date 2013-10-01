/* $Id: TLB.java,v 1.35 2012/01/13 20:49:17 ruiliu Exp $ */

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

package org.eclipse.ptp.internal.etfw.feedback.perfsuite.xml;

import java.util.Set;
import java.util.TreeSet;
import org.xml.sax.Attributes;

/**
 * Class representing a translation lookaside buffer (TLB) within a CPU.
 * <p>
 * <code>TLB</code> objects are exposed through a collection contained in
 * a <code>CPUInfo</code> object.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public class TLB {
    private String type;       // "instruction", "data", or "unified"
    private int    level; 
    private int    entries;
    private int    associativity;
    private String associativityType; // "set" or "full"

    //private static final String TLBINFOTAG = "tlbinfo"; // nothing to parse
    private static final String TLBTAG     = "tlb";
    private static final String TYPEATT    = "type";
    private static final String LEVELATT   = "level";
    private static final String ENTRIESTAG = "entries";
    private static final String ASSOCTAG   = "associativity";

    // strings for types of TLB
    private static final String ITLB       = "instruction";
    private static final String DTLB       = "data";
    private static final String UTLB       = "unified";

    private String tmpValue = "";
    static private final Set<String> keySet;

    static {
	keySet = new TreeSet<String>();
	keySet.add (TLBTAG);
	keySet.add (ENTRIESTAG);
	keySet.add (ASSOCTAG);
    }

    static boolean containsKey (String str) {
	return keySet.contains (str);
    }

    static TLB newInstance(String type) {
	if (type == null) {
	    return new TLB();
	} else if (type.equalsIgnoreCase (ITLB)) {
	    return new InstructionTLB();
	} else if (type.equalsIgnoreCase (DTLB)) {
	    return new DataTLB();
	} else if (type.equalsIgnoreCase (UTLB)) {
	    return new UnifiedTLB();
	} else {
	    return new TLB();
	}
    }

    TLB () {
    }

    void startElement(String uri, String localName, String qName,
                      Attributes atts) {
	if (qName.equals (TLBTAG)) {
	    type = atts.getValue (TYPEATT);
	    level = Integer.parseInt (atts.getValue (LEVELATT));
            if (level <= 0) {
                String str = "Wrong TLB level value '" +
                    atts.getValue (LEVELATT) + "' . Should be > 0.";;
                throw new RuntimeException (str);
            }
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
	if (qName.equals (ENTRIESTAG)) {
	    entries = Integer.parseInt (tmpValue);
            if (entries <= 0) {
                String str = "Wrong TLB entries value '" +
                    tmpValue + "' . Should be > 0.";;
                throw new RuntimeException (str);
            }
	} else if (qName.equals (ASSOCTAG)) {
	    // in PerfSuite hwpcreport DTD v0.1 format,
	    // the associativity tag could contain empty value.
	    if (! tmpValue.equals("")) {
		associativity = Integer.parseInt (tmpValue);
                if (associativity < 0) {
                    String str = "Wrong TLB associativity value '" +
                        tmpValue + "' . Should be >= 0.";;
                    throw new RuntimeException (str);
                }
	    }
	}
	
	tmpValue = "";
    }

    /**
     * Returns a string representation of this TLB.
     */
    public String toString() {
	StringBuilder res = new StringBuilder();
	res.append ("entries: " + entries);
	res.append (", associativity: " + associativity);
	res.append (", associativity type: \"" + associativityType + "\"");
	return res.toString();			  
    }

    /**
     * Returns the type of this TLB, one of
     * &quot;instruction&quot;, &quot;data&quot;, or &quot;unified&quot;.
     */
    public String getType() { return type; }

    /**
     * Returns the level within the TLB hierarchy at which this
     * TLB resides.
     */
    public int getLevel() { return level; }

    /**
     * Returns the number of entries in this TLB.
     */
    public int getEntries() { return entries; }

    /**
     * Returns the degree of associativity of this TLB.
     */
    public int getAssociativity() { return associativity; }

    /**
     * Returns the type of associativity of this TLB,
     * one of &quot;set&quot; or &quot;full&quot;.
     */
    public String getAssociativityType() { return associativityType; }

}
