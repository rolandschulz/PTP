/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIProcessMap.Process;

public interface IOpenMPIProcessMapParserListener {
	/**
	 * Notify that the end of document has been reached.
	 */
	void finish();
	
	/**
	 * Notify that the end of the map has been reached.
	 * 
	 * @param manager attribute manager contain attributes from the map
	 */
	void finishMap(AttributeManager manager);
	
	/**
	 * Notify that a process has been created.
	 * 
	 * @param proc process information
	 */
	void newProcess(Process proc);
	
	/**
	 * Notify that the document is about to start.
	 */
	void start();
	
	/**
	 * Notify that text has been output on the stderr stream.
	 *  
	 * @param proc process that emitted the output
	 * @param ouput	text that was output by the process
	 */
	void stderr(Process proc, String output);
	
	/**
	 * Notify that text has been output on the stdout stream.
	 *  
	 * @param proc process that emitted the output
	 * @param ouput	text that was output by the process
	 */
	void stdout(Process proc, String output);
}
