/* $Id: PS_HwpcPCProfileReport.java,v 1.9 2012/01/13 20:49:17 ruiliu Exp $ */

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

import org.xml.sax.Attributes;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.ptp.internal.etfw.feedback.perfsuite.util.PS_Debug;

/**
 * Class encapsulating information contained in a PerfSuite hardware
 * performance report XML document gathered in &quot;profile&quot; mode.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public class PS_HwpcPCProfileReport extends PS_HwpcReport {

    private static final String PDATATAG        = "hwpcprofiledata";
    private static final String CLASSATT        = "class";
    private static final String CLASSVERSIONATT = "version";
    private static final String PINFOTAG        = "profileinfo";
    private static final String PEVENTTAG       = "profileevent";
    private static final String DOMAINATT       = "domain";
    private static final String TYPEATT         = "type";
    private static final String NAMEATT         = "name";
    private static final String DERIVEDATT      = "derived";
    private static final String THRESHOLDTAG    = "threshold";
    private static final String TOTALSAMPLESTAG = "totalsamples";
    private static final String MODULETAG       = "module";
    private static final String FILEATT         = "file";
    private static final String OFFSETATT       = "offset";
    private static final String SAMPLESTAG      = "samples";
    private static final String SAMPLETAG       = "sample";
    private static final String PCATT           = "pc";

    private ExecutionInfo executionInfo;
    private MachineInfo machineInfo;
    private String className;
    private String classVersion;
    private String eventDomain;
    private String eventType;  // "preset" or "native"
    private String eventName;
    private boolean eventDerived;
    private long   threshold;
    private long   totalSamples;
    private List<PS_Module> moduleList;

    private String tmpValue = "";
    private String tmpElementName;
    private PS_Module tmpModule;
    private String tmpPc;

    PS_HwpcPCProfileReport () {
	executionInfo = new ExecutionInfo();
	machineInfo = new MachineInfo();
	moduleList = new ArrayList<PS_Module>();
    }


    void startElement(String uri, String localName, String qName,
                      Attributes atts) {

	tmpElementName = qName;

	if (ExecutionInfo.containsKey (qName)) {
	    executionInfo.startElement (uri, localName, qName, atts);
	} else if (MachineInfo.containsKey (qName)) {
	    machineInfo.startElement (uri, localName, qName, atts);
	} else {
	    // parse the tags specific to this class
	    String tagname = qName.trim();

	    if (tagname.equals (PDATATAG)) {
		className = atts.getValue (CLASSATT);
		classVersion = atts.getValue (CLASSVERSIONATT); // could be absent
	    } else if (tagname.equals (PEVENTTAG)) {
		eventDomain = atts.getValue (DOMAINATT);
		eventType = atts.getValue (TYPEATT);
		eventName = atts.getValue (NAMEATT);
		eventDerived = (atts.getValue (DERIVEDATT).equals ("yes"));
	    } else if (tagname.equals (MODULETAG)) {
		tmpModule =
		    new PS_Module (atts.getValue (FILEATT), atts.getValue (OFFSETATT));
	    } else if (tagname.equals (SAMPLETAG)) {
		tmpPc = atts.getValue (PCATT);
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
	    if (qName.equals (SAMPLETAG)) {
                // Validate the value of tmpPc --
                // it should be 0 or a positive integer.
                // Validate the value of tmpValue --
                // it should be a positive integer.
                Long pc = -1l;
                Long count = -1l;
                try {
                    pc = Long.parseLong (tmpPc, 16);
                    count = Long.parseLong (tmpValue);
                    if (count <= 0) {
                        PS_Debug.print
                            (PS_Debug.WARNING, "count (" +
                             String.valueOf (count) + ") <= 0 for pc " + tmpPc);
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException (e);
                }
                if (pc >= 0 && (count > 0)) {
                    tmpModule.addSample (pc, count);
                }
	    } else if (qName.equals (MODULETAG)) {
		moduleList.add (tmpModule);
	    } else if (qName.equals (THRESHOLDTAG)) {
		threshold = Long.parseLong (tmpValue);
	    } else if (qName.equals (TOTALSAMPLESTAG)) {
		totalSamples = Long.parseLong (tmpValue);
	    }
	}

	tmpValue = "";
    }

    /**
     * Returns a string representation of this hwpc profile report.
     */
    public String toString() {
        StringBuilder res = new StringBuilder();
	res.append (super.toString());

	res.append (executionInfo.toString());
	res.append (machineInfo.toString());

        res.append ("Profile Information\n");
        res.append ("  [Class:         " + className + "]\n");
	if (classVersion != null) {
	    res.append ("  [Version:       " + classVersion + "]\n");
	}
        res.append ("  [Event Domain:  " + eventDomain + "]\n");
        res.append ("  [Event Name:    " + eventName + "]\n");
        res.append ("  [Event Type:    " + eventType + "]\n");
        res.append ("  [Event Derived: " + eventDerived + "]\n");

        res.append("Module Counts (" + moduleList.size() + " modules)\n");
	for (PS_Module module : moduleList) {
	    res.append (module.toString());
	}
        return res.toString();
    }


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
        return eventDomain;
    }

    /**
     * Returns the name of the measured event.
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Returns the type of the event in effect
     * during the measurement; one of &quot;preset&quot;,
     * or &quot;native&quot;.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Returns a flag indicating whether the measured
     * event is derived.
     */
    public boolean getEventDerived() { return eventDerived;}

    /**
     * Returns the threshold of the measured event;
     * returns 0 if the threshold is absent in the input XML file.
     */
    public long getThreshold() {
	return threshold;
    }

    /**
     * Returns the number of total samples in the measurement.
     */
    public long getTotalSamples() {
	return totalSamples;
    }

    /**
     * Returns the list of modules in the measurement.
     */
    public List<PS_Module> getModules() {
	return moduleList;
    }

    /**
     * Returns an <code>ExecutionInfo</code> object containing 
     * the information related to the execution of this process/thread,
     * such as hostname, user name, command name, CPU time consumed,
     * and the time when the XML file was created.
     */
    public ExecutionInfo getExecutionInfo() { return executionInfo; }

    /**
     * Returns a <code>MachineInfo</code> object containing
     * the information of the system, such as the number of
     * CPUs, the size of total memory, and the system page size.
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

}
