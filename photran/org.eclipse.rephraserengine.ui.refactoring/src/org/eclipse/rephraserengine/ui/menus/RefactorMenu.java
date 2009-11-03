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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.rephraserengine.core.IResourceFilter;
import org.eclipse.rephraserengine.core.refactorings.IEditorRefactoring;
import org.eclipse.rephraserengine.core.refactorings.IResourceRefactoring;
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
import org.eclipse.ui.menus.CommandContributionItemParameter;

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
    private static final String REFACTORING_EXTENSION_POINT_ID = "org.eclipse.rephraserengine.ui.refactoring.refactorings";

    private WorkbenchSelectionInfo selection;

    @Override
    protected IContributionItem[] getContributionItems()
    {
        selection = new WorkbenchSelectionInfo();
        List<IContributionItem> result = loadRefactoringsFromExtensionPoint();
        return result.toArray(new IContributionItem[result.size()]);
    }

    private List<IContributionItem> loadRefactoringsFromExtensionPoint()
    {
        return loadRefactoringsFrom(Platform.getExtensionRegistry().getConfigurationElementsFor(REFACTORING_EXTENSION_POINT_ID));
    }

    private List<IContributionItem> loadRefactoringsFrom(IConfigurationElement[] configs)
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
        if (elt.getName().equals("resourceFilter"))
        {
            IResourceFilter resourceFilter = (IResourceFilter)elt.createExecutableExtension("class");
            selection = new WorkbenchSelectionInfo(resourceFilter); // since resourceFilter changed
        }
        else if (elt.getName().equals("group"))
        {
            if (!result.isEmpty() && !(result.getLast() instanceof SeparatorContributionItem))
                result.add(new SeparatorContributionItem());

            result.addAll(loadRefactoringsFrom(elt.getChildren()));
        }
        else if (elt.getName().equals("resourceRefactoring"))
        {
            addResourceRefactoring(elt, result);
        }
        else if (elt.getName().equals("editorRefactoring"))
        {
            addEditorRefactoring(elt, result);
        }
        else if (elt.getName().equals("command"))
        {
            addCommand(elt, result);
        }
    }

    @SuppressWarnings("unchecked")
    private void addResourceRefactoring(IConfigurationElement elt,
        LinkedList<IContributionItem> result) throws CoreException
    {
        if (selection.someFilesAreSelected())
        {
            if (elt.getAttribute("class") != null && environmentOK(elt))
            {
                IResourceRefactoring refactoring = (IResourceRefactoring)elt.createExecutableExtension("class");
                String label = elt.getAttribute("label");
                CustomUserInputPage customInputPage =
                    elt.getAttribute("inputPage") == null
                    ? null
                    : (CustomUserInputPage)elt.createExecutableExtension("inputPage");
                result.add(new ResourceRefactoringContributionItem(
                    refactoring,
                    label,
                    customInputPage,
                    selection.getAllFilesInSelectedResources()));
            }
            else if (elt.getAttribute("command") != null)
            {
                result.add(commandContribution(elt.getAttribute("command")));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addEditorRefactoring(IConfigurationElement elt,
        LinkedList<IContributionItem> result) throws CoreException
    {
        if (selection.editingAnIFile() && selection.isTextSelectedInEditor())
        {
            if (elt.getAttribute("class") != null && environmentOK(elt))
            {
                IEditorRefactoring refactoring = (IEditorRefactoring)elt.createExecutableExtension("class");
                String label = elt.getAttribute("label");
                CustomUserInputPage customInputPage =
                    elt.getAttribute("inputPage") == null
                    ? null
                    : (CustomUserInputPage)elt.createExecutableExtension("inputPage");
                result.add(new EditorRefactoringContributionItem(
                    refactoring,
                    label,
                    customInputPage,
                    selection.getFileInEditor(),
                    selection.getSelectionInEditor()));
            }
            else if (elt.getAttribute("command") != null)
            {
                result.add(commandContribution(elt.getAttribute("command")));
            }
        }
    }
    
    private boolean environmentOK(IConfigurationElement elt)
    {
        if (elt.getAttribute("require_env") != null)
            return System.getenv(elt.getAttribute("require_env")) != null;
        else
            return true;
    }

    private void addCommand(IConfigurationElement elt, LinkedList<IContributionItem> result)
    {
        result.add(commandContribution(elt.getAttribute("id")));
    }

    private CommandContributionItem commandContribution(String commandID)
    {
        CommandContributionItemParameter param = new CommandContributionItemParameter(
            Workbench.getInstance().getServiceLocator(),    // Service locator
            null,                                           // ID
            commandID,                                      // Command ID
            CommandContributionItem.STYLE_PUSH);            // Style
        return new CommandContributionItem(param);
    }

    @SuppressWarnings("unchecked")
    private static class ResourceRefactoringContributionItem extends ContributionItem
    {
        private IResourceRefactoring refactoring;
        private String label;
        private CustomUserInputPage customInputPage;
        private List<IFile> selectedFiles;

        public ResourceRefactoringContributionItem(
            IResourceRefactoring refactoring,
            String label,
            CustomUserInputPage customInputPage,
            List<IFile> selectedFiles)
        {
            this.refactoring = refactoring;
            this.label = label != null ? label : refactoring.getName() + "...";
            this.customInputPage = customInputPage;
            this.selectedFiles = selectedFiles;
        }

        public void fill(Menu parent, int index)
        {
            MenuItem menuItem = new MenuItem(parent, SWT.NONE, index++);
            menuItem.setText(label);
            menuItem.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    refactoring.initialize(selectedFiles);
                    new RefactoringAction(refactoring, customInputPage).run();
                }
            });
        }
    };

    @SuppressWarnings("unchecked")
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
            this.label = label != null ? label : refactoring.getName() + "...";
            this.customInputPage = customInputPage;
            this.fileInEditor = fileInEditor;
            this.textSelection = textSelection;
        }

        public void fill(Menu parent, int index)
        {
            MenuItem menuItem = new MenuItem(parent, SWT.NONE, index++);
            menuItem.setText(label);
            menuItem.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    refactoring.initialize(fileInEditor, textSelection);
                    new RefactoringAction(refactoring, customInputPage).run();
                }
            });
        }
    }

    private static class SeparatorContributionItem extends ContributionItem
    {
        public void fill(Menu parent, int index)
        {
            new MenuItem(parent, SWT.SEPARATOR, index);
        }
    };
}
