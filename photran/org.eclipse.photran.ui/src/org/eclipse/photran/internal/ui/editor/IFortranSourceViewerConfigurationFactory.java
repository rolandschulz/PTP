/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * Interface implemented by a contribution to the
 * {@value org.eclipse.photran.internal.ui.editor.FortranEditor#SOURCE_VIEWER_CONFIG_EXTENSION_POINT_ID}
 * extension point.
 * 
 * @author Jeff Overbey
 */
public interface IFortranSourceViewerConfigurationFactory
{
    SourceViewerConfiguration create(FortranEditor editor);
}
