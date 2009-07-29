/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.io.Serializable;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITemplate;

public class Template implements ITemplate, Serializable {
	private static final long serialVersionUID = 1L;

	private String[] fParameterTypes;
	private String fSignature;

	public int getNumberOfTemplateParameters() {
		return fParameterTypes == null ? 0 : fParameterTypes.length;
	}

	public String[] getTemplateParameterTypes() {
		return fParameterTypes;
	}

	public String getTemplateSignature() throws CModelException {
		return fSignature;
	}

	public void setTemplateParameterTypes(String[] parameterTypes) {
		fParameterTypes = parameterTypes;
	}
}
