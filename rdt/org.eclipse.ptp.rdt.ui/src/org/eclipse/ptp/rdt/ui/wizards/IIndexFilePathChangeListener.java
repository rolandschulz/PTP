/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;


/**
 * Listener for changes to the path in the IndexFileLocationWidget.
 *
 * @see IndexFileLocationWidget
 */
public interface IIndexFilePathChangeListener {

	public void pathChanged(String newPath);
}
