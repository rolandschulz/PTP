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

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.ISettingsProcessor;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.IncludePathsSettingsProcessor;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.MacroSettingsProcessor;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.ProjectSettingsExportStrategy;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.SettingsImportExportException;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.XMLUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * @since 4.0
 */
public class ImportRemotePathSymbolsHandler {
	
	public static final List<ISettingsProcessor> processors = Arrays.<ISettingsProcessor>asList(
			new IncludePathsSettingsProcessor(),
			new MacroSettingsProcessor()
	);
	
	
	
	public static void importProjectSettings(String projectSetting, IProject project) {
				
			if(projectSetting!=null&&projectSetting.length()>0){
				
				try {
					
					ICConfigurationDescription config = getActiveProjectConfig(project);
					ICProjectDescription writableDescription = CoreModel.getDefault().getProjectDescription(project, true);
					ICConfigurationDescription writableConfig = writableDescription.getConfigurationById(config.getId());
					final ICFolderDescription writableProjectRoot = writableConfig.getRootFolderDescription();
					
					Map<String,ISettingsProcessor> importers = new HashMap<String,ISettingsProcessor>();
					for(ISettingsProcessor processor : processors) {
						importers.put(processor.getSectionName(), processor);
					}
					Document document = parse(projectSetting);
							
					Element root = document.getDocumentElement();
					List<Element> sections = XMLUtils.extractChildElements(root, ProjectSettingsExportStrategy.SECTION_ELEMENT);
								
					for(Element section : sections) {
						String sectionName = section.getAttribute(ProjectSettingsExportStrategy.SECTION_NAME_ATTRIBUTE);
						if(sectionName != null) {
							ISettingsProcessor importer = importers.get(sectionName);
														
							if(importer != null)
								importer.readSectionXML(writableProjectRoot, section);
								
						}
					}
					
					CoreModel.getDefault().setProjectDescription(project, writableDescription);
					
				} catch (SettingsImportExportException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
	}
	
	private static ICConfigurationDescription getActiveProjectConfig(IProject prj) {
		
		ICConfigurationDescription[] cfgDescs = null;
	
		if (prj == null)
			return null;

		
		if (cfgDescs == null) {
			ICProjectDescription pDesc =  CoreModel.getDefault().getProjectDescription(prj);
			cfgDescs = (pDesc == null)? null : pDesc.getConfigurations();
			if (cfgDescs == null || cfgDescs.length == 0) return null;
			
		} 
		
		for (int i = 0; i < cfgDescs.length; ++i) {
			if (cfgDescs[i].isActive()) {
				return cfgDescs[i];
			}
		}
		
		return null;
	}

	private static ErrorHandler ABORTING_ERROR_HANDER = new ErrorHandler() {
		public void error(SAXParseException e) throws SAXException {
			throw e;
		}
		public void fatalError(SAXParseException e) throws SAXException {
			throw e;
		}
		public void warning(SAXParseException e) throws SAXException {
			throw e;
		}
	};

	private static Document parse(String inputXMLString) throws SettingsImportExportException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		factory.setIgnoringComments(true);
		
		try {
			DocumentBuilder parser = factory.newDocumentBuilder();
			parser.setErrorHandler(ABORTING_ERROR_HANDER); 
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(inputXMLString));
			Document doc = parser.parse(is);
			return doc;
			
		} catch (Exception e) {
			throw new SettingsImportExportException(e);
		}
	}


}
