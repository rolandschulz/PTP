/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.internal.core.search.indexing;

/**
 * @author bgheorgh
*/

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.fdt.core.FortranCorePlugin;
import org.eclipse.fdt.core.ICLogConstants;
import org.eclipse.fdt.core.index.IIndexDelta;
import org.eclipse.fdt.core.model.CoreModel;
import org.eclipse.fdt.core.parser.CodeReader;
import org.eclipse.fdt.core.parser.IParser;
import org.eclipse.fdt.core.parser.IScannerInfo;
import org.eclipse.fdt.core.parser.IScannerInfoProvider;
import org.eclipse.fdt.core.parser.ParseError;
import org.eclipse.fdt.core.parser.ParserFactory;
import org.eclipse.fdt.core.parser.ParserFactoryError;
import org.eclipse.fdt.core.parser.ParserLanguage;
import org.eclipse.fdt.core.parser.ParserMode;
import org.eclipse.fdt.core.parser.ParserTimeOut;
import org.eclipse.fdt.core.parser.ParserUtil;
import org.eclipse.fdt.core.parser.ScannerInfo;
import org.eclipse.fdt.internal.core.index.IDocument;
import org.eclipse.fdt.internal.core.index.impl.IndexDelta;

/**
 * A SourceIndexer indexes source files using the parser. The following items are indexed:
 * Declarations:
 * - Classes
 * - Structs
 * - Unions
 * References:
 * - Classes
 * - Structs
 * - Unions
 */
public class SourceIndexer extends AbstractIndexer {
	 
	//TODO: Indexer, add additional file types
	//Header files: "h" , "hh", "hpp"
	//Use the CModelManager defined file types
	//public static final String[] FILE_TYPES= new String[] {"cpp","c", "cc", "cxx"}; //$NON-NLS-1$
	
	//protected DefaultProblemFactory problemFactory= new DefaultProblemFactory(Locale.getDefault());
	public static final String FDT_INDEXER_TIMEOUT= "FDT_INDEXER_TIMEOUT"; //$NON-NLS-1$
	
	IFile resourceFile;
	ParserTimeOut timeOut = null;
	
	/**
	 * @param resource
	 * @param out
	 */
	public SourceIndexer(IFile resource, ParserTimeOut timeOut) {
		this.resourceFile = resource;
		this.timeOut = timeOut;
	}
	
	protected void indexFile(IDocument document) throws IOException {
		// Add the name of the file to the index
		output.addDocument(document);
		// Create a new Parser
		SourceIndexerRequestor requestor = new SourceIndexerRequestor(this, resourceFile, timeOut);
		
		IndexManager manager = FortranCorePlugin.getDefault().getCoreModel().getIndexManager();
		int problems = manager.indexProblemsEnabled( resourceFile.getProject() );
		requestor.setProblemMarkersEnabled( problems );
		requestor.requestRemoveMarkers( resourceFile, null );
		
		//Get the scanner info
		IProject currentProject = resourceFile.getProject();
		IScannerInfo scanInfo = new ScannerInfo();
		IScannerInfoProvider provider = FortranCorePlugin.getDefault().getScannerInfoProvider(currentProject);
		if (provider != null){
		  IScannerInfo buildScanInfo = provider.getScannerInformation(resourceFile);
		  if (buildScanInfo != null){
			scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
		  }
		}
		
		//C or CPP?
		ParserLanguage language = ParserLanguage.C;
		
		IParser parser = null;

		InputStream contents = null;
		try {
			contents = resourceFile.getContents();
			CodeReader reader = new CodeReader(resourceFile.getLocation().toOSString(), resourceFile.getCharset(), contents);
			parser = ParserFactory.createParser( 
							ParserFactory.createScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE, language, requestor, ParserUtil.getScannerLogService(), null ), 
							requestor, ParserMode.COMPLETE_PARSE, language, ParserUtil.getParserLogService() );
			requestor.setParser(parser);
		} catch( ParserFactoryError pfe ){
		} catch (CoreException e) {
		} finally {
			if (contents != null) {
				contents.close();
			}
		}
		
		try{

			// start timer
			String timeOut = FortranCorePlugin.getDefault().getPluginPreferences().getString(FDT_INDEXER_TIMEOUT);
			Integer timeOutValue = new Integer(timeOut);
			if (timeOutValue.intValue() > 0) {
				requestor.setTimeout(timeOutValue.intValue());
				requestor.startTimer();
			}
			boolean retVal = parser.parse();
	
			if (AbstractIndexer.VERBOSE){
				if (!retVal)
					AbstractIndexer.verbose("PARSE FAILED " + resourceFile.getName().toString()); //$NON-NLS-1$
				else
					AbstractIndexer.verbose("PARSE SUCCEEDED " + resourceFile.getName().toString());			 //$NON-NLS-1$
			}	
		}
		catch ( VirtualMachineError vmErr){
			if (vmErr instanceof OutOfMemoryError){
				org.eclipse.fdt.internal.core.model.Util.log(null, "Out Of Memory error: " + vmErr.getMessage() + " on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		catch (ParseError e){
			org.eclipse.fdt.internal.core.model.Util.log(null, "Parser Timeout on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch ( Exception ex ){
			if (ex instanceof IOException)
				throw (IOException) ex;
		}
		finally{
			requestor.stopTimer();
			//if the user disable problem reporting since we last checked, don't report the collected problems
			if( manager.indexProblemsEnabled( resourceFile.getProject() ) != 0 )
				requestor.reportProblems();
			
			//Report events
			ArrayList filesTrav = requestor.getFilesTraversed();
			IndexDelta indexDelta = new IndexDelta(resourceFile.getProject(),filesTrav, IIndexDelta.INDEX_FINISHED_DELTA);
			FortranCorePlugin.getDefault().getCoreModel().getIndexManager().notifyListeners(indexDelta);
			//Release all resources
			parser=null;
			currentProject = null;
			requestor = null;
			provider = null;
			scanInfo=null;
		}
	}
	/**
	 * Sets the document types the <code>IIndexer</code> handles.
	 */
	
	public void setFileTypes(String[] fileTypes){}
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.search.indexing.AbstractIndexer#getResourceFile()
	 */
	public IFile getResourceFile() {
		return resourceFile;
	}
	
}
