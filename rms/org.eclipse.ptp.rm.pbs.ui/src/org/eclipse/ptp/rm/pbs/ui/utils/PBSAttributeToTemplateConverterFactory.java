/*******************************************************************************
 * Copyright (c) 2010 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - original API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.utils;

import org.eclipse.ptp.rm.pbs.ui.IPBSAttributeToTemplateConverter;
import org.eclipse.ptp.rm.pbs.ui.IPBSNonNLSConstants;

public class PBSAttributeToTemplateConverterFactory implements IPBSNonNLSConstants {

	public static IPBSAttributeToTemplateConverter getConverter() {
		PBSXMLAttributeToTemplateConverter pbsConverter = new PBSXMLAttributeToTemplateConverter();
		pbsConverter.setResourcePath(SRC + PATH_SEP + TMP_ATTR_XML);
		return pbsConverter;
	}
}
