package org.eclipse.ptp.internal.ui.search;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Clement
 *
 */
public class SelectionConverter {
	public static IStructuredSelection getStructuredSelection(IWorkbenchPart part) {
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof IStructuredSelection)
				return (IStructuredSelection)selection;
		}
		return StructuredSelection.EMPTY;
	}	
}
