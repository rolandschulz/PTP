/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.index.IndexBasedFileContentProvider;
import org.eclipse.cdt.internal.core.parser.scanner.AbstractCharArray;
import org.eclipse.cdt.internal.core.parser.scanner.FileCharArray;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.internal.rdt.core.RemoteIndexerInfoProvider;
import org.eclipse.ptp.internal.rdt.core.RemoteIndexerInputAdapter;

/**
 * Provides cached access to the ASTs of translation units for given files.
 * The cache is implemented via soft references.
 * 
 * @author crecoskie
 *
 */
public class ASTCache {

	private Map<String, SoftReference<IASTTranslationUnit>> cache = new TreeMap<String, SoftReference<IASTTranslationUnit>> ();
	private Map<String, Long> fPathToLastIndexWriteTimeMap = new TreeMap<String, Long>();
	private Map<String, Integer> fPathToScannerInfoHashCodeMap = new TreeMap<String, Integer>();
	
	private static ASTCache fInstance;
	
	private ASTCache() {
		
	}
	
	public static synchronized ASTCache getDefault() {
		if(fInstance == null) {
			fInstance = new ASTCache();
		}
		
		return fInstance;
	}
	
	
	/**
	 * Gets the AST for a given source file.  If there is not already an AST in the cache,
	 * one will be parsed from the file.
	 * 
	 * @param absolutePath
	 * @param dataStore 
	 * @param status 
	 * @return
	 */
	public synchronized IASTTranslationUnit getASTTranslationUnit(String absolutePath, RemoteIndexerInfoProvider infoProvider, DataStore dataStore, DataElement status) {
		SoftReference<IASTTranslationUnit> refTU = cache.get(absolutePath);
		
		IASTTranslationUnit tu = null;
		
		if(refTU != null) {
			tu = refTU.get();
		}
		
		String scope = ScopeManager.getInstance().getScopeForFile(absolutePath);
		IIndex index = RemoteIndexManager.getInstance().getIndexForScope(scope, dataStore);
		
		File file = new File(absolutePath);
		long fileLastModifiedTime = file.lastModified();
		
		int currentScannerInfoHashcode = infoProvider.getScannerInformation(absolutePath).hashCode();
		
		
		final Long cachedModifiedTime = fPathToLastIndexWriteTimeMap.get(absolutePath);
		final boolean invalidDueToTimestamp = (cachedModifiedTime) == null ? true : fileLastModifiedTime > cachedModifiedTime.longValue();
		final Integer cachedHashcode = fPathToScannerInfoHashCodeMap.get(absolutePath);
		final boolean invalidDueToScannerInfo = (cachedHashcode == null) ? true : cachedHashcode.intValue() != currentScannerInfoHashcode;
		
		if(tu == null || invalidDueToTimestamp || invalidDueToScannerInfo) {
			
			RemoteLanguageMapper languageMapper = new RemoteLanguageMapper(infoProvider, dataStore);
			ILanguage language = languageMapper.getLanguage(absolutePath);
			
			FileInputStream in;
			AbstractCharArray chars = null;
			try {
				in = new FileInputStream(file);
				String fileEncoding = infoProvider.getFileEncodingRegistry().getFileEncoding(absolutePath);
				chars= FileCharArray.create(absolutePath, fileEncoding, in);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			long fileReadTime = System.currentTimeMillis();
			FileContent fileContent =  new InternalFileContent(absolutePath, chars, file.lastModified(), file.length(), fileReadTime);
			
			try {
				index.acquireReadLock();
				
				IncludeFileContentProvider fileCreator = getIncludeFileContentProvider(
						ITranslationUnit.AST_SKIP_INDEXED_HEADERS, index, language.getLinkageID());

				int options = ILanguage.OPTION_IS_SOURCE_UNIT;

				IParserLogService log = new DefaultLogService();
				try {
					tu = language.getASTTranslationUnit(fileContent, infoProvider.getScannerInformation(absolutePath),
							fileCreator, index, options, log);
					cache.put(absolutePath, new SoftReference<IASTTranslationUnit>(tu));
					fPathToLastIndexWriteTimeMap.put(absolutePath, fileLastModifiedTime);
					fPathToScannerInfoHashCodeMap.put(absolutePath, new Integer(currentScannerInfoHashcode));
				} catch (CoreException e) {

					// TODO: handle this properly
					return null;

				}

			} catch (InterruptedException e) {
				return null;
			}

			finally {
				index.releaseReadLock();
			}
		}
		
		return tu;
		
	}
		
	private IncludeFileContentProvider getIncludeFileContentProvider(int style, IIndex index, int linkageID) {
		final ASTFilePathResolver pathResolver = new RemoteIndexerInputAdapter();

		IncludeFileContentProvider fileContentsProvider;
		if ((style & ITranslationUnit.AST_SKIP_NONINDEXED_HEADERS) != 0) {
			fileContentsProvider= IncludeFileContentProvider.getEmptyFilesProvider();
		} else {
			fileContentsProvider= IncludeFileContentProvider.getSavedFilesProvider();
		}
		
		if (index != null && (style &  ITranslationUnit.AST_SKIP_INDEXED_HEADERS) != 0) {
			IndexBasedFileContentProvider ibcf= new IndexBasedFileContentProvider(index, pathResolver, linkageID, fileContentsProvider);
			if ((style &  ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT) != 0) {
				//TODO - the context to header gap shouldn't be null
				ibcf.setContextToHeaderGap(null);
//				ibcf.setSupportFillGapFromContextToHeader(true);
			}
			fileContentsProvider= ibcf;
		}
		
		return fileContentsProvider;
	}
}
