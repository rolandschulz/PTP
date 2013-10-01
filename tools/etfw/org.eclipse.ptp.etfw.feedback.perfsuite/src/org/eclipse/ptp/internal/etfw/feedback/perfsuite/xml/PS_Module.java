/* $Id: PS_Module.java,v 1.7 2009/11/05 16:26:31 ruiliu Exp $ */

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

import org.xml.sax.Attributes;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Class encapsulating module information contained in a PerfSuite hardware
 * performance report XML document gathered in &quot;profile&quot; mode.
 * A module is a combination of file name and the offset into the file.
 *
 * @author Rui Liu
 */
public class PS_Module {
    private final String file;
    private final String offset;
    Map<Long,Long> sampleMap;
    PS_Module (String file, String offset) {
	this.file = file;
	this.offset = offset;
	sampleMap = new TreeMap<Long,Long>();
    }
    void addSample (Long pc, long count) {
	sampleMap.put (pc, count);
    }

    /** Returns the name of the file. */
    public String getFile() { return file; }

    /** Returns the offset into the file. */
    public String getOffset() { return offset; }

    /**
     * Returns the map of programming counter (PC) value / count pairs.
     * The count for a PC value of pc1 indicates that 
     * in how many samples the programming counter
     * of the running program had the value of pc1.
     */
    public Map<Long,Long> getSampleMap() { return sampleMap; }

    /**
     * Returns the number of samples in this module.
     */
    public long getNumSamples() {
        long sum = 0;
        for (Map.Entry<Long,Long> pairs : sampleMap.entrySet()) {
            sum += pairs.getValue();
        }
        return sum;
    }

    /** Returns a string representation of this module. */
    public String toString() {
	StringBuilder res = new StringBuilder();
	res.append ("  [file: " + file + ", offset: " + offset + "]\n");
	for (Map.Entry<Long,Long> entry : sampleMap.entrySet()) {
	    res.append ("    [").append (entry.getKey().toString())
                .append (" = ").append (entry.getValue().toString())
                .append ("]\n");
	}
	return res.toString();
    }
}

