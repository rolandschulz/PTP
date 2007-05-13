/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.debug.ui.tests;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

/** Abtract Debug Junit Test
 * @author clement
 */
public abstract class AbstractDebugTest extends TestCase {
	//given the name of resource manager
	protected final String resourceMgrName = "ORTE";
	//given machine name
	protected final String machineName = "Machine0";
	//given queue name
	protected final String queueName = "localQueue";

	protected IPJob job = null;

	protected int nProcs = 1;
	protected int firstNode = 0;
	protected int NProcsPerNode = 1;
	
	/**
	 * Constructor
	 * @param name
	 * @param nProcs
	 * @param firstNode
	 * @param NProcsPerNode
	 */
	public AbstractDebugTest(String name, int nProcs, int firstNode, int NProcsPerNode) {
		super(name);
		this.nProcs = nProcs;
		this.firstNode = firstNode;
		this.NProcsPerNode = NProcsPerNode;
	}
	/**
	 * Start Debugger Server
	 * @throws CoreException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected abstract void startDebugServer() throws CoreException, IOException, InterruptedException;
	/**
	 * Sets up the test fixture.
	 * 
	 * Called before every test case method.
	 */
	protected void setUp() throws CoreException, InvocationTargetException, IOException, InterruptedException {
		//Creae debug model object..
		startDebugServer();
	}
	/**
	 * Tear down when test case is finished
	 */
	protected void tearDown() throws CoreException, IOException, PCDIException {
		if (job != null) {
			if(job.isTerminated()) {
				job.removeAllProcesses();
			}
		}
		job = null;
	}
}
