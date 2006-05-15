/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.rm.ui.views;

import org.eclipse.ptp.internal.rm.ui.views.AbstractElementsView;
import org.eclipse.ptp.internal.rm.ui.views.AbstractResourceManagerListener;
import org.eclipse.ptp.internal.rm.ui.views.IRMElementsProvider;
import org.eclipse.ptp.internal.rm.ui.views.IStatusDisplayProvider;
import org.eclipse.ptp.internal.rm.ui.views.StatusDisplayProviderFactory;
import org.eclipse.ptp.rm.core.IRMElement;
import org.eclipse.ptp.rm.core.IRMNode;
import org.eclipse.ptp.rm.core.IRMResourceManager;
import org.eclipse.ptp.rm.core.attributes.IAttrDesc;
import org.eclipse.ptp.rm.core.events.IRMResourceManagerListener;
import org.eclipse.ptp.rm.core.events.RMNodesChangedEvent;
import org.eclipse.ptp.rm.core.events.RMResourceManagerEvent;

public class NodesView extends AbstractElementsView {

	/**
	 * Factored out class to provide the IRMNode's needed for this view.
	 * 
	 * @author rsqrd
	 *
	 */
	private static class ElementsProvider implements IRMElementsProvider {
	
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.ui.views.IElementDisplayProvider#getElementAttrDescs(org.eclipse.ptp.rm.core.IRMResourceManager)
		 */
		public IAttrDesc[] getElementAttrDescs(IRMResourceManager manager) {
			return manager.getNodeAttrDescs();
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.ui.views.IElementDisplayProvider#getElements(org.eclipse.ptp.rm.core.IRMResourceManager)
		 */
		public IRMElement[] getElements(IRMResourceManager manager) {
			return manager.getAllNodes();
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.ui.views.IElementDisplayProvider#getNameFieldName()
		 */
		public String getNameFieldName() {
			return "Node";
		}
	
		public IStatusDisplayProvider getStatus(IRMElement element) {
			return StatusDisplayProviderFactory.create(((IRMNode)element).getStatus());
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.rm.ui.views.IElementDisplayProvider#hasStatus()
		 */
		public boolean hasStatus() {
			return true;
		}
	}

	/**
	 * Respond to changes in the Nodes from the resource manager.
	 * 
	 * @author rsqrd
	 *
	 */
	private final class ResourceManagerListener extends AbstractResourceManagerListener {
		public void nodesChanged(RMNodesChangedEvent event) {
			switch (event.getType()) {
			case RMResourceManagerEvent.MODIFIED:
				elementsModified(event.getNodes());
				break;
			case RMResourceManagerEvent.ADDED:
				elementsAdded(event.getNodes());
				break;
			case RMResourceManagerEvent.REMOVED:
				elementsRemoved(event.getNodes());
				break;
			default:
				throw new IllegalStateException("unknown event type");	
			}
		}
	}

	private final AbstractResourceManagerListener listener = new ResourceManagerListener();

	public NodesView() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rm.ui.views.AbstractElementsView#createElementsProvider()
	 */
	protected IRMElementsProvider createElementsProvider() {
		return new ElementsProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rm.ui.views.AbstractElementsView#getListener()
	 */
	protected IRMResourceManagerListener getListener() {
		return listener;
	}

}
