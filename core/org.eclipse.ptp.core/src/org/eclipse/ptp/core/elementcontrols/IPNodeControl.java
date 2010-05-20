/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core.elementcontrols;

import java.util.BitSet;

import org.eclipse.ptp.core.elements.IPNode;

public interface IPNodeControl extends IPElementControl, IPNode {
	/**
	 * @param job
	 * @param processRanks
	 * @since 4.0
	 */
	public void addJobProcessRanks(IPJobControl job, BitSet processRanks);

	/**
	 * Get the parent machine for this node
	 * 
	 * @return parent machine
	 */
	public IPMachineControl getMachineControl();

	/**
	 * @param job
	 * @param processRanks
	 * @since 4.0
	 */
	public void removeJobProcessRanks(IPJobControl job, BitSet processRanks);
}
