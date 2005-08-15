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
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.sourcelookup;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * Locates source elements in a Java project. Returns instances of <code>IFile</code>.
 * 
 * @since Sep 23, 2002
 */
public class CProjectSourceLocation implements IProjectSourceLocation
{
	private static final String ELEMENT_NAME = "cProjectSourceLocation"; //$NON-NLS-1$
	private static final String ATTR_PROJECT = "project"; //$NON-NLS-1$
	private static final String ATTR_GENERIC = "generic"; //$NON-NLS-1$

	/**
	 * The project associated with this source location
	 */
	private IProject fProject;

	private IResource[] fFolders;

	private HashMap fCache = new HashMap( 20 );
	
	private HashSet fNotFoundCache = new HashSet( 20 );

	private boolean fGenerated = true;
	
	private boolean fSearchForDuplicateFiles = false;

	/**
	 * Constructor for CProjectSourceLocation.
	 */
	public CProjectSourceLocation()
	{
	}

	/**
	 * Constructor for CProjectSourceLocation.
	 */
	public CProjectSourceLocation( IProject project )
	{
		setProject( project );
		fGenerated = true;
	}

	/**
	 * Constructor for CProjectSourceLocation.
	 */
	public CProjectSourceLocation( IProject project, boolean generated )
	{
		setProject( project );
		fGenerated = generated;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#findSourceElement(String)
	 */
	public Object findSourceElement( String name ) throws CoreException
	{
		Object result = null;
		if ( !isEmpty( name ) && getProject() != null && !notFoundCacheLookup( name ) )
		{
			result = cacheLookup( name );
			if ( result == null )
			{ 
				result = doFindSourceElement( name );
				if ( result != null )
				{
					cacheSourceElement( name, result );
				}
			}
			if ( result == null )
			{
				cacheNotFound( name );
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( ICSourceLocation.class ) )
			return this;
		if ( adapter.equals( CProjectSourceLocation.class ) )
			return this;
		if ( adapter.equals( IProject.class ) )
			return getProject();
		return null;
	}

	/**
	 * Sets the project in which source elements will be searched for.
	 * 
	 * @param project the project
	 */
	private void setProject( IProject project )
	{
		fProject = project;
	}

	/**
	 * Returns the project associated with this source location.
	 * 
	 * @return project
	 */
	public IProject getProject()
	{
		return fProject;
	}

	private Object doFindSourceElement( String name )
	{
		File file = new File( name );
		return ( file.isAbsolute() ) ? findFileByAbsolutePath( file ) : findFileByRelativePath( name );
	}

	private Object findFileByAbsolutePath( File file )
	{
		LinkedList list = new LinkedList();
		if ( file.exists() )
		{
			IPath path = new Path( file.getAbsolutePath() );
			IFile[] wsFiles = PTPDebugCorePlugin.getWorkspace().getRoot().findFilesForLocation( path );
			for ( int i = 0; i < wsFiles.length; ++i )
				if ( wsFiles[i].getProject().equals( getProject() ) && wsFiles[i].exists() )
				{
					if ( !searchForDuplicateFiles() )
						return wsFiles[i];
					list.add( wsFiles[i] );
				}
		}
		return ( list.size() > 0 ) ? ( ( list.size() == 1 ) ? list.getFirst() : list ) : null;
	}

	private Object findFileByRelativePath( String fileName )
	{
		IResource[] folders = getFolders();
		LinkedList list = new LinkedList();
		for ( int i = 0; i < folders.length; ++i )
		{
			if ( list.size() > 0 && !searchForDuplicateFiles() )
				break;
			IPath path = folders[i].getLocation();
			if ( path != null ) {
				path = path.append( fileName );
				File file = new File( path.toOSString() );
				if ( file.exists() )
				{
					IFile[] wsFiles = PTPDebugCorePlugin.getWorkspace().getRoot().findFilesForLocation( path );
					for ( int j = 0; j < wsFiles.length; ++j )
						if ( wsFiles[j].exists() )
						{
							if ( !searchForDuplicateFiles() )
								return wsFiles[j];
							list.add( wsFiles[j] );
						}
				}
			}
		}
		return ( list.size() > 0 ) ? ( ( list.size() == 1 ) ? list.getFirst() : list ) : null;
	}

	private Object cacheLookup( String name )
	{
		return fCache.get( name );
	}
	
	private boolean notFoundCacheLookup( String name )
	{
		return fNotFoundCache.contains( name );
	}
	
	private void cacheSourceElement( String name, Object element )
	{
		fCache.put( name, element );
	}

	private void cacheNotFound( String name )
	{
		fNotFoundCache.add( name );
	}

	public void dispose()
	{
		fCache.clear();
		fNotFoundCache.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#getMemento()
	 */
	public String getMemento() throws CoreException
	{
        Document document = null;
        Throwable ex = null;
        try 
		{
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element node = document.createElement( ELEMENT_NAME );
            document.appendChild( node );
    		node.setAttribute( ATTR_PROJECT, getProject().getName() );
    		node.setAttribute( ATTR_GENERIC, new Boolean( isGeneric() ).toString() );
			return CDebugUtils.serializeDocument( document );
        }
        catch( ParserConfigurationException e ) 
		{
        	ex = e;
        }
		catch( IOException e )
		{
			ex = e;
		}
		catch( TransformerException e )
		{
			ex = e;
		}
		abort( MessageFormat.format( InternalSourceLookupMessages.getString( "CProjectSourceLocation.0" ), new String[] { getProject().getName() } ), ex ); //$NON-NLS-1$
		// execution will not reach here
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#initializeFrom(java.lang.String)
	 */
	public void initializeFrom( String memento ) throws CoreException
	{
		Exception ex = null;
		try
		{
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader( memento );
			InputSource source = new InputSource( reader );
			root = parser.parse( source ).getDocumentElement();

			String name = root.getAttribute( ATTR_PROJECT );
			if ( isEmpty( name ) )
			{
				abort( InternalSourceLookupMessages.getString( "CProjectSourceLocation.1" ), null ); //$NON-NLS-1$
			}
			else
			{
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( name );
				setProject( project );
			}
			String isGeneric = root.getAttribute( ATTR_GENERIC );
			if ( isGeneric == null || isGeneric.trim().length() == 0 )
				isGeneric = Boolean.FALSE.toString();
			setGenerated( isGeneric.equals( Boolean.TRUE.toString() ) );
			return;
		}
		catch( ParserConfigurationException e )
		{
			ex = e;
		}
		catch( SAXException e )
		{
			ex = e;
		}
		catch( IOException e )
		{
			ex = e;
		}
		abort( InternalSourceLookupMessages.getString( "CProjectSourceLocation.2" ), ex ); //$NON-NLS-1$
	}

	/**
	 * Throws an internal error exception
	 */
	private void abort( String message, Throwable e ) throws CoreException
	{
		IStatus s = new Status( IStatus.ERROR,
								PTPDebugCorePlugin.getUniqueIdentifier(),
								PTPDebugCorePlugin.INTERNAL_ERROR,
								message,
								e );
		throw new CoreException( s );
	}

	private boolean isEmpty( String string )
	{
		return string == null || string.length() == 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation#isGenerated()
	 */
	public boolean isGeneric()
	{
		return fGenerated;
	}

	public void setGenerated( boolean b )
	{
		fGenerated = b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object obj )
	{
		if ( obj instanceof IProjectSourceLocation && getProject() != null )
			return getProject().equals( ((IProjectSourceLocation)obj).getProject() );
		return false;
	}

	private void initializeFolders()
	{
		final LinkedList list = new LinkedList();
		if ( getProject() != null && getProject().exists() )
		{
			list.add( getProject() );
			try
			{
				getProject().accept( 
						new IResourceProxyVisitor()
							{
								public boolean visit( IResourceProxy proxy ) throws CoreException
								{
									switch( proxy.getType() )
									{
										case IResource.FILE:
											return false;
										case IResource.FOLDER:
											list.addLast( proxy.requestResource() );
											return true;
									}
									return true;
								}
	
							}, 
						IResource.NONE );
			}
			catch( CoreException e )
			{
			}
		}
		synchronized( this ) 
		{
			if ( fFolders == null ) 
			{
				fFolders = (IResource[])list.toArray( new IResource[list.size()] );
			}
		}
	}

	protected IResource[] getFolders()
	{
		if ( fFolders == null )
			initializeFolders();
		return fFolders;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#searchForDuplicateFiles()
	 */
	public boolean searchForDuplicateFiles()
	{
		return fSearchForDuplicateFiles;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#setSearchForDuplicateFiles(boolean)
	 */
	public void setSearchForDuplicateFiles( boolean search )
	{
		fCache.clear();
		fNotFoundCache.clear();
		fSearchForDuplicateFiles = search;
	}

	public String toString()
	{
		return ( getProject() != null ) ? fProject.toString() : ""; //$NON-NLS-1$
	}
}
