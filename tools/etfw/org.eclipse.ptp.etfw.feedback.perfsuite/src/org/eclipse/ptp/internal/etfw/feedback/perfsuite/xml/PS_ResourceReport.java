/* $Id: PS_ResourceReport.java,v 1.14 2012/01/13 20:49:17 ruiliu Exp $ */

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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Locale;

/**
 * Class encapsulating information contained in a PerfSuite resource
 * usage report XML document.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public class PS_ResourceReport extends PS_Report {

    private static final String PIDTAG                  = "pid";
    private static final String STARTTIMETAG            = "starttime";
    private static final String EXITCODETAG             = "exitcode";
    private static final String EXIT_TYPE_ATT           = "type";
    private static final String SAMPLESTAG              = "samples";
    private static final String SAMPLES_FREQUENCY_ATT   = "frequency";
    private static final String LOADAVGSTAG             = "loadavgs";
    private static final String LOADAVGS_MIN_ATT        = "min";
    private static final String LOADAVGS_MAX_ATT        = "max";
    private static final String LOADAVGS_RESOLUTION_ATT = "resolution";
    private static final String LOADAVGS_OUTOFRANGE_ATT = "outofrange";
    private static final String LOADAVGTAG              = "loadavg";
    private static final String LOADAVG_LOAD_ATT        = "load";
    private static final String MAXLOADSTAG             = "maxloads";
    private static final String MAXLOADTAG              = "maxload";
    private static final String MAXLOAD_INTERVAL_ATT    = "interval";
    private static final String MAXLOAD_UNIT_ATT        = "unit";
    //private static final String MEMTAG                  = "memoryuse";
    // nothing to parse
    private static final String MAXRSSTAG               = "maxrss";
    private static final String MAXVMTAG                = "maxvm";
    private static final String USERTIMETAG             = "usertime";
    private static final String SYSTEMTIMETAG           = "systemtime";
    private static final String CPUTIMETAG              = "cputime";
    //private static final String FAULTSTAG               = "faults";
    // nothing to parse
    private static final String MAJORTAG                = "major";
    private static final String MINORTAG                = "minor";
    private static final String SWAPSTAG                = "swaps";

    private static final String DATEFORMAT = "EEE MMM dd HH:mm:ss yyyy";

    private String tmpValue = "";

    private long pid;
    private Date startTime;
    private int exitCode;
    private String exitType;

    private int samplesFrequency;
    private long samples;
    private float loadavgsMin;
    private float loadavgsMax;
    private float loadavgsResolution;
    private long loadavgsOutofrange;
    private Map<Float, Long> loadavgMap;
    private float tmpLoadavg;
    private Map<Integer, Float> maxloadMap;
    private int tmpMaxload;
    // units of maxload interval (Integer) is seconds
    private float maxrss;
    private float maxvm;
    private CPUTime cputime;
    private float userTime;
    private float systemTime;
    private long majorFaults;
    private long minorFaults;
    private long swaps;

    PS_ResourceReport () {
    }

    void startElement(String uri, String localName, String qName,
                      Attributes atts) {
	// parse the tags specific to this class
	if (qName.equals (EXITCODETAG)) {
	    exitType = atts.getValue (EXIT_TYPE_ATT);
	} else if (qName.equals (SAMPLESTAG)) {
	    samplesFrequency =
                Integer.parseInt (atts.getValue (SAMPLES_FREQUENCY_ATT));
	} else if (qName.equals (LOADAVGSTAG)) {
	    loadavgsMin = Float.parseFloat (atts.getValue (LOADAVGS_MIN_ATT));
	    loadavgsMax = Float.parseFloat (atts.getValue (LOADAVGS_MAX_ATT));
	    loadavgsResolution =
                Float.parseFloat (atts.getValue (LOADAVGS_RESOLUTION_ATT));
	    loadavgsOutofrange =
                Long.parseLong (atts.getValue (LOADAVGS_OUTOFRANGE_ATT));
	    loadavgMap = new TreeMap<Float, Long>();
	} else if (qName.equals (LOADAVGTAG)) {
	    tmpLoadavg = Float.parseFloat (atts.getValue (LOADAVG_LOAD_ATT));
	} else if (qName.equals (MAXLOADSTAG)) {
	    maxloadMap = new TreeMap<Integer, Float>();
	} else if (qName.equals (MAXLOADTAG)) {
	    tmpMaxload =
                Integer.parseInt (atts.getValue (MAXLOAD_INTERVAL_ATT));
	    if (atts.getValue (MAXLOAD_UNIT_ATT).equals ("minutes")) {
		tmpMaxload *= 60;
	    }
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
	if (qName.equals (PIDTAG)) {
	    pid = Long.parseLong (tmpValue);
	} else if (qName.equals (EXITCODETAG)) {
	    exitCode = Integer.parseInt(tmpValue);
	} else if (qName.equals (STARTTIMETAG)) {
	    // SimpleDateFormat.parse() is locale-sensitive,
	    // while currently (as of Jan 2, 2009) the date times in XML reports
	    // generated by psrun and libpshwpc are of the en_US locale only.
	    // The approach we used is: save user's default locale,
	    // use en_US to parse the date, then restore user's default locale.
	    Locale savedDefaultLocale = Locale.getDefault();
	    if (! savedDefaultLocale.equals (Locale.US)) {
		Locale.setDefault (Locale.US);
	    }
	    SimpleDateFormat df = new SimpleDateFormat(DATEFORMAT);
            try {
                startTime = df.parse(tmpValue);
            } catch (ParseException e) {
                throw new RuntimeException (e);
            }
	    if (! savedDefaultLocale.equals (Locale.US)) {
		Locale.setDefault (savedDefaultLocale);
	    }
	} else if (qName.equals (SAMPLESTAG)) {
	    samples = Long.parseLong (tmpValue);
	} else if (qName.equals (LOADAVGTAG)) {
	    loadavgMap.put (tmpLoadavg, Long.parseLong (tmpValue));
	} else if (qName.equals (MAXLOADTAG)) {
	    maxloadMap.put (tmpMaxload, Float.parseFloat (tmpValue));
	} else if (qName.equals (MAXRSSTAG)) {
	    maxrss = Float.parseFloat (tmpValue);
	} else if (qName.equals (MAXVMTAG)) {
	    maxvm = Float.parseFloat (tmpValue);
	} else if (qName.equals (USERTIMETAG)) {
	    userTime = Float.parseFloat (tmpValue);
	} else if (qName.equals (SYSTEMTIMETAG)) {
	    systemTime = Float.parseFloat (tmpValue);
	} else if (qName.equals (CPUTIMETAG)) {
	    cputime = new CPUTime (userTime, systemTime);
	} else if (qName.equals (MAJORTAG)) {
	    majorFaults = Long.parseLong (tmpValue);
	} else if (qName.equals (MINORTAG)) {
	    minorFaults = Long.parseLong (tmpValue);
	} else if (qName.equals (SWAPSTAG)) {
	    swaps = Long.parseLong (tmpValue);
	}
	
	tmpValue = "";
    }
 
    /**
     * Returns a string representation of this resource report.
     */
    public String toString() {
        StringBuilder res = new StringBuilder();
	res.append (super.toString());
	res.append ("  Pid:        " + pid + "\n");
	res.append ("  Start Time: " + startTime + "\n");
	res.append ("  Exit Code:  " + exitCode + "\n");
	res.append ("  Exit Type:  " + exitType + "\n");
	res.append ("  Samples Frequency:  " + samplesFrequency
                    + " (seconds)\n");
	res.append ("  Samples:  " + samples + "\n");
	res.append ("  Load Average Min:  " + loadavgsMin + "\n");
	res.append ("  Load Average Max:  " + loadavgsMax + "\n");
	res.append ("  Load Average Resolution:  " + loadavgsResolution + "\n");
	res.append ("  Number of Measurements " +
                    "where Load Average is Out of Range (>= 100):  " +
                    loadavgsOutofrange + "\n");
	for (Map.Entry<Float,Long> entry : loadavgMap.entrySet()) {
	    res.append ("  Load: " + entry.getKey() +
                        ", Number of Occurrances: " + entry.getValue() + "\n");
	}
	for (Map.Entry<Integer,Float> entry : maxloadMap.entrySet()) {
	    res.append ("  Max Load Interval: " + entry.getKey() +
                        " (seconds), Max Load: " + entry.getValue() + "\n");
	}
	res.append ("  Max RSS:  " + maxrss + " (MB)\n");
	res.append ("  Max VM:   " + maxvm + " (MB)\n");
	res.append ("  User Time:   " + userTime + " (seconds)\n");
	res.append ("  System Time:   " + systemTime + " (seconds)\n");
	res.append ("  Major Faults:   " + majorFaults + "\n");
	res.append ("  Minor Faults:   " + minorFaults + "\n");
	res.append ("  Swaps:   " + swaps + "\n");

        return res.toString();
    }


    /**
     * Returns the process ID (pid) of the program.
     */
    public long getPid() { return pid;}

    /**
     * Returns the start time of the program.
     */
    public Date getStartTime() { return startTime;}

    /**
     * Returns the exit code of the program.
     */
    public int getExitCode() { return exitCode;}

    /**
     * Returns the exit type of the program, one of &quot;exit&quot;
     * or &quot;signal&quot;.
     */
    public String getExitType() { return exitType;}

    /**
     * Returns the sampling frequency, in seconds.
     */
    public int getSamplesFrequency() { return samplesFrequency;}

    /**
     * Returns the total number of samples.
     */
    public long getSamples() { return samples;}

    /**
     * Returns the minimum value of load average.
     */
    public float getLoadavgsMin() { return loadavgsMin;}

    /**
     * Returns the maximum value of load average.
     */
    public float getLoadavgsMax() { return loadavgsMax;}

    /**
     * Returns the resolution of the measured load average values.
     */
    public float getLoadavgsResolution() { return loadavgsResolution;}

    /**
     * Returns the number of measurements when load average is out of range
     * (currently means greater than or equal to 100).
     */
    public long getLoadavgsOutofrange() { return loadavgsOutofrange;}

    /**
     * Returns a map containing pairs of load average -> the number of 
     * samples having this load average.
     */
    public Map<Float,Long> getLoadavgMap() { return loadavgMap;}

    /**
     * Returns a map containing pairs of time interval (in seconds)
     *  -> the maximum load in the time interval.
     */
    public Map<Integer,Float> getMaxloadMap() { return maxloadMap;}

    /**
     * Returns the maximum size of resident memory in mega bytes (MB)
     * that this program used.
     */
    public float getMaxrss() { return maxrss;}

    /**
     * Returns the maximum size of virtual memory in mega bytes (MB)
     * that this program used.
     */
    public float getMaxvm() { return maxvm;}

    /**
     * Returns a <code>CPUTime</code> object, which contains the user time
     * (in seconds) and system time (in seconds) that the program used.
     */
    public CPUTime getCputime() { return cputime;}

    /**
     * Returns the user time in seconds that the program used.
     */
    public float getUserTime() { return userTime;}

    /**
     * Returns the system time in seconds that the program used.
     */
    public float getSystemTime() { return systemTime;}

    /**
     * Returns the number of major faults that occurred
     * in executing the program.
     */
    public long getMajorFaults() { return majorFaults;}

    /**
     * Returns the number of minor faults that occurred
     * in executing the program.
     */
    public long getMinorFaults() { return minorFaults;}

    /**
     * Returns the number of swaps that occurred in executing the program.
     */
    public long getSwaps() { return swaps;}


}
