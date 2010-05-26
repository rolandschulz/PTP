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

import java.util.Iterator;

import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
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
 * 
 * @author Esfar Huq
 * @author Rui Wang
 */
/* Esfar/Rui: 
 * Modified the method run()
 *  1) Added title bar to "Details" dialog 
 *  2) "Details" dialog wraps
 *  3) Focus is shifted to "Close" key, so that dialog can be exited by hitting enter key
 */
public class ShowFullMessageAction extends SelectionDispatchAction
{
    public ShowFullMessageAction(IWorkbenchSite site)
    {
        super(site);
        setText("Event Details");
        setToolTipText("Show the entire message for selected event(s)");
    }

    public ShowFullMessageAction(IWorkbenchSite site, String text, String toolTipText)
    {
        super(site);
        setText(text);
        setToolTipText(toolTipText);
    }

    public ImageDescriptor getImageDescriptor()
    {
        Image img = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        return ImageDescriptor.createFromImage(img);
    }

    protected void run(final IMarker marker)
    {
        Dialog dialog = new Dialog(getSite().getShell())
        {
            @Override
            protected void configureShell(Shell shell)
            {
                super.configureShell(shell);
                shell.setText("Event Details");
                shell.setSize(500, 300);
            }

            @Override
            protected Control createDialogArea(Composite parent)
            {
                Composite dialogArea = (Composite)super.createDialogArea(parent);

                Text message = new Text(dialogArea, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
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
        };
        
        dialog.open();
    }

    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    @Override
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(checkEnabled(selection));
    }

    //Only enables if the selected type is an IMarker
    private boolean checkEnabled(IStructuredSelection selection) {
        if (selection.isEmpty())
            return false;
        for (Iterator<?> iter= selection.iterator(); iter.hasNext();) {
            Object element= iter.next();
            if (element instanceof IMarker)
                continue;
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    @Override
    public void run(IStructuredSelection selection)
    {
        if (!checkEnabled(selection))
            return;

        for (Iterator<?> iter= selection.iterator(); iter.hasNext();)
        {
            Object element= iter.next();
            if (element instanceof IMarker)
            {
                IMarker marker = (IMarker)element;
                run(marker);
            }
        }
    }

}
