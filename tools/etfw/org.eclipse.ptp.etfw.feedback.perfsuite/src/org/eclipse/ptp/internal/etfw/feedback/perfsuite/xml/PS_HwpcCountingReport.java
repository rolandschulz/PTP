/* $Id: PS_HwpcCountingReport.java,v 1.29 2012/01/13 20:49:17 ruiliu Exp $ */

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

import org.xml.sax.Attributes;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;

/**
 * Class encapsulating information contained in a PerfSuite hardware
 * performance report XML document gathered in &quot;counting&quot; mode.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public class PS_HwpcCountingReport extends PS_HwpcReport {

    private static final String EDATATAG        = "hwpceventdata";
    private static final String EVENTTAG        = "hwpcevent";
    private static final String NAMEATT         = "name";
    private static final String TYPEATT         = "type";
    private static final String DERIVEDATT      = "derived";
    private static final String CLASSATT        = "class";
    private static final String CLASSVERSIONATT = "version";
    private static final String DOMAINATT       = "domain";
    private static final String MPXATT          = "multiplexed";
    private static final String MPXUSATT        = "multiplex_usec";

    private static final long DEFAULT_MPX_USEC = 10000;

    private ExecutionInfo executionInfo;
    private MachineInfo machineInfo;
    private String className;
    private String classVersion;
    private String domain;

    private boolean multiplexed;
    private long multiplex_usec = DEFAULT_MPX_USEC;
    private Map<String, PS_HwpcEvent> eventMap;

    private String tmpValue = "";
    private String tmpElementName;
    private PS_HwpcEvent tmpHwpcEvent;

    PS_HwpcCountingReport () {
	executionInfo = new ExecutionInfo();
	machineInfo = new MachineInfo();
    }


    void startElement(String uri, String localName, String qName,
                      Attributes atts) {

	tmpElementName = qName;

	if (ExecutionInfo.containsKey (qName)) {
	    executionInfo.startElement (uri, localName, qName, atts);
	} else if (MachineInfo.containsKey (qName)) {
	    machineInfo.startElement (uri, localName, qName, atts);
	} else {
	    // parse the tags specific to this class (PS_HwpcCountingReport):
	    //   hwpceventdata
	    //   hwpcevent
	    String tagname = qName.trim();

	    if (tagname.equals (EDATATAG)) {
		className = atts.getValue (CLASSATT);
		classVersion = atts.getValue (CLASSVERSIONATT); // could be absent
		domain = atts.getValue (DOMAINATT);
		multiplexed = (atts.getValue(MPXATT).equals ("yes"));
		if (atts.getValue (MPXUSATT) != null) {
		    multiplex_usec = Long.parseLong (atts.getValue (MPXUSATT));
		}
	    } else if (tagname.equals (EVENTTAG)) {
		if (eventMap == null) {
		    eventMap = new TreeMap<String, PS_HwpcEvent>();
		}
		tmpHwpcEvent = new PS_HwpcEvent
                    (atts.getValue (NAMEATT), atts.getValue (TYPEATT),
                     atts.getValue (DERIVEDATT));
	    }

            tmpValue = "";
	}

    }

    void characters(char[] ch, int start, int length) {
	if (tmpElementName == null) {
	    // this is to deal with the case in multi report,
	    // where character data ("\n  ") appear before any hwpc report element
	    return;
	}
	if (ExecutionInfo.containsKey (tmpElementName)) {
	    executionInfo.characters(ch, start, length);
	} else if (MachineInfo.containsKey (tmpElementName)) {
	    machineInfo.characters(ch, start, length);
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
	if (ExecutionInfo.containsKey (qName)) {
	    executionInfo.endElement (uri, localName, qName);
	} else if (MachineInfo.containsKey (qName)) {
	    machineInfo.endElement (uri, localName, qName);
	} else {
	    if (qName.equals (EVENTTAG)) {
		tmpHwpcEvent.setCount (Long.parseLong (tmpValue));
		eventMap.put (tmpHwpcEvent.getName(), tmpHwpcEvent);
	    }
	}

	tmpValue = "";
    }
	

    /**
     * Returns a string representation of this hwpc counting report.
     */
    public String toString() {
        StringBuilder res = new StringBuilder();
	res.append (super.toString());

	res.append (executionInfo.toString());
	res.append (machineInfo.toString());

        res.append("Event Information\n");
        res.append("  [Class:       " + className + "]\n");
	if (classVersion != null) {
	    res.append("  [Version:     " + classVersion + "]\n");
	}
        res.append("  [Domain:      " + domain + "]\n");
        res.append("  [Multiplexed: " + multiplexed + "]\n");
	if (multiplex_usec != 0) {
	    res.append ("  [Multiplex interval: " +
                        multiplex_usec +" (micro-seconds)]\n");
	}

        res.append("Event Counts (" + eventMap.size() + " events)\n");
        for (Map.Entry<String,PS_HwpcEvent> entry : eventMap.entrySet()) {
	    res.append("  [" + entry.getValue() + "]\n");
        }
        return res.toString();
    }


    /**
     * Returns a map of performance event name/value pairs.
     * The key is the name of the event, and the value is of type PS_HwpcEvent.
     */
    public Map<String,PS_HwpcEvent> getEvents() { return eventMap; }

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
     * one of &quot;user&quot;, &quot;kernel&quot; or &quot;all&quot;.
     */
    public String getEventDomain() {
        return domain;
    }

    /**
     * Returns a flag that indicates whether performance counter
     * multiplexing was in effect during the measurement.
     */
    public boolean getMultiplexed() { return multiplexed; }

    /**
     * Returns the multiplexing period, in microseconds.  Not
     * meaningful if multiplexing is not in effect.
     */
    public long getMultiplexUS() { return multiplex_usec; }

    /**
     * Returns the information related to the execution of this process/thread,
     * such as hostname, user name, command name, CPU time consumed,
     * and the time when the XML file was created.
     */
    public ExecutionInfo getExecutionInfo() { return executionInfo; }

    /**
     * Returns the information of the system, such as information regarding
     * CPU, size of total memory, and system page size.
     */
    public MachineInfo getMachineInfo() { return machineInfo; }

    /**
     * Returns the wall clock time of this execution, in seconds.
     */
    public float getWallSecs () {
        float mhz = getMachineInfo().getCPUInfo().getClockSpeed();
        float wallsecs;
        long wallTicks = getExecutionInfo().getWallTicks();
        if (0 == wallTicks) {
            wallsecs = getExecutionInfo().getWallSecs();
        } else {
            wallsecs = wallTicks / (mhz * 1.0e6f);
        }
        return wallsecs;
    }

    /**
     * Returns a map of performance event name/value pairs.
     * The key is the name of the event,
     * and the value is the counter value of the event.
     * Some special name/value pairs are also added manually, specifically,
     * cpusecs, wallsecs, mhz, and l[0-9]cacheline (such as l2cacheline).
     *
     * @throws UnsupportedOperationException if the class of the events
     *         is not &quot;PAPI&quot; or &quot;perfmon&quot;.
     */
    public Map<String,Double> getEventValueMap () {

	Map<String,Double> inMap = new TreeMap<String,Double>();

	// populate inMap with event data
	for (Map.Entry<String,PS_HwpcEvent> entry : getEvents().entrySet()) {
	    inMap.put (entry.getKey().toUpperCase(),
                       new Double(entry.getValue().getCount()));
	}

	// populate inMap with cpusecs, mhz, wallsecs, l[0-9]cacheline
	// mhz
	float mhz = getMachineInfo().getCPUInfo().getClockSpeed();
	inMap.put ("MHZ", new Double(mhz));

	// cpusecs
	String cycleCounter = null;
	if (className.equals("PAPI")) {
	    cycleCounter = "PAPI_TOT_CYC";
	} else if (className.equals("perfmon")) {
	    cycleCounter = "CPU_CYCLES";
	} else {
	    String details = "Class name: " + className +
                ". Metric calculations are currently supported only " +
                "for class \"PAPI\" and \"perfmon\". " +
                "cycleCounter event, which is used to calculate CPU time, " +
                "is not defined for other classes of events.";
	    throw new UnsupportedOperationException (details);
	}
        Double cycleCounterValue = inMap.get(cycleCounter);
        if (cycleCounterValue != null) {
            Double cpusecs = new Double (cycleCounterValue / (mhz * 1.0e6));
            inMap.put ("CPUSECS", cpusecs);
        }

	// wallsecs
	double wallsecs;
	long wallTicks = getExecutionInfo().getWallTicks();
	if (0 == wallTicks) {
	    wallsecs = getExecutionInfo().getWallSecs();
	} else {
	    wallsecs = wallTicks / (mhz * 1.0e6f);
	}
	inMap.put ("WALLSECS", wallsecs);

	//l1cacheline, l2cacheline, ...
        int cacheLevels = getMachineInfo().getCPUInfo().getCacheLevels();
        List<List<Cache>> caches = getMachineInfo().getCPUInfo().getCaches();
        // One time there was an error in the XML file where cacheLevels
        // was 1, but there was no cache info, so caches was empty, and
        // caches.get(0) caused an IndexOutOfBound exception.
        // So added this sanity check.
        if (caches.size() >= cacheLevels) {
            for (int i = 0; i < cacheLevels; i++) {
                String key = "l" + i + "cacheline";
                List<Cache> cacheList = caches.get (i);
                if (null == cacheList)
                    continue;
                for (Cache cache : cacheList) {
                    // instruction trace cache does not have a cache linesize
                    if (! (cache instanceof InstructionTraceCache)) {
                        inMap.put (("l" + cache.getLevel() + "cacheline")
                                   .toUpperCase (),
                                   new Double(cache.getLineSize()));
                        // Once we add cache line size of this level in the map,
                        // we can skip to the next cache level.
                        break;
                    }
                }
            }
        }

	return inMap;
	    
    }

}
