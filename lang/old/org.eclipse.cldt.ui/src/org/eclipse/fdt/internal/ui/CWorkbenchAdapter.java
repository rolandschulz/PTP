package org.eclipse.fdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.IParent;
import org.eclipse.fdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.fdt.ui.CElementLabelProvider;
import org.eclipse.fdt.ui.CUIPlugin;

/**
 * An imlementation of the IWorkbenchAdapter for CElements.
 */
public class CWorkbenchAdapter implements IWorkbenchAdapter {

	private static final Object[] fgEmptyArray = new Object[0];
	private CElementImageProvider fImageProvider;
	private CElementLabelProvider fLabelProvider;

	public CWorkbenchAdapter() {
		fImageProvider = new CElementImageProvider();
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
				CUIPlugin.getDefault().log(e);
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
				CElementImageProvider.OVERLAY_ICONS | CElementImageProvider.SMALL_ICONS);
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
