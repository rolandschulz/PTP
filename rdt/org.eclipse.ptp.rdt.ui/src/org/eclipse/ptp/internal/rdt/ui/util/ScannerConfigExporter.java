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
package org.eclipse.ptp.internal.rdt.ui.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.IProjectSettingsWizardPage;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.IncludePathsSettingsProcessor;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.MacroSettingsProcessor;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.ProjectSettingsExportStrategy;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * @since 4.0
 */
public class ScannerConfigExporter {
	
	protected static final String NONE = ""; //$NON-NLS-1$
	protected static final String CDATA = "CDATA"; //$NON-NLS-1$
	
	//include path section constants
	private static IncludePathsSettingsProcessor includePathsSettingsProcessor = new IncludePathsSettingsProcessor();
	protected static final String INCLUDE_PATH_SECTION_NAME = includePathsSettingsProcessor.getSectionName();
	private static final String INCLUDE_PATH_ELEMENT = "includepath"; //$NON-NLS-1$
	
	//macro section constants
	private static MacroSettingsProcessor macroSettingsProcessor = new MacroSettingsProcessor();
	protected static final String MACRO_SECTION_NAME = macroSettingsProcessor.getSectionName();
	private static final String MACRO_ELEMENT = "macro"; //$NON-NLS-1$
	private static final String NAME_ELEMENT = "name";   //$NON-NLS-1$
	private static final String VALUE_ELEMENT = "value"; //$NON-NLS-1$
	
	//language names
	private static final String C_LINKAGE_ELEMENT_NAME="C Source File"; //$NON-NLS-1$
	private static final String CPP_LINKAGE_ELEMENT_NAME="C++ Source File"; //$NON-NLS-1$
	//private static final String[] CLIKE_LANGUAGE_NAMES=new String[]{"C Source File", "C++ Source File"};
	
	protected static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	protected static final String LANGUAGE_ELEMENT = "language"; //$NON-NLS-1$
	
	
	
	public static boolean exportScannerConfiguration(String exportFileName, Map<String, IScannerInfo> scannerInfoMapByLinkage){
		
		SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		TransformerHandler handler = null;
		Writer writer = null;
		try {
			handler = factory.newTransformerHandler();
		}catch (TransformerConfigurationException e) {
			//todo logging
			return false;
		}
		
		IPath path = new Path(exportFileName);
		if(!IProjectSettingsWizardPage.FILENAME_EXTENSION.equals(path.getFileExtension()))
			path.addFileExtension(IProjectSettingsWizardPage.FILENAME_EXTENSION);
		try {
			writer = new FileWriter(path.toFile());
		} catch (IOException e) {
			//todo logging
			return false;
		}
		
		handler.setResult(new StreamResult(writer));
		
		// write out the XML header
		Transformer transformer = handler.getTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		
		AttributesImpl attributes = new AttributesImpl();
		try {
			handler.startDocument();
			newline(handler);
			handler.startElement(NONE, NONE, ProjectSettingsExportStrategy.ROOT_ELEMENT, null);
			newline(handler);
			
			
			writeSectionXML(INCLUDE_PATH_SECTION_NAME, scannerInfoMapByLinkage, handler);
			writeSectionXML(MACRO_SECTION_NAME, scannerInfoMapByLinkage, handler);
			
			
			handler.endElement(NONE, NONE, ProjectSettingsExportStrategy.ROOT_ELEMENT);
			newline(handler);
			handler.endDocument();
			newline(handler);
		} catch (SAXException e) {
			//todo logging
			return false;
		} 
		return true;
	}
	
	private static void writeSectionXML(String sectionName, Map<String, IScannerInfo> scannerInfoMapByLinkage, TransformerHandler handler) throws SAXException{
		AttributesImpl attributes = new AttributesImpl();
		attributes.clear();
		attributes.addAttribute(NONE, NONE, ProjectSettingsExportStrategy.SECTION_NAME_ATTRIBUTE, CDATA, sectionName);
		handler.startElement(NONE, NONE, ProjectSettingsExportStrategy.SECTION_ELEMENT, attributes);
		newline(handler);
		Set<String> linkages = scannerInfoMapByLinkage.keySet();
		
		for(String linkageidStr : linkages) {
			int linkageid = Integer.parseInt(linkageidStr);
			String languageElementName=null;;
			if(linkageid==ILinkage.C_LINKAGE_ID){
				languageElementName = C_LINKAGE_ELEMENT_NAME;
			}else if(linkageid==ILinkage.CPP_LINKAGE_ID){
				languageElementName = CPP_LINKAGE_ELEMENT_NAME;
			}
			if(languageElementName!=null){
				attributes.clear();
				attributes.addAttribute(NONE, NONE, NAME_ATTRIBUTE, CDATA, languageElementName);
				handler.startElement(NONE, NONE, LANGUAGE_ELEMENT, attributes);
				newline(handler);
				//IScannerInfo projScannerInfo = scannerInfoProvider.getScannerInfoCaughtByBuildScan(linkageid);
				IScannerInfo projScannerInfo = scannerInfoMapByLinkage.get(linkageidStr);
				if(sectionName.equals(INCLUDE_PATH_SECTION_NAME)){
					writeIncludeSettings(projScannerInfo.getIncludePaths(), handler);
				}else if(sectionName.equals(MACRO_SECTION_NAME)){
					writeMacroSettings(projScannerInfo.getDefinedSymbols(), handler);
				}
				newline(handler);
				handler.endElement(NONE, NONE, LANGUAGE_ELEMENT);
				newline(handler);
			}
		}
			
		handler.endElement(NONE, NONE, ProjectSettingsExportStrategy.SECTION_ELEMENT);
		newline(handler);
		
		
	}
	
	private static void writeIncludeSettings(String[] includePaths, TransformerHandler handler) throws SAXException{
		for(String includePath : includePaths){
			if(includePath!=null){
				
				handler.startElement(NONE, NONE, INCLUDE_PATH_ELEMENT, null);
				handler.characters(includePath.toCharArray(), 0, includePath.length());
				handler.endElement(NONE, NONE, INCLUDE_PATH_ELEMENT);
				newline(handler);
				
				
			}
		}
	}
	
	private static void writeMacroSettings(Map<String, String> macros, TransformerHandler handler) throws SAXException{
		for(String macroKey: macros.keySet()){
			if(macroKey!=null){
				String macroValue = macros.get(macroKey);
				if(macroValue!=null){
					handler.startElement(NONE, NONE, MACRO_ELEMENT, null);
					newline(handler);
					
					handler.startElement(NONE, NONE, NAME_ELEMENT, null);
					handler.characters(macroKey.toCharArray(), 0, macroKey.length());
					handler.endElement(NONE, NONE, NAME_ELEMENT);
					
					handler.startElement(NONE, NONE, VALUE_ELEMENT, null);
					handler.characters(macroValue.toCharArray(), 0, macroValue.length());
					handler.endElement(NONE, NONE, VALUE_ELEMENT);
					newline(handler);
					
					handler.endElement(NONE, NONE, MACRO_ELEMENT);
					newline(handler);
				}
			}
		}
	}
	

	/**
	 * Outputs a newline (\n) to the given ContentHandler.
	 */
	private static void newline(ContentHandler handler) throws SAXException {
		handler.ignorableWhitespace("\n".toCharArray(), 0, 1); //$NON-NLS-1$
	}
		
		

}
