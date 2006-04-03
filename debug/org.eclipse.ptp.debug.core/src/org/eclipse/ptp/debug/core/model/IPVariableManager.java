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
package org.eclipse.ptp.debug.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.IPJob;

/**
 * @author Clement chu
 * 
 */
public interface IPVariableManager {
	public void shutdown();
	public void addVariable(IPJob job, String set_id, String variable, IProgressMonitor monitor) throws CoreException;
	public void removeVariable(IPJob job, String variable, IProgressMonitor monitor) throws CoreException;
	public void removeAllVariables(IPJob job);
	public boolean hasVariable(IPJob job);
	public String[] getVariables(IPJob job);
	public void updateVariableResults(IPJob job, String set_id, IProgressMonitor monitor) throws CoreException;
	public String getResultDisplay(IPJob job, String set_id, int taskID);
	public void cleanVariableResults(IPJob job);
	public void addListener(IPVariableListener listener);
	public void removeListener(IPVariableListener listener);

	public interface IPVariableListener {
		public void update(IPJob job);
	}
}

