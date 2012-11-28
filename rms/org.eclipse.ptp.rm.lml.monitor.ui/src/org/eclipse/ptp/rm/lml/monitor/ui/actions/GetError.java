/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.monitor.ui.actions;

/**
 * Display the remote error file contents in a console.
 * 
 * @author arossi
 * 
 */
public class GetError extends AbstractConsoleAction {

	public GetError() {
		error = true;
	}
}
