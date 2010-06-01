/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.views.vpgproblems;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Action to display the entire message for a marker in a separate dialog.
 *
 * @author Timofey Yuvashev
 * @author Esfar Huq
 * @author Rui Wang
 * @author Jeff Overbey
 */
/* Esfar/Rui:
 *   Modified the method run()
 *       1) Added title bar to "Details" dialog 
 *       2) "Details" dialog wraps
 *       3) Focus is shifted to "Close" key, so that dialog can be exited by hitting enter key
 * Jeff - Converted to use Dialog instead of Shell
 */
public class ShowFullMessageAction extends MarkerDispatchAction
{
    public ShowFullMessageAction(IWorkbenchSite site)
    {
        super(site);
        setText(Messages.ShowFullMessageAction_EventDetails);
        setToolTipText(Messages.ShowFullMessageAction_ShowDetailsTooltip);
    }

    public ShowFullMessageAction(IWorkbenchSite site, String text, String toolTipText)
    {
        super(site);
        setText(text);
        setToolTipText(toolTipText);
    }

    @Override public ImageDescriptor getImageDescriptor()
    {
        Image img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        return ImageDescriptor.createFromImage(img);
    }

    @Override protected void run(final IMarker marker)
    {
        new DetailsDialog(getSite().getShell(), marker).open();
    }

    private static final class DetailsDialog extends Dialog
    {
        private final IMarker marker;

        private DetailsDialog(Shell parentShell, IMarker marker)
        {
            super(parentShell);
            this.marker = marker;
        }

        @Override
        protected void configureShell(Shell shell)
        {
            super.configureShell(shell);
            shell.setText(Messages.ShowFullMessageAction_EventDetails);
            shell.setSize(500, 300);
        }

        @Override
        protected Control createDialogArea(Composite parent)
        {
            Composite dialogArea = (Composite)super.createDialogArea(parent);

            Text message = new Text(
                dialogArea,
                SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
            message.setText(MarkerUtilities.getMessage(marker));
            message.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            dialogArea.layout();
            return dialogArea;
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent)
        {
            Button close = createButton(parent,
                                        IDialogConstants.CLOSE_ID,
                                        IDialogConstants.CLOSE_LABEL,
                                        true);
            close.addSelectionListener(new SelectionListener()
            {
                public void widgetSelected(SelectionEvent e)        { close(); }
                public void widgetDefaultSelected(SelectionEvent e) { close(); }
            });
            close.setFocus();
        }
    }
}
