/*******************************************************************************
 * Copyright (c) 2010 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 *                    a placeholder implementation which uses a
 *                    static plugin XML resource to create the definitions
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.templates;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.rm.pbs.core.Activator;
import org.eclipse.ptp.rm.pbs.core.attributes.PBSXMLJobAttributeData;
import org.osgi.framework.Bundle;

/**
 * One-off implementation of the converter which uses an XML resource file in
 * this plugin to initialize the base template.
 * 
 * @author arossi
 * @since 5.0
 * 
 */
public class PBSXMLAttributeToTemplateConverter extends PBSBaseAttributeToTemplateConverter {
	protected String resourcePath;

	public PBSXMLAttributeToTemplateConverter() {
		data = new PBSXMLJobAttributeData();
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	@Override
	protected void initializeInternal() throws Throwable {
		if (defs == null)
			return;

		URL url = null;
		if (Activator.getDefault() != null) {
			Bundle bundle = Activator.getDefault().getBundle();
			url = FileLocator.find(bundle, new Path(DATA).append(resourcePath), null);
		} else
			url = new File(resourcePath).toURL();
		if (url == null)
			return;
		InputStream s = null;
		try {
			s = url.openStream();
			data.deserialize(s);
		} finally {
			try {
				if (s != null)
					s.close();
			} catch (IOException e) {
			}
		}
	}
}