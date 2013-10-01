/* $Id: PS_HwpcProfile.java,v 1.1 2011/04/11 20:31:11 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2011 The Board of Trustees of the University of Illinois.
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

import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;


/**
 * Class encapsulating profile information that describes the
 * profiling event used, the sampling rate, the total number
 * of collected samples, the locations of the source code
 * and the number of samples collected at these locations.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
/*
 * Non-javadoc comments.
 *
 * This class, and the PS_Function, PS_File,
 * and PS_PModule classes contained in this file, were separated
 * from PS_HwpcProfileReport.java around March/April 2011, in the
 * JVMTI Java profiling efforts, to support multiple profiles contained
 * within the same hwpcprofilereport.  The goal is to support profiling
 * multiple events in one execution.
 */
public class PS_HwpcProfile {

    private String  mapper;
    private PS_HwpcEvent hwpcEvent;
    private long   period;
    private long   totalSamples;
    private List<PS_PModule> moduleList;

    PS_HwpcProfile () {
	moduleList = new ArrayList<PS_PModule>();
    }

    /**
     * Returns a string representation of this profile.
     */
    public String toString() {
        StringBuilder res = new StringBuilder();
	res.append (super.toString());

        res.append ("Profile Information\n");
        res.append ("  Event Info:\n");
        res.append (hwpcEvent.toString());
        res.append ("\n");

        res.append ("  Period: " + period + "\n");
        res.append ("  Total number of samples: " + totalSamples + "\n");

        res.append("Module Counts (" + moduleList.size() + " modules)\n");
	for (PS_PModule tmpModule : moduleList) {
            res.append ("Module: " + tmpModule.getName() + "\n");
            for (PS_File tmpFile : tmpModule.getFiles()) {
                res.append ("  File: " + tmpFile.getName() + "\n");
                for (PS_Function tmpFunction : tmpFile.getFunctions()) {
                    res.append ("    Function: " +
                                tmpFunction.getName() + "\n");
                    for (Map.Entry entry : tmpFunction.getMap().entrySet()) {
                        res.append("      Line number: " + entry.getKey() +
                                   ", number of samples: " +
                                   entry.getValue() + "\n");
                    }
                }
            }
	}
        return res.toString();
    }


    /**
     * Returns the facility that was used to do source code mapping;
     * one of &quot;psbfd&quot; and &quot;binutils&quot;.
     */
    public String getMapper() { return mapper; }

    /**
     * Returns the event that was used to do profiling.
     */
    public PS_HwpcEvent getHwpcEvent() { return hwpcEvent; }

    /**
     * Returns the period (that is, the sampling rate) of the event.
     */
    public long getPeriod() { return period;}

    /**
     * Returns the total number of samples in the profile.
     */
    public long getTotalSamples() { return totalSamples; }

    /**
     * Returns the modules in the profile.
     */
    public List<PS_PModule> getModules() { return moduleList; }

    /**
     * Returns a nested map containing information about the
     * modules, files, functions, line numbers and number of samples
     * for the line numbers, in such order.
     */
    public Map<String,Map<String,Map<String,Map<Long,Long>>>>
        getNestedModuleMap() {

        Map<String,Map<String,Map<String,Map<Long,Long>>>>
            nestedModuleMap =
            new TreeMap<String,Map<String,Map<String,Map<Long,Long>>>>();
	for (PS_PModule tmpModule : moduleList) {
            String module = tmpModule.getName();
            for (PS_File tmpFile : tmpModule.getFiles()) {
                String file = tmpFile.getName();
                for (PS_Function tmpFunction : tmpFile.getFunctions()) {
                    String func = tmpFunction.getName();
                    for (Map.Entry<Integer,Long> entry :
                             tmpFunction.getMap().entrySet()) {

                        int line = entry.getKey().intValue();
                        Long numSamples = entry.getValue();

                        Map<String,Map<String,Map<Long,Long>>> nestedFileMap;
                        Map<String,Map<Long,Long>> nestedFuncMap;
                        Map<Long,Long>  nestedLineMap;
                        nestedFileMap = nestedModuleMap.get (module);
                        if (null == nestedFileMap) {
                            nestedFileMap = new TreeMap<String,Map<String,Map<Long,Long>>>();
                            nestedModuleMap.put (module, nestedFileMap);
                        }
                        nestedFuncMap = nestedFileMap.get (file);
                        if (null == nestedFuncMap) {
                            nestedFuncMap =
                                new TreeMap<String,Map<Long,Long>>();
                            nestedFileMap.put (file, nestedFuncMap);
                        }
                        nestedLineMap = nestedFuncMap.get (func);
                        if (null == nestedLineMap) {
                            nestedLineMap = new TreeMap<Long,Long>();
                            nestedFuncMap.put (func, nestedLineMap);
                        }
                        Long value = nestedLineMap.get (Long.valueOf(line));
                        nestedLineMap.put (Long.valueOf (line),
                                           value == null ?
                                           numSamples : (numSamples + value));
                    }
                }
            }
	}
        return nestedModuleMap;
    }


    /* The following setter methods are package-private. */

    void setMapper(String in) { mapper = in; }

    void setHwpcEvent(PS_HwpcEvent in) { hwpcEvent = in; }

    void setPeriod(long in) { period = in;}

    void setTotalSamples(long in) { totalSamples = in; }

    void setModules(List<PS_PModule> in) { moduleList = in; }

}

/* The classes below are package-private.  They do not have
 * javadoc documentation written. */
class PS_Function {
    String name;
    long numSamples;
    Map<Integer,Long> map;
    /* Map of line number --> sample count.
     * We don't expect a file to contain more than 2 G lines,
     * so used "Integer" as the data type for now.  This is internal
     * implementation, and can be easily changed later if necessary. */
    PS_Function(String name) {
        this.name = name;
        this.numSamples = 0;
        map = new TreeMap<Integer,Long>();
    }
    void add (int lineNo, long samples) {
        map.put (lineNo, samples);
        numSamples += samples;
    }
    long getNumSamples() { return numSamples; }
    String getName() { return name; }
    Map<Integer,Long> getMap() { return map; }
}

class PS_File {
    String name;
    long numSamples;
    List<PS_Function> functions;
    PS_File(String name) {
        this.name = name;
        this.numSamples = 0;
        functions = new ArrayList<PS_Function>();
    }
    void add (PS_Function function) {
        functions.add (function);
        numSamples += function.getNumSamples();
    }
    long getNumSamples() { return numSamples; }
    List<PS_Function> getFunctions() { return functions; }
    String getName() { return name; }
}

class PS_PModule {
    String name;
    long numSamples;
    List<PS_File> files;
    PS_PModule(String name) {
        this.name = name;
        this.numSamples = 0;
        files = new ArrayList<PS_File>();
    }
    void add (PS_File file) {
        files.add (file);
        numSamples += file.getNumSamples();
    }
    long getNumSamples() { return numSamples; }
    List<PS_File> getFiles() { return files; }
    String getName() { return name; }
}
