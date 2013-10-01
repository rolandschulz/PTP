/* $Id: CPUInfo.java,v 1.44 2012/05/11 15:14:36 ruiliu Exp $ */

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

/*
 * This file is part of PerfSuite.
 */

package org.eclipse.ptp.internal.etfw.feedback.perfsuite.xml;

import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 * Class that contains information describing the characteristics of
 * a Central Processing Unit (CPU). The two words &quot;processor&quot;
 * and &quot;CPU&quot; are used exchangeably in the javadoc of this class.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public class CPUInfo {
    private String vendor;
    private String brand;
    private String cpuidInfo;
    private int    revision;
    private float  clockspeed;  // in MHz
    private int    cacheLevels;
    private List<List<Cache>> cacheList;
    private List<List<TLB>> tlbList;

    private static final String VENDORTAG     = "vendor";
    private static final String BRANDTAG      = "brand";
    private static final String CPUIDINFOTAG  = "cpuidinfo";
    private static final String REVISIONTAG   = "revision";
    private static final String CLOCKSPEEDTAG = "clockspeed";
    private static final String CACHEINFOTAG  = "cacheinfo";
    // nothing to parse for this element itself
    private static final String CACHETAG      = "cache";
    private static final String TYPEATT       = "type";
    private static final String LEVELSTAG     = "levels";  // cacheinfo levels
    private static final String TLBINFOTAG    = "tlbinfo"; // nothing to parse
    private static final String TLBTAG        = "tlb";
    // commented out as the checking of maximum is considered unneeded for now
    // private static final int MAX_CACHE_LEVEL = 10;
    // private static final int MAX_TLB_LEVEL = 10;
    // a number used as the limit in sanity checking of cache/TLB level value

    static private final Set<String> keySet;

    private String tmpValue = "";
    private String tmpElementName;
    private boolean inTLBParsing;
    // since the element "associativity" is present in both Cache and TLB,
    // so use this flag to help find correct context
    private Cache tmpCache;
    private TLB tmpTLB;

    static {
	keySet = new TreeSet<String>();
	keySet.add (VENDORTAG);
	keySet.add (BRANDTAG);
	keySet.add (CPUIDINFOTAG);
	keySet.add (REVISIONTAG);
	keySet.add (CLOCKSPEEDTAG);
	keySet.add (LEVELSTAG);
	keySet.add (TLBTAG);
	keySet.add (CACHEINFOTAG);
	keySet.add (TLBINFOTAG);
    }

    CPUInfo () {
        // caches have a <levels> tag in PS generated XML files,
        // however, TLBs don't, so need to handle TLB specially.
        // here we first add one list<TLB> into tlbList,
        // then when we see level attribute = 2 in a TLB element,
        // we add another list<TLB> into it.
	cacheList = new ArrayList<List<Cache>>();
	tlbList = new ArrayList<List<TLB>>();
        tlbList.add (new ArrayList<TLB>());
	inTLBParsing = false;
    }

    static boolean containsKey (String str) {
	return (keySet.contains (str) ||
                Cache.containsKey (str) ||
                TLB.containsKey (str));
    }

    void startElement(String uri, String localName, String qName,
                      Attributes atts) {
	tmpElementName = qName;

	if (qName.equals (CACHETAG)) {
	    tmpCache = Cache.newInstance (atts.getValue (TYPEATT));
	}
 	else if (qName.equals (TLBTAG)) {
	    tmpTLB = TLB.newInstance (atts.getValue (TYPEATT));
 	    inTLBParsing = true;
 	}

	if (Cache.containsKey (qName) && (! inTLBParsing)) {
	    tmpCache.startElement (uri, localName, qName, atts);
	}
 	else if (TLB.containsKey (qName)) {
 	    tmpTLB.startElement (uri, localName, qName, atts);
 	}

	tmpValue = "";
    }

    void characters(char[] ch, int start, int length) {
	if (Cache.containsKey (tmpElementName) && (! inTLBParsing)) {
	    tmpCache.characters (ch, start, length);
	}
 	else if (TLB.containsKey (tmpElementName)) {
 	    tmpTLB.characters (ch, start, length);
 	}
	else {
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
	if (Cache.containsKey (qName) && (! inTLBParsing)) {
	    tmpCache.endElement (uri, localName, qName);
 	} else if (TLB.containsKey (qName)) {
 	    tmpTLB.endElement (uri, localName, qName);
	} else {
	    if (qName.equals (VENDORTAG)) {
		vendor = tmpValue;
	    } else if (qName.equals (BRANDTAG)) {
		brand = tmpValue;
	    } else if (qName.equals (CPUIDINFOTAG)) {
		cpuidInfo = tmpValue;
	    } else if (qName.equals (REVISIONTAG)) {
		revision = Integer.parseInt (tmpValue);
	    } else if (qName.equals (CLOCKSPEEDTAG)) {
		clockspeed = Float.parseFloat (tmpValue);
	    } else if (qName.equals (LEVELSTAG)) {
		cacheLevels = Integer.parseInt (tmpValue);
                if (cacheLevels < 1) {
                    throw new RuntimeException
                        ("Error: cache level is " + cacheLevels +
                         ", it should be >= 1.");
                }
	    }
	}
	if (qName.equals (CACHETAG)) {
            // check nullness of tmpCache
            if (null == tmpCache) {
                throw new RuntimeException
                    ("Error: in parsing \"cache\" tag, tmpCache is null.");
            }
            // check whether level value is larger than current list size,
            // if so, allocate ONE new list, and possibly "null" list in between;
            // otherwise, check if it is null (the null list created in between)
            // -- if so, allocate ONE new list.
            // This is done, so unnecessary memory allocation is avoided.
            int tmpLevel = tmpCache.getLevel();
            if (tmpLevel > cacheList.size()) {
                for (int i = cacheList.size(); i < tmpLevel-1; i++) {
                    cacheList.add (null);
                }
                cacheList.add (new ArrayList<Cache>());
            } else if (null == cacheList.get(tmpLevel-1)) {
                cacheList.set (tmpLevel-1, new ArrayList<Cache>());
            }
            cacheList.get(tmpLevel-1).add (tmpCache);
	}
 	else if (qName.equals (TLBTAG)) {
            // check nullness of tmpTLB
            if (null == tmpTLB) {
                throw new RuntimeException
                    ("Error: in parsing \"TLB\" tag, tmpTLB is null.");
            }
            // validate level value
            int tmpLevel = tmpTLB.getLevel();
            if (tmpLevel < 1) {
                throw new RuntimeException
                    ("Error: tlb level is " + tmpLevel +
                     ", it should be >=1.");
            }
            // check whether level value is larger than current list size,
            // if so, allocate ONE new list, and possibly "null" list in between;
            // otherwise, check if it is null (the null list created in between)
            // -- if so, allocate ONE new list.
            // This is done, so unnecessary memory allocation is avoided.
            if (tmpLevel > tlbList.size()) {
                for (int i = tlbList.size(); i < tmpLevel-1; i++) {
                    tlbList.add (null);
                }
                tlbList.add (new ArrayList<TLB>());
            } else if (null == tlbList.get(tmpLevel-1)) {
                tlbList.set (tmpLevel-1, new ArrayList<TLB>());
            }
            tlbList.get(tmpLevel-1).add (tmpTLB);
 	}

	tmpValue = "";
    }

    /**
     * Returns a string representation of this CPU,
     * including the list of caches and TLBs.
     */
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append ("CPU Information:\n")
            .append ("  [Vendor:     ").append (vendor).append ("]\n");
	if (brand != null) {
	    res.append ("  [Brand:      ").append (brand).append ("]\n");
	}
	if (cpuidInfo != null) {
	    res.append ("  [CPUID info:      ").append (cpuidInfo).append ("]\n");
	}
	res.append ("  [Revision:   ").append (revision).append ("]\n")
            .append ("  [Clockspeed: ").append (clockspeed).append (" (MHz)]\n")
            .append ("  [Cache Levels: ").append (cacheLevels).append ("]\n");
        // Added checking for nullness, as that is possible now that
        // nulls are now possible in cacheList and tlbList.
        for (int i = 0; i < cacheList.size(); i++) {
            if (cacheList.get(i) != null && cacheList.get(i).size() > 0) {
                res.append ("    Level ").append ((i+1)).append (":\n");
                for (Cache tmpc : cacheList.get(i)) {
                    res.append ("  ").append (tmpc);
                }
            }
        }
	res.append ("  [TLB Info]\n");
        for (int i = 0; i < tlbList.size(); i++) {
            if (tlbList.get(i) != null && tlbList.get(i).size() > 0) {
                res.append ("    Level ").append ((i+1)).append (":\n");
                for (TLB tmpt : tlbList.get(i)) {
                    res.append("  ").append (tmpt);
                }
            }
        }
        return res.toString();
    }

    /**
     * Returns the CPU manufacturer.
     */
    public String getVendor() { return vendor; }

    /**
     * Returns the processor brand.
     */
    public String getBrand() { return brand; }

    /**
     * Returns the processor CPUID info.
     */
    public String getCPUIDInfo() { return cpuidInfo; }

    /**
     * Returns the revision (also referred to as &quot;stepping&quot;).
     */
    public int getRevision() { return revision; }

    /**
     * Returns the clock frequency in megahertz.
     */
    public float getClockSpeed() { return clockspeed; }

    /**
     * Returns the levels of caches in this processor.
     */
    public int getCacheLevels() { return cacheLevels; }

    /**
     * Returns the list of list of caches in this processor.
     * The first item in the returned value corresonds to level 1 cache,
     * the second level 2, and so on.
     */
    public List<List<Cache>> getCaches() { return cacheList; }

    /**
     * Returns the list of list of translation lookaside buffers (TLBs)
     * in this processor.
     * The first item in the returned value corresonds to level 1 TLB,
     * the second level 2, and so on.
     */
    public List<List<TLB>> getTLBs() { return tlbList; }

    /**
     * Returns the total cache size of the given cache level in this processor,
     * in units of kilobytes (KB), returns 0 if the given cache level is
     * less than 1, or larger than the maximum level in this processor.
     */
    public int getTotalCacheSizeOfLevel(int level) {
        if (level < 1 || level > cacheList.size()) {
            return 0;
        }
        int size = 0;
	for (Cache cache : cacheList.get(level-1)) {
            if (cache.getSizeUnits().equals ("KB")) {
                size += cache.getSize();
            }
	}
        return size;
    }

}
