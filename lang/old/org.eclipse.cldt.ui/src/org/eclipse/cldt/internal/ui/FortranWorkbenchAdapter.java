package org.eclipse.cldt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.core.model.IParent;
import org.eclipse.cldt.internal.ui.viewsupport.FortranElementImageProvider;
import org.eclipse.cldt.ui.CElementLabelProvider;
import org.eclipse.cldt.ui.FortranUIPlugin;

/**
 * An imlementation of the IWorkbenchAdapter for CElements.
 */
public class FortranWorkbenchAdapter implements IWorkbenchAdapter {

	private static final Object[] fgEmptyArray = new Object[0];
	private FortranElementImageProvider fImageProvider;
	private CElementLabelProvider fLabelProvider;

	public FortranWorkbenchAdapter() {
		fImageProvider = new FortranElementImageProvider();
		fLabelProvider = new CElementLabelProvider();
	}

	/**
	 * @see IWorkbenchAdapter#getChildren
	 */
	public Object[] getChildren(Object o) {
		if (o instanceof IParent) {
			try {
				Object[] members = ((IParent) o).getChildren();
				if (members != null) {
					return members;
				}
			} catch (CModelException e) {
				FortranUIPlugin.getDefault().log(e);
			}
		}
		return fgEmptyArray;
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor
	 */
	public ImageDescriptor getImageDescriptor(Object element) {
		if (element instanceof ICElement) {
			return fImageProvider.getCImageDescriptor(
				(ICElement) element,
				FortranElementImageProvider.OVERLAY_ICONS | FortranElementImageProvider.SMALL_ICONS);
		}
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getLabel
	 */
	public String getLabel(Object o) {
		if (o instanceof ICElement) {
			return fLabelProvider.getText(o);
		}
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getParent
	 */
	public Object getParent(Object o) {
		if (o instanceof ICElement) {
			return ((ICElement) o).getParent();
		}
		return null;
	}
}
