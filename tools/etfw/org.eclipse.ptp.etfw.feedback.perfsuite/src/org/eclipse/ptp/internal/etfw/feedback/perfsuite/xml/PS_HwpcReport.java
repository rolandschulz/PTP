/* $Id: PS_HwpcReport.java,v 1.7 2009/09/16 21:02:30 ruiliu Exp $ */

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

/**
 * Abstract class representing the contents of a PerfSuite XML document
 * gathered through monitoring using hardware performance counters.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public abstract class PS_HwpcReport extends PS_Report {
    public abstract ExecutionInfo getExecutionInfo();
    public abstract MachineInfo getMachineInfo();
}
