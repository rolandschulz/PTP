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
package org.eclipse.ptp.internal.debug.ui.views.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.AbstractBreakpointOrganizerDelegate;
import org.eclipse.debug.ui.BreakpointTypeCategory;
import org.eclipse.debug.ui.IBreakpointTypeCategory;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.internal.debug.ui.PDebugImage;

/**
 * @author Clement chu
 * 
 */
public class PBreakpointSetOrganizer extends AbstractBreakpointOrganizerDelegate {
	private final Map<String, IAdaptable[]> types = new HashMap<String, IAdaptable[]>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public IAdaptable[] getCategories(IBreakpoint breakpoint) {
		if (!(breakpoint instanceof IPBreakpoint)) {
			return null;
		}

		IBreakpointTypeCategory category = (IBreakpointTypeCategory) breakpoint.getAdapter(IBreakpointTypeCategory.class);
		if (category != null) {
			return new IAdaptable[] { category };
		}

		IPBreakpoint pBpt = (IPBreakpoint) breakpoint;
		try {
			String sid = pBpt.getSetId();
			if (sid.length() > 0) {
				IAdaptable[] categories = types.get(sid);
				if (category == null) {
					categories = new IAdaptable[] { new BreakpointTypeCategory(sid,
							PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_PTPBPTSET)) };
					types.put(sid, categories);
				}
				return categories;
			}
		} catch (CoreException e) {
			return null;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#dispose()
	 */
	@Override
	public void dispose() {
		types.clear();
	}
}
