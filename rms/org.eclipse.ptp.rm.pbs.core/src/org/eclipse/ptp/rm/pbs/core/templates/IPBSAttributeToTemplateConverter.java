/*******************************************************************************
 * Copyright (c) 2010 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - original API
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.templates;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.rm.pbs.core.attributes.IPBSJobAttributeData;

/**
 * Layer between the valid attribute definition access/storage and the creation
 * of the batch script template used to build the UI widgets.
 * 
 * @since 5.0
 */
public interface IPBSAttributeToTemplateConverter {

	String generateFullBatchScriptTemplate() throws Throwable;

	String generateMinBatchScriptTemplate() throws Throwable;

	IPBSJobAttributeData getData();

	void initialize() throws Throwable;

	void setAttributeDefinitions(IAttributeDefinition<?, ?, ?>[] defs);
}
