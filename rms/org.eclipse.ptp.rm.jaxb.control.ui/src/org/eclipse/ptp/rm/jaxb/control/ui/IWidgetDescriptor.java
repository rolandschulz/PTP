/**********************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rm.jaxb.control.ui;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Basic API for widget objects associated with SWT controls.
 * 
 * @author bwatt
 * @since 1.1
 * 
 */
public interface IWidgetDescriptor {

	/**
	 * Get the background color of the widget.
	 * 
	 * @return the background color
	 */
	public Color getBackground();

	/**
	 * Get the choice of the widget.
	 * 
	 * @return the choice
	 */
	public String getChoice();

	/**
	 * Get the fixed text of the widget.
	 * 
	 * @return the fixed text
	 */
	public String getFixedText();

	/**
	 * Get the font of the widget.
	 * 
	 * @return the font
	 */
	public Font getFont();

	/**
	 * Get the foreground color of the widget.
	 * 
	 * @return the foreground color
	 */
	public Color getForeground();

	/**
	 * Get the items from of the widget.
	 * 
	 * @return the items from
	 */
	public String getItemsFrom();

	/**
	 * Get the layout data of the widget.
	 * 
	 * @return the layout data
	 */
	public Object getLayoutData();

	/**
	 * Get the maximum of the widget.
	 * 
	 * @return the maximum
	 */
	public Integer getMax();

	/**
	 * Get the minimum of the widget.
	 * 
	 * @return the minimum
	 */
	public Integer getMin();

	/**
	 * Get the read-only flag of the widget.
	 * 
	 * @return the read-only flag
	 */
	public boolean getReadOnly();

	/**
	 * Get the remote connection of the widget.
	 * 
	 * @return the remote connection
	 */
	public IRemoteConnection getRemoteConnection();

	/**
	 * Get the style of the widget.
	 * 
	 * @return the style
	 */
	public int getStyle();

	/**
	 * Get the title of the widget.
	 * 
	 * @return the title
	 */
	public String getTitle();

	/**
	 * Get the tool tip text of the widget.
	 * 
	 * @return the tool tip text
	 */
	public String getToolTipText();

	/**
	 * Get the translate-boolean-as of the widget.
	 * 
	 * @return the translate-boolean-as
	 */
	public String getTranslateBooleanAs();

	/**
	 * Get the type of the widget.
	 * 
	 * @return the type
	 */
	public String getType();

	/**
	 * Get the type ID of the widget.
	 * 
	 * @return the type ID
	 */
	public String getTypeId();

	/**
	 * Query if the widget is read-only.
	 * 
	 * @return <code>true</code> if widget is read-only
	 */
	public boolean isReadOnly();

}
