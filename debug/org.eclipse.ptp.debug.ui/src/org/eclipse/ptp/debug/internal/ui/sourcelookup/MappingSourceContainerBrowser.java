package org.eclipse.ptp.debug.internal.ui.sourcelookup;

import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.swt.widgets.Shell;

/**
 * @author clement
 *
 */
public class MappingSourceContainerBrowser extends AbstractSourceContainerBrowser {
	private static final String MAPPING = SourceLookupUIMessages.getString("MappingSourceContainerBrowser.0");

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#addSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		return new ISourceContainer[] { new MappingSourceContainer(generateName(director)) };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canAddSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	public boolean canAddSourceContainers(ISourceLookupDirector director) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#canEditSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers) {
		return (containers.length == 1 && containers[0] instanceof MappingSourceContainer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser#editSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.sourcelookup.ISourceLookupDirector, org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers) {
		if (containers.length == 1 && containers[0] instanceof MappingSourceContainer) {
			PathMappingDialog dialog = new PathMappingDialog(shell, (MappingSourceContainer)containers[0]);
			if (dialog.open() == Window.OK) {
				return new ISourceContainer[] { dialog.getMapping() };
			}
		}
		return new ISourceContainer[0];
	}

	private String generateName(ISourceLookupDirector director) {
//		int counter = 1;
//		ISourceContainer[] containers = director.getSourceContainers();
//		for (int i = 0; i < containers.length; ++i) {
//			if (MappingSourceContainer.TYPE_ID.equals(containers[i].getType().getId())) {
//				String name = containers[i].getName(); 
//				if (name.startsWith(MAPPING)) {
//					try {
//						int number = Integer.valueOf(name.substring(MAPPING.length())).intValue();
//						if (number == counter)
//							++counter;
//					}
//					catch(NumberFormatException e) {
//					}
//				}
//			}
//		}
//		return MAPPING + counter;
		return MAPPING;
	}
}
