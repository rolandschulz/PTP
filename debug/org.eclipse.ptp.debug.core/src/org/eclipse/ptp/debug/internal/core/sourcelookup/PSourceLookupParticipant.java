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

import java.io.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.sourcelookup.ISourceLookupChangeListener;

/**
 * @author Clement chu
 * 
 */
public class PSourceLookupParticipant extends AbstractSourceLookupParticipant {
	static private class NoSourceElement {}

	private static final NoSourceElement gfNoSource = new NoSourceElement();
	private ListenerList fListeners;

	public PSourceLookupParticipant() {
		super();
		fListeners = new ListenerList(1);
	}
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof String) {
			return (String) object;
		}
		if (object instanceof IAdaptable) {
			IPStackFrame frame = (IPStackFrame) ((IAdaptable) object).getAdapter(IPStackFrame.class);
			if (frame != null) {
				String name = frame.getFile();
				return (name != null && name.trim().length() > 0) ? name : null;
			}
		}
		return null;
	}
	public Object[] findSourceElements(Object object) throws CoreException {
		// Workaround for cases when the stack frame doesn't contain the source file name
		String name = null;
		if (object instanceof IAdaptable) {
			IPStackFrame frame = (IPStackFrame) ((IAdaptable) object).getAdapter(IPStackFrame.class);
			if (frame != null) {
				name = frame.getFile().trim();
				if (name == null || name.length() == 0)
					return new Object[] { gfNoSource };
			}
		} else if (object instanceof String) {
			name = (String) object;
		}
		// Workaround. See bug #91808.
		if (name != null) {
			File file = new File(name);
			if (file.isAbsolute() && file.exists()) {
				return findSourceElementByFile(file);
			}
		}
		return super.findSourceElements(object);
	}
	private Object[] findSourceElementByFile(File file) {
		IFile[] wfiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(file.getPath()));
		if (wfiles.length > 0)
			return wfiles;
		return new LocalFileStorage[] { new LocalFileStorage(file) };
	}
	public void dispose() {
		fListeners.clear();
		super.dispose();
	}
	public void addSourceLookupChangeListener(ISourceLookupChangeListener listener) {
		fListeners.add(listener);
	}
	public void removeSourceLookupChangeListener(ISourceLookupChangeListener listener) {
		fListeners.remove(listener);
	}
	public void sourceContainersChanged(ISourceLookupDirector director) {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i)
			((ISourceLookupChangeListener) listeners[i]).sourceContainersChanged(director);
		super.sourceContainersChanged(director);
	}
}
