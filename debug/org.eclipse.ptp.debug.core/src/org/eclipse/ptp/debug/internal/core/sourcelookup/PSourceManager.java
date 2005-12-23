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
package org.eclipse.ptp.debug.internal.core.sourcelookup;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLocation;
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLocator;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;

/**
 * @author Clement chu
 * 
 */
public class PSourceManager implements IPSourceLocator, IPersistableSourceLocator, IAdaptable {
	private ISourceLocator fSourceLocator = null;
	private ILaunch fLaunch = null;
	private PDebugTarget fDebugTarget = null;

	public PSourceManager(ISourceLocator sourceLocator) {
		setSourceLocator(sourceLocator);
	}
	public int getLineNumber(IStackFrame frame) {
		if (getPSourceLocator() != null) {
			return getPSourceLocator().getLineNumber(frame);
		}
		if (frame instanceof IPStackFrame) {
			return ((IPStackFrame) frame).getFrameLineNumber();
		}
		return 0;
	}
	public IPSourceLocation[] getSourceLocations() {
		return (getPSourceLocator() != null) ? getPSourceLocator().getSourceLocations() : new IPSourceLocation[0];
	}
	public void setSourceLocations(IPSourceLocation[] locations) {
		if (getPSourceLocator() != null) {
			getPSourceLocator().setSourceLocations(locations);
			PDebugTarget target = getDebugTarget();
			if (target != null) {
				/**
				 * FIXME 
				 Disassembly d = null; 
				 try { 
				 	d = (Disassembly)target.getDisassembly(); 
				 } catch(DebugException e) {} 
				 if (d != null) {
				 	d.reset();
				 }
				 */
			}
		}
	}
	public boolean contains(IResource resource) {
		return (getPSourceLocator() != null) ? getPSourceLocator().contains(resource) : false;
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(PSourceManager.class))
			return this;
		if (adapter.equals(IPSourceLocator.class))
			return this;
		if (adapter.equals(IPersistableSourceLocator.class))
			return this;
		if (adapter.equals(IResourceChangeListener.class) && fSourceLocator instanceof IResourceChangeListener)
			return fSourceLocator;
		return null;
	}
	public Object getSourceElement(IStackFrame stackFrame) {
		Object result = null;
		if (getSourceLocator() != null)
			result = getSourceLocator().getSourceElement(stackFrame);
		return result;
	}
	protected IPSourceLocator getPSourceLocator() {
		if (getSourceLocator() instanceof IPSourceLocator)
			return (IPSourceLocator) getSourceLocator();
		return null;
	}
	protected ISourceLocator getSourceLocator() {
		if (fSourceLocator != null)
			return fSourceLocator;
		else if (fLaunch != null)
			return fLaunch.getSourceLocator();
		return null;
	}
	private void setSourceLocator(ISourceLocator sl) {
		fSourceLocator = sl;
	}
	public Object findSourceElement(String fileName) {
		if (getPSourceLocator() != null) {
			return getPSourceLocator().findSourceElement(fileName);
		}
		return null;
	}
	public String getMemento() throws CoreException {
		if (getPersistableSourceLocator() != null)
			return getPersistableSourceLocator().getMemento();
		return null;
	}
	public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
		if (getPersistableSourceLocator() != null)
			getPersistableSourceLocator().initializeDefaults(configuration);
	}
	public void initializeFromMemento(String memento) throws CoreException {
		if (getPersistableSourceLocator() != null)
			getPersistableSourceLocator().initializeFromMemento(memento);
	}
	private IPersistableSourceLocator getPersistableSourceLocator() {
		if (fSourceLocator instanceof IPersistableSourceLocator)
			return (IPersistableSourceLocator) fSourceLocator;
		return null;
	}
	public IProject getProject() {
		return (getPSourceLocator() != null) ? getPSourceLocator().getProject() : null;
	}
	public void setDebugTarget(PDebugTarget target) {
		fDebugTarget = target;
	}
	protected PDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
	public void setSearchForDuplicateFiles(boolean search) {
		if (getPSourceLocator() != null)
			getPSourceLocator().setSearchForDuplicateFiles(search);
	}
	public boolean searchForDuplicateFiles() {
		return (getPSourceLocator() != null) ? getPSourceLocator().searchForDuplicateFiles() : false;
	}
}
