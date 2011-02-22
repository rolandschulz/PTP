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
package org.eclipse.rephraserengine.ui.menus;

import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.rephraserengine.core.refactorings.IEditorRefactoring;
import org.eclipse.rephraserengine.core.refactorings.IResourceRefactoring;
import org.eclipse.rephraserengine.core.resources.IResourceFilter;
import org.eclipse.rephraserengine.internal.ui.actions.RefactoringAction;
import org.eclipse.rephraserengine.ui.WorkbenchSelectionInfo;
import org.eclipse.rephraserengine.ui.refactoring.CustomUserInputPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.menus.CommandContributionItem;

/**
 * A dynamically-populated set of menu items for the Refactor menu in the menu bar and for various
 * pop-up menus.
 * <p>
 * This class iterates through the contributions to the <i>refactorings</i> extension point and uses
 * them to populate the menu.
 * <ul>
 * <li> When a <i>resourceRefactoring</i> is contributed, if there is some way to get at least one
 *      {@link IResource} from the current selection in the workbench, then that refactoring is
 *      shown in the menu.
 * <li> When a <i>editorRefactoring<i> is selected, if an editor is active and there is an
 *      {@link ITextSelection} available from that editor, then that refactoring is
 *      shown in the menu.
 * <li> When a <i>command</i> is contributed, the Eclipse Platform determines how it is displayed,
 *      whether it is enabled, etc.  It is always shown in the menu, but it might be disabled
 *      depending on the current selection.
 * </ul>
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
@SuppressWarnings("restriction")
public final class RefactorMenu extends CompoundContributionItem
{
    private static final String REFACTORING_EXTENSION_POINT_ID = "org.eclipse.rephraserengine.ui.refactoring.refactorings"; //$NON-NLS-1$

    private WorkbenchSelectionInfo selection;

    /** @since 3.0 */
    @Override
    public IContributionItem[] getContributionItems()
    {
        selection = new WorkbenchSelectionInfo();
        LinkedList<IContributionItem> result = loadRefactoringsFromExtensionPoint();
        return fixMenu(result);
    }

    private IContributionItem[] fixMenu(LinkedList<IContributionItem> result)
    {
        // The last item in a menu should never be a separator, and there
        // should never be two separators in a row (e.g., if this menu is
        // followed by another group of commands).  Both of those look
        // strange, and they can be avoided by never ending the list of
        // contribution items with a separator.
        if (!result.isEmpty() && result.getLast() instanceof SeparatorContributionItem)
            result.removeLast();

        // Adds an informative item ("No refactorings available") when
        // necessary, since an empty menu looks like a bug.
        if (result.isEmpty())
            return new IContributionItem[] { new EmptyMenuContributionItem() };

        return result.toArray(new IContributionItem[result.size()]);
    }

    private LinkedList<IContributionItem> loadRefactoringsFromExtensionPoint()
    {
        return loadRefactoringsFrom(Platform.getExtensionRegistry().getConfigurationElementsFor(REFACTORING_EXTENSION_POINT_ID));
    }

    private LinkedList<IContributionItem> loadRefactoringsFrom(IConfigurationElement[] configs)
    {
        LinkedList<IContributionItem> result = new LinkedList<IContributionItem>();

        for (IConfigurationElement elt : configs)
        {
            try
            {
                processConfigElt(elt, result);
            }
            catch (CoreException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    private void processConfigElt(IConfigurationElement elt, LinkedList<IContributionItem> result)
        throws CoreException
    {
        if (elt.getName().equals("resourceFilter")) //$NON-NLS-1$
        {
            IResourceFilter resourceFilter = (IResourceFilter)elt.createExecutableExtension("class"); //$NON-NLS-1$
            selection = new WorkbenchSelectionInfo(resourceFilter); // since resourceFilter changed
        }
        else if (elt.getName().equals("group")) //$NON-NLS-1$
        {
            if (!result.isEmpty() && !(result.getLast() instanceof SeparatorContributionItem))
                result.add(new SeparatorContributionItem());

            result.addAll(loadRefactoringsFrom(elt.getChildren()));
        }
        else if (elt.getName().equals("resourceRefactoring")) //$NON-NLS-1$
        {
            addResourceRefactoring(elt, result);
        }
        else if (elt.getName().equals("editorRefactoring")) //$NON-NLS-1$
        {
            addEditorRefactoring(elt, result);
        }
        else if (elt.getName().equals("command")) //$NON-NLS-1$
        {
            addCommand(elt, result);
        }
    }

    @SuppressWarnings("rawtypes")
    private void addResourceRefactoring(IConfigurationElement elt,
        LinkedList<IContributionItem> result) throws CoreException
    {
        if (selection.someFilesAreSelected())
        {
            if (elt.getAttribute("class") != null && environmentOK(elt)) //$NON-NLS-1$
            {
                try
                {
                    IResourceRefactoring refactoring = (IResourceRefactoring)elt.createExecutableExtension("class"); //$NON-NLS-1$
                    String label = elt.getAttribute("label"); //$NON-NLS-1$
                    CustomUserInputPage customInputPage =
                        elt.getAttribute("inputPage") == null //$NON-NLS-1$
                        ? null
                        : (CustomUserInputPage)elt.createExecutableExtension("inputPage"); //$NON-NLS-1$
                    result.add(new ResourceRefactoringContributionItem(
                        refactoring,
                        label,
                        customInputPage,
                        selection));
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
            else if (elt.getAttribute("command") != null) //$NON-NLS-1$
            {
                result.add(commandContribution(elt.getAttribute("command"))); //$NON-NLS-1$
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void addEditorRefactoring(IConfigurationElement elt,
        LinkedList<IContributionItem> result) throws CoreException
    {
        if (selection.editingAnIFile() && selection.isTextSelectedInEditor())
        {
            if (elt.getAttribute("class") != null && environmentOK(elt)) //$NON-NLS-1$
            {
                try
                {
                    IEditorRefactoring refactoring = (IEditorRefactoring)elt.createExecutableExtension("class"); //$NON-NLS-1$
                    String label = elt.getAttribute("label"); //$NON-NLS-1$
                    CustomUserInputPage customInputPage =
                        elt.getAttribute("inputPage") == null //$NON-NLS-1$
                        ? null
                        : (CustomUserInputPage)elt.createExecutableExtension("inputPage"); //$NON-NLS-1$
                    result.add(new EditorRefactoringContributionItem(
                        refactoring,
                        label,
                        customInputPage,
                        selection.getFileInEditor(),
                        selection.getSelectionInEditor()));
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
            else if (elt.getAttribute("command") != null) //$NON-NLS-1$
            {
                result.add(commandContribution(elt.getAttribute("command"))); //$NON-NLS-1$
            }
        }
    }

    private boolean environmentOK(IConfigurationElement elt)
    {
        if (elt.getAttribute("require_env") != null) //$NON-NLS-1$
            return System.getenv(elt.getAttribute("require_env")) != null; //$NON-NLS-1$
        else
            return true;
    }

    private void addCommand(IConfigurationElement elt, LinkedList<IContributionItem> result)
    {
        result.add(commandContribution(elt.getAttribute("id"))); //$NON-NLS-1$
    }

    @SuppressWarnings("deprecation")
    private CommandContributionItem commandContribution(String commandID)
    {
//        CommandContributionItemParameter param = new CommandContributionItemParameter(
//            Workbench.getInstance().getServiceLocator(),    // Service locator
//            null,                                           // ID
//            commandID,                                      // Command ID
//            CommandContributionItem.STYLE_PUSH);            // Style
//        return new CommandContributionItem(param);
        
        return new CommandContributionItem(
            Workbench.getInstance().getActiveWorkbenchWindow(), // Service locator
            null,                                           // ID
            commandID,                                      // Command ID
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            CommandContributionItem.STYLE_PUSH);            // Style
    }

    @SuppressWarnings("rawtypes")
    private static class ResourceRefactoringContributionItem extends ContributionItem
    {
        private IResourceRefactoring refactoring;
        private String label;
        private CustomUserInputPage customInputPage;
        private WorkbenchSelectionInfo selection;

        public ResourceRefactoringContributionItem(
            IResourceRefactoring refactoring,
            String label,
            CustomUserInputPage customInputPage,
            WorkbenchSelectionInfo selection)
        {
            this.refactoring = refactoring;
            this.label = label != null ? label : refactoring.getName() + "..."; //$NON-NLS-1$
            this.customInputPage = customInputPage;
            this.selection = selection;
        }

        @Override public void fill(Menu parent, int index)
        {
            MenuItem menuItem = new MenuItem(parent, SWT.NONE, index);
            menuItem.setText(label);
            menuItem.addSelectionListener(new SelectionAdapter()
            {
                @SuppressWarnings("unchecked")
                @Override public void widgetSelected(SelectionEvent e)
                {
                    refactoring.initialize(selection.getAllFilesInSelectedResources());
                    new RefactoringAction(refactoring, customInputPage).run();
                }
            });
        }
    };

    @SuppressWarnings("rawtypes")
    private static class EditorRefactoringContributionItem extends ContributionItem
    {
        private IEditorRefactoring refactoring;
        private String label;
        private CustomUserInputPage customInputPage;
        private IFile fileInEditor;
        private ITextSelection textSelection;

        public EditorRefactoringContributionItem(
            IEditorRefactoring refactoring,
            String label,
            CustomUserInputPage customInputPage,
            IFile fileInEditor,
            ITextSelection textSelection)
        {
            this.refactoring = refactoring;
            this.label = label != null ? label : refactoring.getName() + "..."; //$NON-NLS-1$
            this.customInputPage = customInputPage;
            this.fileInEditor = fileInEditor;
            this.textSelection = textSelection;
        }

        @Override public void fill(Menu parent, int index)
        {
            MenuItem menuItem = new MenuItem(parent, SWT.NONE, index);
            menuItem.setText(label);
            menuItem.addSelectionListener(new SelectionAdapter()
            {
                @SuppressWarnings("unchecked")
                @Override public void widgetSelected(SelectionEvent e)
                {
                    refactoring.initialize(fileInEditor, textSelection);
                    new RefactoringAction(refactoring, customInputPage).run();
                }
            });
        }
    }

    private static class SeparatorContributionItem extends ContributionItem
    {
        @Override public void fill(Menu parent, int index)
        {
            new MenuItem(parent, SWT.SEPARATOR, index);
        }
    };

    private static class EmptyMenuContributionItem extends ContributionItem
    {
        @Override public void fill(Menu parent, int index)
        {
            MenuItem item = new MenuItem(parent, SWT.NONE, index);
            item.setText(Messages.RefactorMenu_NoRefactoringsAvailable);
            item.setEnabled(false);
        }
    };
    
    /** @since 3.0 */
    public boolean isEmpty()
    {
        IContributionItem[] items = getContributionItems();
        if (items.length == 0)
            return true;
        else if (items.length == 1 && items[0] instanceof EmptyMenuContributionItem)
            return true;
        else
            return false;
    }
}