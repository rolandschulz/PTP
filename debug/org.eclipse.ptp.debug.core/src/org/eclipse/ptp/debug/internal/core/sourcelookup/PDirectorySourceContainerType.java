/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.sourcelookup; 

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.eclipse.ptp.debug.core.sourcelookup.PDirectorySourceContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
 
/**
 * See <code>CDirectorySourceContainer</code>.
 */
public class PDirectorySourceContainerType extends AbstractSourceContainerTypeDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer( String memento ) throws CoreException {
		Node node = parseDocument( memento );
		if ( node.getNodeType() == Node.ELEMENT_NODE ) {
			Element element = (Element)node;
			if ( "directory".equals( element.getNodeName() ) ) { //$NON-NLS-1$
				String string = element.getAttribute( "path" ); //$NON-NLS-1$
				if ( string == null || string.length() == 0 ) {
					abort( InternalSourceLookupMessages.getString( "CDirectorySourceContainerType.0" ), null ); //$NON-NLS-1$
				}
				String nest = element.getAttribute( "nest" ); //$NON-NLS-1$
				boolean nested = "true".equals( nest ); //$NON-NLS-1$
				return new PDirectorySourceContainer( new Path( string ), nested );
			}
			abort( InternalSourceLookupMessages.getString( "CDirectorySourceContainerType.1" ), null ); //$NON-NLS-1$
		}
		abort( InternalSourceLookupMessages.getString( "CDirectorySourceContainerType.2" ), null ); //$NON-NLS-1$
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento( ISourceContainer container ) throws CoreException {
		PDirectorySourceContainer folder = (PDirectorySourceContainer)container;
		Document document = newDocument();
		Element element = document.createElement( "directory" ); //$NON-NLS-1$
		element.setAttribute( "path", folder.getDirectory().getAbsolutePath() ); //$NON-NLS-1$
		String nest = "false"; //$NON-NLS-1$
		if ( folder.isComposite() ) {
			nest = "true"; //$NON-NLS-1$
		}
		element.setAttribute( "nest", nest ); //$NON-NLS-1$
		document.appendChild( element );
		return serializeDocument( document );
	}
}
