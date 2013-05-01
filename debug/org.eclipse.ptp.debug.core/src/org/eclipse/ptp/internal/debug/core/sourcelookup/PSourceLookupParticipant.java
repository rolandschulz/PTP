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
package org.eclipse.ptp.internal.debug.core.sourcelookup;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.ptp.debug.core.model.IPStackFrame;

/**
 * @author Clement chu
 * 
 */
public class PSourceLookupParticipant extends AbstractSourceLookupParticipant {
	private static class NoSourceElement {
	}

	private static final NoSourceElement gfNoSource = new NoSourceElement();
	private final ListenerList fListeners = new ListenerList(1);

	/**
	 * Add a listener for source lookup changes.
	 * 
	 * @param listener
	 */
	public void addSourceLookupChangeListener(ISourceLookupChangeListener listener) {
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#dispose
	 * ()
	 */
	@Override
	public void dispose() {
		fListeners.clear();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#
	 * findSourceElements(java.lang.Object)
	 */
	@Override
	public Object[] findSourceElements(Object object) throws CoreException {
		// Workaround for cases when the stack frame doesn't contain the source
		// file name
		String name = null;
		if (object instanceof IAdaptable) {
			IPStackFrame frame = (IPStackFrame) ((IAdaptable) object).getAdapter(IPStackFrame.class);
			if (frame != null) {
				name = frame.getFile().trim();
				if (name == null || name.length() == 0) {
					return new Object[] { gfNoSource };
				}
			}
		} else if (object instanceof String) {
			name = (String) object;
		}
		Object[] foundElements = super.findSourceElements(object);
		if (foundElements.length == 0 && (object instanceof IDebugElement)) {
			if (new File(name).exists()) {
				foundElements = new AbsolutePathSourceContainer().findSourceElements(name);
			} else {
				foundElements = new Object[] { new PSourceNotFoundElement((IDebugElement) object) };
			}
		}
		return foundElements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#getSourceName
	 * (java.lang.Object)
	 */
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

	/**
	 * Remove a listener for source lookup changes.
	 * 
	 * @param listener
	 */
	public void removeSourceLookupChangeListener(ISourceLookupChangeListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#
	 * sourceContainersChanged
	 * (org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	@Override
	public void sourceContainersChanged(ISourceLookupDirector director) {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((ISourceLookupChangeListener) listeners[i]).sourceContainersChanged(director);
		}
		super.sourceContainersChanged(director);
	}
}
