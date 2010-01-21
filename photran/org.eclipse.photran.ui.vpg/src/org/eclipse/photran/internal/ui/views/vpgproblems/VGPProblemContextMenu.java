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

/*
 * This class sets up the popup menu for our VGP Problems view
 * @author Timofey Yuvashev
 */

import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;

public class VGPProblemContextMenu extends MenuManager
{
    public VGPProblemContextMenu(IViewSite site, String title)
    {
        super(title);
        
        OpenMarkedFileAction openAct = new OpenMarkedFileAction(site);
        add(openAct);
        
        CopyMarkedFileAction copyAct = new CopyMarkedFileAction((VGPProblemView)site.getPart(), "Copy");
        add(copyAct);
        
       /* RemoveMarkerAction remAct = new RemoveMarkerAction(site);
        add(remAct);*/
        
        add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        
        ShowFullMessageAction showAct = new ShowFullMessageAction(site);
        add(showAct);
        
        ISelectionProvider provider = site.getSelectionProvider();
        ISelection sel = provider.getSelection();
        
        registerAction(showAct, provider, sel);
        registerAction(openAct, provider, sel);
        //registerAction(remAct,  provider, sel);
        
        site.getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), copyAct);
        
    }
    
    private void registerAction(SelectionDispatchAction action, 
                                ISelectionProvider provider, 
                                ISelection sel)
    {
        action.update(sel);
        provider.addSelectionChangedListener(action);
    }
}
