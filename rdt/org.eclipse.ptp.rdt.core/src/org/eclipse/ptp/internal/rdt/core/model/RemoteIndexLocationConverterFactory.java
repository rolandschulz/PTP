/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.internal.rdt.core.miners.SimpleLocationConverter;

/**
 * Returns an location converter usable by the remote index manager.
 *
 */
public class RemoteIndexLocationConverterFactory implements IIndexLocationConverterFactory {

	private String scheme;
	private String host;
	private DataStore datastore;
	
	
	public RemoteIndexLocationConverterFactory(String scheme, String host, DataStore datastore) {
		this.scheme = scheme;
		this.host = host;
		this.datastore = datastore;
	}


	public IIndexLocationConverter getConverter(ICProject project) {
		return new SimpleLocationConverter(scheme, host, datastore);
	}
	
	/**
	 * Convenience method because the project isn't actually available on the remote side.
	 */
	public IIndexLocationConverter getConverter() {
		return new SimpleLocationConverter(scheme, host, datastore);
	}

}
