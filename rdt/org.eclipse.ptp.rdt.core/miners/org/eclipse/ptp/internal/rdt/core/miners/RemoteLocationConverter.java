/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;

/**
 * @author crecoskie
 *
 */
public class RemoteLocationConverter implements IIndexLocationConverter {

	private DataStore _dataStore = null;
	private static final String CLASS_NAME = "CDTMiner-RemoteLocationConverter"; //$NON-NLS-1$
	
	public RemoteLocationConverter(DataStore _dataStore) {
		this._dataStore = _dataStore;
	}

	public RemoteLocationConverter() {
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexLocationConverter#fromInternalFormat(java.lang.String)
	 */
	public IIndexFileLocation fromInternalFormat(String raw) {
		try {
			//the imported index file is relative
			File rawFile = new File(raw);
			if(rawFile.isAbsolute()){
				return new IndexFileLocation(new URI("file", null, raw, null, null), null); //$NON-NLS-1$
			}else{
				return new IndexFileLocation(new URI(raw), null);
			}
		} catch (URISyntaxException e) {
			if (_dataStore != null) {
				UniversalServerUtilities.logError(CLASS_NAME, e.toString(), e, _dataStore );
			}
			else {
				StandaloneLogService.getInstance().errorLog(CLASS_NAME+":"+ e.toString(), e); //$NON-NLS-1$
			}
		}
		
		return null;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexLocationConverter#toInternalFormat(org.eclipse.cdt.core.index.IIndexFileLocation)
	 */
	public String toInternalFormat(IIndexFileLocation location) {
		return location.getURI().getPath();
	}

}
