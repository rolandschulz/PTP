/****************************
 * IBM Confidential
 * Licensed Materials - Property of IBM
 *
 * IBM Rational Developer for Power Systems Software
 * IBM Rational Team Concert for Power Systems Software
 *
 * (C) Copyright IBM Corporation 2011.
 *
 * The source code for this program is not published or otherwise divested of its trade secrets, 
 * irrespective of what has been deposited with the U.S. Copyright Office.
 */
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
