/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.runnable.command;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.core.data.Read;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ReadImpl;

public class ConfigurableRegexTokenizer implements IStreamParserTokenizer, IJAXBNonNLSConstants, Runnable {

	public static final String EXT_ID = "org.eclipse.ptp.rm.jaxb.configurableRegexTokenizer"; //$NON-NLS-1$

	private Throwable error;
	private InputStream in;
	private List<ReadImpl> read;
	private String uuid;

	public ConfigurableRegexTokenizer() {
	}

	public ConfigurableRegexTokenizer(String uuid, List<Read> read) {
		this.uuid = uuid;
		setRead(read);
	}

	public Throwable getInternalError() {
		return error;
	}

	public void run() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			boolean endOfStream = false;
			while (!endOfStream) {
				for (ReadImpl r : read) {
					endOfStream = r.read(br);
					if (endOfStream) {
						break;
					}
				}
			}
		} catch (Throwable t) {
			error = t;
			// we do not close the out here because it probably is stdout
		}
	}

	public void setInputStream(InputStream stream) {
		in = stream;
	}

	public void setRead(List<Read> read) {
		this.read = new ArrayList<ReadImpl>();
		for (Read r : read) {
			this.read.add(new ReadImpl(uuid, r));
		}
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
