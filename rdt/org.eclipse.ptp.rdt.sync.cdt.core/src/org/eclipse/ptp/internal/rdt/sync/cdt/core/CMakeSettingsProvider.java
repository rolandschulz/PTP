/*******************************************************************************
 * Copyright (c) 2013 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.core;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.messages.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Language settings provider for projects that build with CMake
 */
public class CMakeSettingsProvider extends AbstractXMLSettingsProvider implements ICBuildOutputParser, ILanguageSettingsEditableProvider {
	private IConfiguration config = null;
	private IPath buildDir = null;
	private String languageId = null;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider#clone()
	 */
	@Override
	public CMakeSettingsProvider clone() throws CloneNotSupportedException {
		return (CMakeSettingsProvider) super.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider#cloneShallow()
	 */
	@Override
	public CMakeSettingsProvider cloneShallow() throws CloneNotSupportedException {
		return (CMakeSettingsProvider) super.cloneShallow();
	}

	@Override
	public Document getXML() throws IOException, SAXException {
		IPath CProjectFile = config.getBuilder().getBuildLocation().append(".cproject"); //$NON-NLS-1$
		Document XMLDoc = XMLConversionUtil.XMLFileToDOM(CProjectFile);
		DocumentBuilderFactory builderFactory =
		        DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
		    builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// Should never happen since no configuring is done
			throw new RuntimeException(e);
		}

		// Create new document with basic XML tags
		Document newDoc = builder.newDocument();
		Element providerNode = newDoc.createElement("provider"); //$NON-NLS-1$
		newDoc.appendChild(providerNode);
		Element languageNode = newDoc.createElement("language"); //$NON-NLS-1$
		languageNode.setAttribute("id", languageId); //$NON-NLS-1$
		providerNode.appendChild(languageNode);

		// Main loop - create entries in new format from old path entries
		NodeList oldEntries = XMLDoc.getElementsByTagName("pathentry"); //$NON-NLS-1$
		for (int i=0; i<oldEntries.getLength(); i++) {

			// Extract and error-check entry values
			Node kindNode = oldEntries.item(i).getAttributes().getNamedItem("kind"); //$NON-NLS-1$
			Node nameNode = oldEntries.item(i).getAttributes().getNamedItem("name"); //$NON-NLS-1$
			Node valueNode = oldEntries.item(i).getAttributes().getNamedItem("value"); //$NON-NLS-1$
			if (kindNode == null || nameNode == null) {
				continue;
			}
			String oldEntryKind = kindNode.getNodeValue();
			if (oldEntryKind.equals("mac") && valueNode == null) { //$NON-NLS-1$
				continue;
			}

			// Create new entry node from old entry values
			Element newEntryNode = newDoc.createElement("entry"); //$NON-NLS-1$
			if (oldEntryKind.equals("include")) { //$NON-NLS-1$
				newEntryNode.setAttribute("kind", "includePath"); //$NON-NLS-1$ //$NON-NLS-2$
				newEntryNode.setAttribute("name", nameNode.getNodeValue()); //$NON-NLS-1$
			} else if (oldEntryKind.equals("mac")) { //$NON-NLS-1$
				newEntryNode.setAttribute("kind", "macro"); //$NON-NLS-1$ //$NON-NLS-2$
				newEntryNode.setAttribute("name", nameNode.getNodeValue()); //$NON-NLS-1$
				newEntryNode.setAttribute("value", valueNode.getNodeValue()); //$NON-NLS-1$
			}

			// Flags are the same for all entries in this case
			Element flagNode = newDoc.createElement("flag"); //$NON-NLS-1$
			flagNode.setAttribute("value", "BUILTIN|READONLY"); //$NON-NLS-1$ //$NON-NLS-2$
			newEntryNode.appendChild(flagNode);

			languageNode.appendChild(newEntryNode);
		}

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DOMSource source = new DOMSource(newDoc);
		StreamResult result = new StreamResult(new File("/Users/john/xmlstupidity")); //$NON-NLS-1$
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return newDoc;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.sync.cdt.core.AbstractXMLSettingsProvider#getXSLT()
	 */
	@Override
	public Source getXSLT() {
		// No XSLT, because transformations are done internally
		return null;
	}

	/**
	 * Get the build directory
	 * @return build directory
	 */
	public IPath getBuildDir() {
		return buildDir;
	}

	/**
	 * Set the build directory
	 * @param path to build directory or null for no build directory
	 */
	public void setBuildDir(IPath dir) {
		buildDir = dir;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser#startup(org.eclipse.cdt.core.settings.model.ICConfigurationDescription, org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker)
	 */
	@Override
	public void startup(ICConfigurationDescription cfgDescription,
			IWorkingDirectoryTracker cwdTracker) throws CoreException {
		languageId = this.getLanguageId(cfgDescription);
		config = ManagedBuildManager.getConfigurationForDescription(cfgDescription);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser#processLine(java.lang.String)
	 */
	@Override
	public boolean processLine(String line) {
		// Do nothing with individual build output lines
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser#shutdown()
	 */
	@Override
	public void shutdown() {
		try {
			reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Encapsulate strategy for finding the language id for which settings will apply.
	// This is somewhat tricky and thus subject to change.
	private String getLanguageId(ICConfigurationDescription cfgDescription) {
		ICLanguageSetting[] settings = cfgDescription.getRootFolderDescription().getLanguageSettings();
		// Current strategy is just to return the last entry that has a valid language id.
		String languageId = null;
		for (ICLanguageSetting s : settings) {
			if (s.getLanguageId() != null) {
				languageId = s.getLanguageId();
			}
		}
		assert languageId != null : Messages.CMakeSettingsProvider_0 + cfgDescription.getName();
		return languageId;
	}
}
