/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.THDropTargetListener
 * Version: 1.2
 */
package org.eclipse.ptp.internal.rdt.ui.typehierarchy;

import java.util.Iterator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.typehierarchy.TypeHierarchyUI;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.LocalTypeHierarchyService;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;


public class THDropTargetListener implements DropTargetListener {
	private static final ITypeHierarchyService fService = new LocalTypeHierarchyService();
	private ICElement fInput;
	private boolean fEnabled= true;
	private IWorkbenchWindow fWindow;

    public THDropTargetListener(RemoteTHViewPart view) {
    	fWindow= view.getSite().getWorkbenchWindow();
    }
    
    public void setEnabled(boolean val) {
    	fEnabled= val;
    }
    
    public void dragEnter(DropTargetEvent event) {
    	fInput= null;
        checkOperation(event);
        if (event.detail != DND.DROP_NONE) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
				fInput= checkLocalSelection();
				if (!TypeHierarchyUI.isValidInput(fInput)) {
					event.detail= DND.DROP_NONE;
					fInput= null;
				}
        	}
        }
    }

	private ICElement checkLocalSelection() {
		ISelection sel= LocalSelectionTransfer.getTransfer().getSelection();
		if (sel instanceof IStructuredSelection) {
			for (Iterator iter = ((IStructuredSelection)sel).iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof ICElement) {
					return (ICElement) element;
				}
				if (element instanceof IAdaptable) {
					ICElement adapter= (ICElement) ((IAdaptable) element).getAdapter(ICElement.class);
					if (adapter != null) {
						return adapter;
					}
				}
			}
		}
		return null;
	}

    public void dragLeave(DropTargetEvent event) {
    }

    public void dragOperationChanged(DropTargetEvent event) {
        checkOperation(event);
    }

    public void dragOver(DropTargetEvent event) {
    }

    public void drop(DropTargetEvent event) {
    	if (fInput == null) {
            Display.getCurrent().beep();
        }
        else {
        	TypeHierarchyUtil.open(fService, fInput, fWindow);
        }
    }

    public void dropAccept(DropTargetEvent event) {
    }
    
    private void checkOperation(DropTargetEvent event) {
        if (fEnabled && (event.operations & DND.DROP_COPY) != 0) {
            event.detail= DND.DROP_COPY;
        }
        else {
            event.detail= DND.DROP_NONE;
        }
    }
}
