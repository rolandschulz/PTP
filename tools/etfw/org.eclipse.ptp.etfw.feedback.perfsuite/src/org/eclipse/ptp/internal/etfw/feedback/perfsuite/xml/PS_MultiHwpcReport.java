/* $Id: PS_MultiHwpcReport.java,v 1.4 2009/11/05 16:26:31 ruiliu Exp $ */

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

import java.util.List;
import java.util.ArrayList;

/**
 * Class that represents a collection of PerfSuite hardware
 * performance reports, known as a &quot;multihwpcreport&quot;.
 *
 * @author Rui Liu
 */
public class PS_MultiHwpcReport extends PS_Report {

    private List<PS_HwpcReport> reportList;

    PS_MultiHwpcReport() {
        reportList = new ArrayList<PS_HwpcReport>();
    }

    void addReport(PS_HwpcReport report) {
        reportList.add (report);
    }

    /**
     * Returns a list containing the component hwpcreports that
     * make up the multihwpcreport.
     */
    public List<PS_HwpcReport> getReports() {
        return reportList;
    }

    /**
     * Returns a string representation of this multi hwpc report.
     */
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append ("Number of reports in the MultiHwpcReport: ");
        res.append (reportList.size());
        res.append ("\n\n");
        for (PS_HwpcReport tmpr : reportList) {
            res.append (tmpr.toString());
            res.append ("\n"); // separator for individual hwpc reports
        }
        return res.toString();
    }

}
