/* $Id: PS_HwpcProfileReport.java,v 1.23 2012/01/13 20:49:17 ruiliu Exp $ */

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

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
// Used in parsing date when default locale is not en_US.

import org.xml.sax.Attributes;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;

/**
 * Class encapsulating information contained in a PerfSuite hardware
 * performance profile report XML document, which contains the source
 * code locations mapped from program counters (PCs) collected in a
 * previous profiling measurement.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public class PS_HwpcProfileReport extends PS_Report {

    private static final String DATETAG          = "date";
    private static final String HOSTTAG          = "hostname";
    private static final String DOMAINTAG        = "domainname";
    private static final String USERTAG          = "username";
    private static final String ANNOTATIONTAG    = "annotation";
    private static final String EXECUTIONINFOTAG = "executioninfo";
    private static final String MACHINEINFOTAG   = "machineinfo";

    private static final String HWPCPROFILETAG  = "hwpcprofile";
    private static final String MAPPERATT       = "mapper";
    private static final String HWPCEVENTTAG    = "hwpcevent";
    private static final String CLASSATT        = "class";
    private static final String CLASSVERSIONATT = "version";
    private static final String TYPEATT         = "type";
    private static final String NAMEATT         = "name";
    private static final String DOMAINATT       = "domain";

    private static final String PERIODTAG       = "period";
    private static final String TOTALSAMPLESTAG = "totalsamples";
    private static final String MODULETAG       = "module";
    private static final String FILETAG         = "file";
    private static final String FUNCTIONTAG     = "function";
    private static final String LINETAG         = "line";
    private static final String LINENOATT       = "lineno";

    // ctime() output: "Tue Mar  2 12:48:28 2004".
    private static final String DATEFORMAT    = "EEE MMM dd HH:mm:ss yyyy";

    private Date    date;
    private String  hostname;
    private String  domainname;
    private String  username;
    private String  annotation;
    private boolean inExecutionInfo;

    private ExecutionInfo executionInfo;
    private MachineInfo machineInfo;
    private List<PS_HwpcProfile> profileList;

    // Used in SAX XML parsing.
    private String tmpValue = "";
    private String tmpElementName;

    // For the profiling event.
    private String       tmpMapper;
    private PS_HwpcEvent tmpHwpcEvent;
    private long         tmpPeriod;
    private long         tmpTotalSamples;

    // For the profile samples.
    private int              tmpLineNo;
    private PS_Function      tmpFunction;
    private PS_File          tmpFile;
    private PS_PModule       tmpModule;
    private List<PS_PModule> tmpModuleList;
    private PS_HwpcProfile   tmpProfile;

    PS_HwpcProfileReport () {
	executionInfo = new ExecutionInfo();
	machineInfo   = new MachineInfo();
	profileList   = new ArrayList<PS_HwpcProfile>();
        inExecutionInfo = false;
    }


    void startElement(String uri, String localName, String qName,
                      Attributes atts) {

	tmpElementName = qName;

        String tagname = qName.trim();

        if (tagname.equals (MACHINEINFOTAG)) {
            inExecutionInfo = false;
        } else if (inExecutionInfo) {
	    executionInfo.startElement (uri, localName, qName, atts);
	} else if (MachineInfo.containsKey (qName)) {
	    machineInfo.startElement (uri, localName, qName, atts);
	} else {
	    // Parse the tags specific to this class.
            if (tagname.equals (EXECUTIONINFOTAG)) {
                inExecutionInfo = true;
	    } else if (tagname.equals (HWPCPROFILETAG)) {
		tmpMapper     = atts.getValue (MAPPERATT);
                tmpModuleList = new ArrayList<PS_PModule>();
                tmpProfile    = new PS_HwpcProfile ();
            } else if (tagname.equals (HWPCEVENTTAG)) {
                tmpHwpcEvent = new PS_HwpcEvent
                    (atts.getValue (NAMEATT), atts.getValue (CLASSATT),
                     atts.getValue (DOMAINATT),
                     atts.getValue (CLASSVERSIONATT),
                     atts.getValue (TYPEATT));
	    } else if (tagname.equals (MODULETAG)) {
		tmpModule =
		    new PS_PModule (atts.getValue (NAMEATT));
	    } else if (tagname.equals (FILETAG)) {
		tmpFile = new PS_File (atts.getValue (NAMEATT));
	    } else if (tagname.equals (FUNCTIONTAG)) {
		tmpFunction = new PS_Function (atts.getValue (NAMEATT));
	    } else if (tagname.equals (LINETAG)) {
		tmpLineNo =  Integer.parseInt (atts.getValue (LINENOATT));
	    }

            tmpValue = "";
	}
    }

    void characters(char[] ch, int start, int length) {
	if (tmpElementName == null) {
	    // This is to deal with the case in multi report,
	    // where character data ("\n  ") appear before any hwpc report element.
	    return;
	}
	if (inExecutionInfo) {
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
        if (qName.equals (EXECUTIONINFOTAG)) {
            inExecutionInfo = false;
        } else if (inExecutionInfo) {
	    executionInfo.endElement (uri, localName, qName);
	} else if (MachineInfo.containsKey (qName)) {
	    machineInfo.endElement (uri, localName, qName);
	} else {
            if (qName.equals (DATETAG)) {
                // SimpleDateFormat.parse() is locale-sensitive,
                // while as of Jan 2, 2009, the date times in XML reports
                // generated by psrun and libpshwpc use en_US locale only.
                // The approach we used is: save user's default locale,
                // use en_US to parse the date,
                // then restore user's default locale.
                Locale savedDefaultLocale = Locale.getDefault();
                if (! savedDefaultLocale.equals (Locale.US)) {
                    Locale.setDefault (Locale.US);
                }
                SimpleDateFormat df = new SimpleDateFormat(DATEFORMAT);
                try {
                    date = df.parse(tmpValue);
                } catch (java.text.ParseException pe) {
                    System.err.println
                        ("Error in parsing date in ExecutionInfo.endElement:" +
                         " date = \"" + tmpValue + "\".");
                    throw new RuntimeException (pe);
                }
                if (! savedDefaultLocale.equals (Locale.US)) {
                    Locale.setDefault (savedDefaultLocale);
                }
            } else if (qName.equals (HOSTTAG)) {
                hostname = tmpValue;
            } else if (qName.equals (DOMAINTAG)) {
                domainname = tmpValue;
            } else if (qName.equals (USERTAG)) {
                username = tmpValue;
            } else if (qName.equals (ANNOTATIONTAG)) {
                annotation = tmpValue;
	    } else if (qName.equals (PERIODTAG)) {
		tmpPeriod = Long.parseLong (tmpValue);
	    } else if (qName.equals (TOTALSAMPLESTAG)) {
		tmpTotalSamples = Long.parseLong (tmpValue);
	    } else if (qName.equals (MODULETAG)) {
		tmpModuleList.add (tmpModule);
	    } else if (qName.equals (FILETAG)) {
                tmpModule.add (tmpFile);
	    } else if (qName.equals (FUNCTIONTAG)) {
                tmpFile.add (tmpFunction);
	    } else if (qName.equals (LINETAG)) {
                long tmpNumSamples = Long.parseLong (tmpValue);
                tmpFunction.add (tmpLineNo, tmpNumSamples);
	    } else if (qName.equals (HWPCPROFILETAG)) {
                tmpProfile.setMapper (tmpMapper);
                tmpProfile.setHwpcEvent (tmpHwpcEvent);
                tmpProfile.setPeriod (tmpPeriod);
                tmpProfile.setTotalSamples (tmpTotalSamples);
                tmpProfile.setModules (tmpModuleList);
                profileList.add (tmpProfile);
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

        res.append("  [Date:        " + date.toString() + "]\n");
        res.append("  [Hostname:    " + hostname + "]\n");
        res.append("  [User:        " + username + "]\n");
        if ( annotation != null ) {
            res.append("  [Annotation:  " + annotation + "]\n");
        }
	res.append (executionInfo.toString());
	res.append (machineInfo.toString());

        res.append ("Profile Information\n");

        int numProfiles = profileList.size ();
        if (0 == numProfiles) {
            res.append ("No profile information is available.\n");
        }

        for (int prof_idx = 0; prof_idx < numProfiles; prof_idx++) {
            PS_HwpcProfile cur_profile = profileList.get (prof_idx);

            res.append ("  Event Info:\n");
            res.append (cur_profile.getHwpcEvent().toString());
            res.append ("\n");

            res.append ("  Period: " + cur_profile.getPeriod() + "\n");
            res.append ("  Total number of samples: " +
                        cur_profile.getTotalSamples() + "\n");

            List<PS_PModule> moduleList = cur_profile.getModules();

            res.append("Module Counts (" + moduleList.size() + " modules)\n");
            for (PS_PModule tmpModule : moduleList) {
                res.append ("Module: " + tmpModule.getName() + "\n");
                for (PS_File tmpFile : tmpModule.getFiles()) {
                    res.append ("  File: " + tmpFile.getName() + "\n");
                    for (PS_Function tmpFunction : tmpFile.getFunctions()) {
                        res.append ("    Function: " +
                                    tmpFunction.getName() + "\n");
                        for (Map.Entry entry :
                                 tmpFunction.getMap().entrySet()) {
                            res.append("      Line number: " +
                                       entry.getKey() +
                                       ", number of samples: " +
                                       entry.getValue() + "\n");
                        }
                    }
                }
            }
        }

        return res.toString();
    }


    /**
     * Returns the date and time when this profile report was generated
     * from the original HWPC report.
     */
    public Date getDate() { return date; }

    /**
     * Returns the name of the machine where this profile report was generated
     * from the original HWPC report.
     */
    public String getHostName() { return hostname; }

    /**
     * Returns the domain name of the machine where this profile report
     * was generated from the original HWPC report.
     */
    public String getDomainName() { return domainname; }

    /**
     * Returns the name of the user who generated this profile report
     * from the original HWPC report.
     */
    public String getUserName() { return username; }

    /**
     * Returns the string that was set by the user as
     * a PerfSuite &quot;annotation&quot; element when this profile report
     * was generated from the original HWPC report.
     */
    public String getAnnotation() { return annotation; }

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
        float mhz = machineInfo.getCPUInfo().getClockSpeed();
        float wallsecs;
        long wallTicks = executionInfo.getWallTicks();
        if (0 == wallTicks) {
            wallsecs = executionInfo.getWallSecs();
        } else {
            wallsecs = wallTicks / (mhz * 1.0e6f);
        }
        return wallsecs;
    }

    /**
     * Returns a <code>List&lt;PS_HwpcProfile&gt;</code> object
     * containing the information of the profiles obtained.
     */
    public List<PS_HwpcProfile> getProfiles() { return profileList; }

}
