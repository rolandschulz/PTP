/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.sourcelookup;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.sourcelookup.CDirectorySourceContainer;
import org.eclipse.ptp.debug.core.sourcelookup.MappingSourceContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SourceUtils {

	private static final String NAME_COMMON_SOURCE_LOCATIONS = "commonSourceLocations"; //$NON-NLS-1$

	private static final String NAME_SOURCE_LOCATION = "sourceLocation"; //$NON-NLS-1$

	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	private static final String ATTR_MEMENTO = "memento"; //$NON-NLS-1$

	public static String getCommonSourceLocationsMemento( ICSourceLocation[] locations ) {
		Document document = null;
		Throwable ex = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element element = document.createElement( NAME_COMMON_SOURCE_LOCATIONS );
			document.appendChild( element );
			saveSourceLocations( document, element, locations );
			return CDebugUtils.serializeDocument( document );
		}
		catch( ParserConfigurationException e ) {
			ex = e;
		}
		catch( IOException e ) {
			ex = e;
		}
		catch( TransformerException e ) {
			ex = e;
		}
		PTPDebugCorePlugin.log( new Status( IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0, "Error saving common source settings.", ex ) ); //$NON-NLS-1$
		return null;
	}

	private static void saveSourceLocations( Document doc, Element node, ICSourceLocation[] locations ) {
		for( int i = 0; i < locations.length; i++ ) {
			Element child = doc.createElement( NAME_SOURCE_LOCATION );
			child.setAttribute( ATTR_CLASS, locations[i].getClass().getName() );
			try {
				child.setAttribute( ATTR_MEMENTO, locations[i].getMemento() );
			}
			catch( CoreException e ) {
				PTPDebugCorePlugin.log( e );
				continue;
			}
			node.appendChild( child );
		}
	}

	public static ICSourceLocation[] getCommonSourceLocationsFromMemento( String memento ) {
		ICSourceLocation[] result = new ICSourceLocation[0];
		if ( !isEmpty( memento ) ) {
			try {
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				StringReader reader = new StringReader( memento );
				InputSource source = new InputSource( reader );
				Element root = parser.parse( source ).getDocumentElement();
				if ( root.getNodeName().equalsIgnoreCase( NAME_COMMON_SOURCE_LOCATIONS ) )
					result = initializeSourceLocations( root );
			}
			catch( ParserConfigurationException e ) {
				PTPDebugCorePlugin.log( new Status( IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0, "Error initializing common source settings.", e ) ); //$NON-NLS-1$
			}
			catch( SAXException e ) {
				PTPDebugCorePlugin.log( new Status( IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0, "Error initializing common source settings.", e ) ); //$NON-NLS-1$
			}
			catch( IOException e ) {
				PTPDebugCorePlugin.log( new Status( IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0, "Error initializing common source settings.", e ) ); //$NON-NLS-1$
			}
		}
		return result;
	}

	public static ICSourceLocation[] initializeSourceLocations( Element root ) {
		List sourceLocations = new LinkedList();
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for( int i = 0; i < length; ++i ) {
			Node node = list.item( i );
			short type = node.getNodeType();
			if ( type == Node.ELEMENT_NODE ) {
				Element entry = (Element)node;
				if ( entry.getNodeName().equalsIgnoreCase( NAME_SOURCE_LOCATION ) ) {
					String className = entry.getAttribute( ATTR_CLASS );
					String data = entry.getAttribute( ATTR_MEMENTO );
					if ( className == null || className.trim().length() == 0 ) {
						PTPDebugCorePlugin.log( "Unable to restore common source locations - invalid format." ); //$NON-NLS-1$
						continue;
					}
					Class clazz = null;
					try {
						clazz = PTPDebugCorePlugin.getDefault().getBundle().loadClass( className );
					}
					catch( ClassNotFoundException e ) {
						PTPDebugCorePlugin.log( MessageFormat.format( "Unable to restore source location - class not found {0}", new String[]{ className } ) ); //$NON-NLS-1$
						continue;
					}
					ICSourceLocation location = null;
					try {
						location = (ICSourceLocation)clazz.newInstance();
					}
					catch( IllegalAccessException e ) {
						PTPDebugCorePlugin.log( "Unable to restore source location: " + e.getMessage() ); //$NON-NLS-1$
						continue;
					}
					catch( InstantiationException e ) {
						PTPDebugCorePlugin.log( "Unable to restore source location: " + e.getMessage() ); //$NON-NLS-1$
						continue;
					}
					try {
						location.initializeFrom( data );
						sourceLocations.add( location );
					}
					catch( CoreException e ) {
						PTPDebugCorePlugin.log( "Unable to restore source location: " + e.getMessage() ); //$NON-NLS-1$
					}
				}
			}
		}
		return (ICSourceLocation[])sourceLocations.toArray( new ICSourceLocation[sourceLocations.size()] );
	}

	private static boolean isEmpty( String string ) {
		return (string == null || string.trim().length() == 0);
	}

	static public ISourceContainer[] convertSourceLocations( ICSourceLocation[] locations ) {
		ArrayList containers = new ArrayList( locations.length );
		int mappingCount = 0;
		for ( int i = 0; i < locations.length; ++i ) {
			if ( locations[i] instanceof IProjectSourceLocation ) {
				containers.add( new ProjectSourceContainer( ((IProjectSourceLocation)locations[i]).getProject(), false ) );
			}
			else if ( locations[i] instanceof IDirectorySourceLocation ) {
				IDirectorySourceLocation d = (IDirectorySourceLocation)locations[i];
				IPath a = d.getAssociation();
				if ( a != null ) {
					MappingSourceContainer mapping = new MappingSourceContainer( InternalSourceLookupMessages.getString( "SourceUtils.0" ) + (++mappingCount) ); //$NON-NLS-1$
					mapping.addMapEntries( new MapEntrySourceContainer[] { new MapEntrySourceContainer( a, d.getDirectory() ) } );
					containers.add( mapping );
					
				}
				containers.add( new CDirectorySourceContainer( d.getDirectory(), d.searchSubfolders() ) );
			}
		}
		return (ISourceContainer[])containers.toArray( new ISourceContainer[containers.size()] );
	}
}
