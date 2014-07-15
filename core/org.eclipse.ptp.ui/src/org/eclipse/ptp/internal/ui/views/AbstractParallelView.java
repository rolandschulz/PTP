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
package org.eclipse.ptp.internal.ui.views;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

/**
 * @author clement chu
 *
 */
public abstract class AbstractParallelView extends ViewPart {
	private boolean fIsVisible = false;
	private ParallelViewPartListener partListener = null;
	private final boolean debug = false;

	protected void registerPartListener() {
		if (partListener == null) {
			partListener = new ParallelViewPartListener();
			getSite().getPage().addPartListener(partListener);
		}
	}

	protected void deregisterPartListener() {
		if (partListener != null) {
			getSite().getPage().removePartListener(partListener);
			partListener = null;
		}
	}

	public abstract void repaint(boolean all);

	/**
	 * Notification this view is now visible.
	 */
	protected void becomesVisible() {
	}

	/**
	 * Notification this view is now hidden.
	 */
	protected void becomesHidden() {
	}

	public boolean isVisible() {
		return fIsVisible;
	}

	private class ParallelViewPartListener implements IPartListener2 {
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			if (debug) {
				System.err.println("-------------------- partActivated"); //$NON-NLS-1$
			}
			repaint(true);
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part == AbstractParallelView.this) {
				fIsVisible = false;
				becomesHidden();
			}
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part == AbstractParallelView.this) {
				fIsVisible = true;
				becomesVisible();
			}
		}
	}
}
