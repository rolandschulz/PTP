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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 *
 * @author tyuvash2
 */
public class ShowFullMessageAction extends SelectionDispatchAction
{

    /**
     * @param site
     */
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

    //TODO: Make shell content automatically resize when the shell is re-sized
    protected void run(IMarker marker)
    {
        Display disp = getSite().getShell().getDisplay();
        final Shell shell = new Shell(disp, SWT.RESIZE);
        shell.setText("Event Details");
        shell.setSize(800,600);

        GridLayout gridLayout = new GridLayout(1, false);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;

        shell.setLayout(gridLayout);
        shell.setLayoutData(gridData);

        Text message = new Text(shell, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        message.setSize(700, 500);
        message.setLayoutData(gridData);
        message.setText(MarkerUtilities.getMessage(marker));

        Button close = new Button(shell, SWT.PUSH);
        close.setText("Close");
        close.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    shell.close();
                }
            });

        message.pack();
        shell.pack();
        shell.open();

        while(!shell.isDisposed())
        {
            if(!disp.readAndDispatch())
                disp.sleep();
        }
        shell.dispose();
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
