/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.rdt.core.tests.miner;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.indexer.ILanguageMapper;
import org.eclipse.cdt.internal.core.indexer.IStandaloneScannerInfoProvider;
import org.eclipse.cdt.internal.core.indexer.StandaloneFastIndexer;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPLinkageFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.miners.RemoteLocationConverter;

@SuppressWarnings({ "restriction", "serial" })
public class StandaloneIndexerTest extends TestCase {

	public static final String PDOM_FILE_NAME = "test.pdom"; //$NON-NLS-1$
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public static void main(String[] args) {
		
	}

	private static final Map<String,IPDOMLinkageFactory> linkageFactoryMap = new HashMap<String,IPDOMLinkageFactory>() {{
		put(ILinkage.C_LINKAGE_NAME, new PDOMCLinkageFactory());
		put(ILinkage.CPP_LINKAGE_NAME, new PDOMCPPLinkageFactory());
	}};
	
	private static final IParserLogService LOG = new DefaultLogService();
	
	
	private static final ILanguageMapper mapper = new ILanguageMapper(){
		public ILanguage getLanguage(String file) {
			if(file.endsWith(".cpp"))
				return GPPLanguage.getDefault();
			else if(file.endsWith(".c"))
				return GCCLanguage.getDefault();
			return null;
		}
	};
	
	
	private static class DirtSimpleScannerInfoProvider implements IStandaloneScannerInfoProvider {
		private IScannerInfo scannerInfo;
		
		public DirtSimpleScannerInfoProvider(IScannerInfo scannerInfo) {
			this.scannerInfo = scannerInfo;
		}
		public IScannerInfo getScannerInformation(String path) {
			return scannerInfo;
		}
		public IScannerInfo getDefaultScannerInformation(int linkageId) {
			return scannerInfo;
		}
	};

	
	
	
	/**
	 * Create a project, but since we don't have projects we will create
	 * a 'scope' instead.
	 * @throws Exception 
	 */
	private static StandaloneFastIndexer createIndexer(File userDir) throws Exception {
		File indexFile = new File(userDir + File.separator + PDOM_FILE_NAME);
		//IIndexLocationConverter locationConverter = new URIRelativeLocationConverter(userDir.toURI());
		IIndexLocationConverter locationConverter = new RemoteLocationConverter();
		
		String[] includePaths = FileManager.getIncludePaths();
		Map<String,String> macroDefinitions = Collections.emptyMap();
		IScannerInfo scannerInfo = new ScannerInfo(macroDefinitions, includePaths);
		
		StandaloneFastIndexer indexer = new StandaloneFastIndexer(indexFile, locationConverter, linkageFactoryMap, null, LOG);
		indexer.setScannerInfoProvider(new DirtSimpleScannerInfoProvider(scannerInfo));
		indexer.setLanguageMapper(mapper);
		indexer.setTraceStatistics(true);
		indexer.setShowProblems(true);
		indexer.setShowActivity(true);
		return indexer;
	}
	
	
	public void testIndexer() throws Exception {
		System.out.println("Running Standalone Indexer Test");
		List<String> tus = FileManager.copyProjectContent();

		File userDir = FileManager.getTestDirectory();
		System.out.println("Starting indexer");
		StandaloneFastIndexer indexer = createIndexer(userDir);
		System.out.println("PDOM file at: " + (userDir + File.separator + PDOM_FILE_NAME));
		
		//List<String> empty = Collections.emptyList();
		indexer.rebuild(tus, new NullProgressMonitor());
		//indexer.handleDelta(tus, empty, empty, new NullProgressMonitor());
	}
	
	
}
