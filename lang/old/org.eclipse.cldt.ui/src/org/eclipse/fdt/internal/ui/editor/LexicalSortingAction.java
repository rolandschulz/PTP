package org.eclipse.fdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;


import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.internal.ui.CPluginImages;
import org.eclipse.fdt.ui.CUIPlugin;


public class LexicalSortingAction extends Action {
	
	private static final String ACTION_NAME= "LexicalSortingAction"; //$NON-NLS-1$
	private static final String DIALOG_STORE_KEY= ACTION_NAME + ".sort"; //$NON-NLS-1$
	
	private LexicalCSorter fSorter;
	private TreeViewer fTreeViewer;
	
	public LexicalSortingAction(TreeViewer treeViewer) {
		super(CUIPlugin.getResourceString(ACTION_NAME + ".label")); //$NON-NLS-1$
		
		setDescription(CUIPlugin.getResourceString(ACTION_NAME + ".description")); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString(ACTION_NAME + ".tooltip")); //$NON-NLS-1$
	
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_ALPHA_SORTING);
	
		fTreeViewer= treeViewer;
		fSorter= new LexicalCSorter();
		
		boolean checked= CUIPlugin.getDefault().getDialogSettings().getBoolean(DIALOG_STORE_KEY);
		valueChanged(checked, false);
	}
	
	public void run() {
		valueChanged(isChecked(), true);
	}
	
	private void valueChanged(boolean on, boolean store) {
		setChecked(on);
		fTreeViewer.setSorter(on ? fSorter : null);
		
		String key= ACTION_NAME + ".tooltip" + (on ? ".on" : ".off"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setToolTipText(CUIPlugin.getResourceString(key));
		
		if (store) {
			CUIPlugin.getDefault().getDialogSettings().put(DIALOG_STORE_KEY, on);
		}
	}
	
	private class LexicalCSorter extends ViewerSorter {		
		public boolean isSorterProperty(Object element, Object property) {
			return true;
		}
		
		public int category(Object obj) {
			if (obj instanceof ICElement) {
				ICElement elem= (ICElement)obj;
				switch (elem.getElementType()) {
					case ICElement.C_MACRO: return 1;
					case ICElement.C_INCLUDE: return 2;
					
					case ICElement.C_CLASS: return 3;
					case ICElement.C_STRUCT: return 4;
					case ICElement.C_UNION: return 5;
					
					case ICElement.C_FIELD: return 6;
					case ICElement.C_FUNCTION: return 7;		
				}
				
			}
			return 0;
		}
	}
	
}
