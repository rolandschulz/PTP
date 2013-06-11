/**********************************************************************
 * Copyright (c) 2005,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.editorHelp;

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpResource;
import org.eclipse.ptp.pldt.common.CommonPlugin;

/**
 * Implementation of ICHelpResourceDescriptor, help information e.g. for an API  
 * @author beth tibbitts
 *
 */
public class CHelpResourceDescriptorImpl implements ICHelpResourceDescriptor {
	ICHelpBook book;
	String name;
	String label;
	String href;
	IHelpResource[] resources;
	static boolean traceOn = false;

	/**
	 * This is where the location of the html help file (to be displayed for an
	 * API) is located
	 * 
	 * @param helpBook
	 * @param functionSummary
	 * @param pluginId
	 */
	public CHelpResourceDescriptorImpl(ICHelpBook helpBook, IFunctionSummary functionSummary, String pluginId) {
		book = helpBook;
		name = functionSummary.getName();
		// href = "/"+pluginId + "/html/" + name + ".html";
		StringBuffer buf = new StringBuffer();

		// Find where the html dir is located - could vary e.g. if a fragment
		// provides "extra" info
		String htmlLocn = findHTMLdir(pluginId);

		buf.append("/").append(pluginId).append("/").append(htmlLocn).append("/").append(name).append(".html"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		href = buf.toString();
		// System.out.println("looking for help file: "+href);
		IFunctionPrototypeSummary proto = functionSummary.getPrototype();
		if (proto != null) {
			label = functionSummary.getPrototype().getPrototypeString(false);
		} else {
			label = functionSummary.getName();
		}
		resources = new IHelpResource[1];
		resources[0] = new IHelpResource() {
			public String getHref() {
				return href;
			}

			public String getLabel() {
				if (label == null) {
					return "NO SUCH LABEL BETH";
				}
				return label;
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpResourceDescriptor#getCHelpBook()
	 */
	public ICHelpBook getCHelpBook() {
		return book;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpResourceDescriptor#getHelpResources()
	 */
	public IHelpResource[] getHelpResources() {
		return resources;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + " -> " + href; //$NON-NLS-1$
	}

	/**
	 * Find the HTML directory that holds the html files that dynamic help will
	 * use. Normally this is in the 'html' directory but if there is a fragment
	 * plug-in that e.g. adds "extra" information for the html files (via an
	 * Eclipse extension), then we want to use that one.
	 * 
	 * @param pluginId
	 *            the plugin/bundle that we want the HTML directory for
	 * @return the name of the directory to use for the HTML files
	 */
	private String findHTMLdir(String pluginId) {
		String result = "html";
		String pid = CommonPlugin.PLUGIN_ID;
		String extid = "althelp";
		if (traceOn) {
			System.out.println("Looking for HTML dir for " + pluginId);
		}
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(pid, extid).getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension extn = extensions[i];
			String extLabel = extn.getLabel();
			if (traceOn) {
				System.out.println("  Found extension for " + extLabel + "  id=" + extn.getUniqueIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			String uid = extn.getUniqueIdentifier();
			if (uid.startsWith(pluginId)) {
				IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					IConfigurationElement ice = configElements[j];
					result = ice.getAttribute("dirname"); //$NON-NLS-1$
					if (traceOn) {
						System.out.println("    dirname=" + result); //$NON-NLS-1$
					}
					// we can stop looking now
					return result;
				}
			}
		}
		// Didn't find anything better, so stick with what we had
		return result;
	}
}
