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
package org.eclipse.ptp.ui.views;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
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

	/** Get Display
	 * @return display
	 */
	protected Display getDisplay() {
		return getViewSite().getShell().getDisplay();
	}
	public void asyncExec(Runnable r) {
		if (isVisible())
			getDisplay().asyncExec(r);
	}
	public void showWhile(Runnable r) {
		if (isVisible())
			BusyIndicator.showWhile(getDisplay(), r);
	}
	
	public abstract void repaint(boolean all);
	
	/**
	 * Notification this view is now visible.
	 */
	protected void becomesVisible() {}
	/**
	 * Notification this view is now hidden.
	 */
	protected void becomesHidden() {}
	
	public boolean isVisible() {
		return fIsVisible;
	}
	private class ParallelViewPartListener implements IPartListener2 {
		public void partActivated(IWorkbenchPartReference partRef) {
			System.err.println("-------------------- partActivated");
			repaint(true);
		}
		public void partBroughtToTop(IWorkbenchPartReference partRef) {}
		public void partClosed(IWorkbenchPartReference partRef) {}
		public void partDeactivated(IWorkbenchPartReference partRef) {}
		public void partInputChanged(IWorkbenchPartReference partRef) {}
		public void partOpened(IWorkbenchPartReference partRef) {}
		public void partHidden(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part == AbstractParallelView.this) {
				fIsVisible = false;
				becomesHidden();
			}
		}
		public void partVisible(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part == AbstractParallelView.this) {
				fIsVisible = true;
				becomesVisible();
			}
		}
	}
}
