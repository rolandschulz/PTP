/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.fdt.utils.som.parser;

import java.io.EOFException;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.AbstractCExtension;
import org.eclipse.fdt.core.CCorePlugin;
import org.eclipse.fdt.core.IBinaryParser;
import org.eclipse.fdt.utils.DefaultGnuToolFactory;
import org.eclipse.fdt.utils.IGnuToolFactory;
import org.eclipse.fdt.utils.som.AR;
import org.eclipse.fdt.utils.som.SOM;

/**
 * HP-UX SOM binary parser
 * 
 * @author vhirsl
 */
public class SOMParser extends AbstractCExtension implements IBinaryParser {
	private DefaultGnuToolFactory toolFactory;

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser#getBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(byte[] hints, IPath path) throws IOException {
		if (path == null) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.nullPath")); //$NON-NLS-1$
		}

		IBinaryFile binary = null;
		if (isBinary(hints, path)) {
			try {
				SOM.Attribute attribute = null;
				if (hints != null && hints.length > 0) {
					try {
						attribute = SOM.getAttributes(hints);
					} catch (EOFException eof) {
						// continue, the array was to small.
					}
				}
	
				//Take a second run at it if the data array failed. 			
	 			if(attribute == null) {
					attribute = SOM.getAttributes(path.toOSString());
	 			}
	
				if (attribute != null) {
					switch (attribute.getType()) {
						case SOM.Attribute.SOM_TYPE_EXE :
							binary = createBinaryExecutable(path);
							break;
	
						case SOM.Attribute.SOM_TYPE_SHLIB :
							binary = createBinaryShared(path);
							break;
	
						case SOM.Attribute.SOM_TYPE_OBJ :
							binary = createBinaryObject(path);
							break;
	
						case SOM.Attribute.SOM_TYPE_CORE :
							binary = createBinaryCore(path);
							break;
					}
				}
			} catch (IOException e) {
				binary = createBinaryArchive(path);
			}
		}
		return binary;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		return getBinary(null, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "SOM"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser#isBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	public boolean isBinary(byte[] hints, IPath path) {
		return SOM.isSOMHeader(hints) || AR.isARHeader(hints);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser#getHintBufferSize()
	 */
	public int getHintBufferSize() {
		return 512;	// size of file header
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryExecutable createBinaryExecutable(IPath path) {
		return new SOMBinaryExecutable(this, path);
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryShared createBinaryShared(IPath path) {
		return new SOMBinaryShared(this, path);
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryObject createBinaryObject(IPath path) {
		return new SOMBinaryObject(this, path, IBinaryFile.OBJECT);
	}

	/**
	 * @param path
	 * @return
	 */
	private IBinaryObject createBinaryCore(IPath path) {
		return new SOMBinaryObject(this, path, IBinaryFile.CORE);
	}

	/**
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new SOMBinaryArchive(this, path);
	}

	/**
	 * @return
	 */
	protected DefaultGnuToolFactory createGNUToolFactory() {
		return new DefaultGnuToolFactory(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IGnuToolFactory.class)) {
			if (toolFactory == null) {
				toolFactory = createGNUToolFactory();
			}
			return toolFactory;
		}
		return super.getAdapter(adapter);
	}
}
