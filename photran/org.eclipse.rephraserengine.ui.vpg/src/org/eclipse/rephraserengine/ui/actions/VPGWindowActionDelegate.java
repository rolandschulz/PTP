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
package org.eclipse.rephraserengine.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.core.vpg.eclipse.VPGSchedulingRule;
import org.eclipse.rephraserengine.ui.IEclipseVPGFactory;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.progress.IProgressService;

/**
 * Abstract class for an {@link IWorkbenchWindowActionDelegate} that requires access to a VPG.
 * <p>
 * The user interface will allow this action to be run on any VPG contributed to the <i>vpg</i>
 * extension point.  If there is only one VPG, it will run on that; if multiple VPGs are available,
 * the user will be asked to select one.
 * <p>
 * This class schedules itself to run after it can successfully lock all of the resources in the
 * workspace; this guarantees that only one such action will be accessing the VPG at a time.
 *
 * @author Jeff Overbey
 * 
 * @since 1.0
 */
public abstract class VPGWindowActionDelegate
           implements IWorkbenchWindowActionDelegate,
                      IRunnableWithProgress
{
    private static final String VPG_EXTENSION_POINT_ID = Messages.VPGWindowActionDelegate_0;

    private EclipseVPG vpg = null;

    /** The active workbench window; may be <code>null</code> */
    protected IWorkbenchWindow activeWindow = null;

    /** The active shell; may be <code>null</code> */
    protected Shell activeShell = null;

    public final void init(IWorkbenchWindow window)
    {
        activeWindow = window;
        if (activeWindow != null)
            activeShell = activeWindow.getShell();
    }

    public void dispose() {;}
    public void selectionChanged(IAction action, ISelection selection) {;}

    public final void run(IAction action)
    {
        vpg = determineVPG();

        if (vpg == null)
        {
            MessageDialog.openError(
                    activeShell,
                    Messages.VPGWindowActionDelegate_ErrorTitle,
                    Messages.VPGWindowActionDelegate_NoVPGsAvailable);
        }
        else
        {
            scheduleThisUsingVPGSchedulingRule();
        }
    }

    private void scheduleThisUsingVPGSchedulingRule()
    {
        IProgressService context = PlatformUI.getWorkbench().getProgressService();

        ISchedulingRule lockEntireWorkspace = ResourcesPlugin.getWorkspace().getRoot();
        ISchedulingRule vpgSched = VPGSchedulingRule.getInstance();
        ISchedulingRule schedulingRule = MultiRule.combine(lockEntireWorkspace, vpgSched);

        try
        {
            context.runInUI(context, this, schedulingRule);
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
            MessageDialog.openError(
                    activeShell,
                    Messages.VPGWindowActionDelegate_UnhandledExceptionTitle,
                    e.getMessage());
        }
        catch (InterruptedException e)
        {
            // Do nothing
        }
    }

    private EclipseVPG determineVPG()
    {
        IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(VPG_EXTENSION_POINT_ID);
        if (configs.length == 0)
            return null;
        else
            return createVPG(configs, determineWhichVPGToUse(configs));
    }

    private EclipseVPG createVPG(IConfigurationElement[] configs, int index)
    {
        try
        {
            if (index < 0)
            {
                return null;
            }
            else
            {
//                MessageDialog.openInformation(
//                        activeShell,
//                        "FYI",
//                        "You chose " + configs[index].getAttribute("name"));

                IEclipseVPGFactory factory = (IEclipseVPGFactory)configs[index].createExecutableExtension("class"); //$NON-NLS-1$
                return factory.getVPG();
            }
        }
        catch (CoreException e)
        {
            return null;
        }
    }

    private int determineWhichVPGToUse(IConfigurationElement[] configs)
    {
        if (configs.length == 1)
            return 0;
        else
            return askUserWhichVPGToUse(configs);
    }

    @SuppressWarnings("unchecked")
    private int askUserWhichVPGToUse(IConfigurationElement[] configs)
    {
        Map<Integer, String> vpgs = new TreeMap<Integer, String>();
        for (int i = 0; i < configs.length; i++)
            vpgs.put(i, configs[i].getAttribute("name")); //$NON-NLS-1$

        ListDialog dlg = new ListDialog(activeShell);
        dlg.setInput(vpgs);
        dlg.setTitle(Messages.VPGWindowActionDelegate_SelectVPGTitle);
        dlg.setContentProvider(new ArrayPairContentProvider());
        dlg.setLabelProvider(new ArrayPairLabelProvider());
        dlg.setMessage(Messages.VPGWindowActionDelegate_SelectDatabaseToUse);
        dlg.setBlockOnOpen(true);
        if (dlg.open() == ListDialog.OK
                && dlg.getResult() != null
                && dlg.getResult().length > 0)
        {
            return ((Entry<Integer, String>)dlg.getResult()[0]).getKey();
        }
        else
        {
            return -1;
        }
    }

    private static final class ArrayPairContentProvider implements IStructuredContentProvider
    {
        @SuppressWarnings("unchecked")
        public Object[] getElements(Object inputElement)
        {
            return ((Map<Integer, String>)inputElement).entrySet().toArray();
        }

        public void dispose() {;}
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {;}
    }

    private static final class ArrayPairLabelProvider extends LabelProvider
    {
        @SuppressWarnings("unchecked")
        @Override
        public String getText(Object element)
        {
            return ((Entry<Integer, String>)element).getValue();
        }
    }

    public final void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
            run(vpg, progressMonitor);
        }
        catch (Throwable e)
        {
            throw new InvocationTargetException(e);
        }
        finally
        {
            progressMonitor.done();
        }
    }

    /**
     * Subclasses must override this method; this is where the action-specific VPG work is defined.
     *
     * @param vpg an {@link EclipseVPG} contributed to the <i>vpg</i> extension point; if only one
     *            has been contributed, it will be that; otherwise, the user will have been prompted
     *            to select a VPG, and this will be the VPG selected by the user
     * @param progressMonitor an {@link IProgressMonitor} for displaying status information to the
     *            user (if the operation is long-running)
     *
     * @throws Exception
     */
    protected abstract void run(EclipseVPG vpg, IProgressMonitor progressMonitor) throws Exception;

    ///////////////////////////////////////////////////////////////////////////
    // Utility Methods for Subclasses
    ///////////////////////////////////////////////////////////////////////////

    /** @return the active shell, or <code>null</code> if no shell is active */
    protected Shell getShell()
    {
        return activeShell;
    }
}
