/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import java.util.EventListener;

public interface IRemoteFastIndexerListener extends EventListener {
	/**
	 * Notifies this listener that the indexer is updating 
	 * @param event The update event
	 */
	public void indexerUpdating(IRemoteFastIndexerUpdateEvent event);
}
