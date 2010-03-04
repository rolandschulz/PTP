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
package org.eclipse.rephraserengine.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.rephraserengine.core.refactorings.IRefactoring;
import org.eclipse.rephraserengine.core.refactorings.UserInputBoolean;
import org.eclipse.rephraserengine.core.refactorings.UserInputString;
import org.eclipse.rephraserengine.internal.ui.UIUtil;
import org.eclipse.rephraserengine.ui.menus.RefactorMenu;
import org.eclipse.rephraserengine.ui.refactoring.CustomUserInputPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * A default action for refactorings, which provides a refactoring wizard and optionally generates
 * a simple user input page for that wizard based on {@link UserInputString} and
 * {@link UserInputBoolean} method annotations..
 *
 * @author Jeff Overbey
 *
 * @see RefactorMenu
 * @see UserInputString
 * @see UserInputBoolean
 */
public class RefactoringAction<T extends Refactoring>
    implements IWorkbenchWindowActionDelegate,
               //IEditorActionDelegate,
               IRunnableWithProgress
{
    private IWorkbenchWindow activeWindow = null;
    private Shell activeShell = null;

    protected IRefactoring refactoring;
    protected CustomUserInputPage<T> customUserInputPage = null;

    public RefactoringAction(IRefactoring refactoring)
    {
        this(refactoring, null);
    }

    public RefactoringAction(IRefactoring refactoring,
                             CustomUserInputPage<T> customUserInputPage)
    {
        this.refactoring = refactoring;
        this.customUserInputPage = customUserInputPage;
    }

    public void init(IWorkbenchWindow window)
    {
        activeWindow = window;
        if (activeWindow != null)
            activeShell = activeWindow.getShell();
    }

    public void dispose() {;}
    public void selectionChanged(IAction action, ISelection selection) {;}

    public void run()
    {
        run((IAction)null);
    }

    public void run(IAction action)
    {
        if (!(refactoring instanceof Refactoring))
        {
            UIUtil.displayErrorDialog("ERROR: " + refactoring.getClass().getName() +
                " is not a subclass of org.eclipse.ltk.core.refactoring.Refactoring." +
                " A custom Action must be provided.");
            return;
        }

        IProgressService context = PlatformUI.getWorkbench().getProgressService();
        ISchedulingRule lockEntireWorkspace = ResourcesPlugin.getWorkspace().getRoot();
        try
        {
            context.runInUI(context, this, lockEntireWorkspace);
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
            MessageDialog.openError(
                    activeShell,
                    "Unhandled Exception",
                    e.getMessage());
        }
        catch (InterruptedException e)
        {
            // Do nothing
        }
    }

    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        if (filesSavedAsNecessary())
        {
            String name = refactoring.getName();
            DefaultRefactoringWizard wizard = new DefaultRefactoringWizard();
            RefactoringWizardOpenOperation wiz = new RefactoringWizardOpenOperation(wizard);
            if (activeShell == null) activeShell = UIUtil.determineActiveShell();
            wiz.run(activeShell, name);
        }
    }

    private boolean filesSavedAsNecessary()
    {
        return UIUtil.askUserToSaveModifiedFiles();
    }

    public class DefaultRefactoringWizard extends RefactoringWizard
    {
        @SuppressWarnings("unchecked")
        public DefaultRefactoringWizard()
        {
            // CHECK_INITIAL_CONDITIONS_ON_OPEN causes the initial conditions to be checked
            // twice, which may lead to duplicate or missing error messages
            // (missing if, say, an INCLUDE could not be found, but the AST was already
            // loaded into the workspace on the second invocation)
            super((Refactoring)refactoring, DIALOG_BASED_USER_INTERFACE /*| CHECK_INITIAL_CONDITIONS_ON_OPEN*/);
            setNeedsProgressMonitor(true);
            setChangeCreationCancelable(false);
            setWindowTitle(getRefactoring().getName());

            if (customUserInputPage != null)
                customUserInputPage.setRefactoring((T)getRefactoring());
        }

        @Override
        protected final void addUserInputPages()
        {
            setDefaultPageTitle(refactoring.getName());
            if (customUserInputPage != null)
                addPage(customUserInputPage);
            else if (hasAnnotatedMethods())
                addPage(new SimpleUserInputWizardPage(refactoring.getName()));
            else
                addPage(new NoUserInputWizardPage(refactoring.getName()));
        }

        private boolean hasAnnotatedMethods()
        {
            for (Method method : refactoring.getClass().getMethods())
            {
                if (method.getAnnotation(UserInputString.class) != null
                    || method.getAnnotation(UserInputBoolean.class) != null)
                {
                    return true;
                }
            }

            return false;
        }
    }

    private final class NoUserInputWizardPage extends UserInputWizardPage
    {
        private NoUserInputWizardPage(String name)
        {
            super(name);
        }

        public void createControl(Composite parent)
        {
            Composite top = new Composite(parent, SWT.NONE);
            initializeDialogUnits(top);
            setControl(top);

            top.setLayout(new GridLayout(1, false));

            Label lbl = new Label(top, SWT.NONE);
            lbl.setText(
                "Click OK to run the " +
                refactoring.getName() +
                " refactoring.\n" +
                "To see what changes will be made, click Preview.");
        }
    }

    private final class SimpleUserInputWizardPage extends UserInputWizardPage
    {
        private Control firstField = null;

        private SimpleUserInputWizardPage(String name)
        {
            super(name);
        }

        public void createControl(Composite parent)
        {
            Composite top = new Composite(parent, SWT.NONE);
            initializeDialogUnits(top);
            setControl(top);

            top.setLayout(new GridLayout(2, false));

            addContolsFromAnnotatedFields(top);
        }

        private void addContolsFromAnnotatedFields(Composite group)
        {
            for (Method method : refactoring.getClass().getMethods())
            {
                UserInputString stringAnnotation = method.getAnnotation(UserInputString.class);
                if (stringAnnotation != null)
                {
                    checkMethodSignature(method, String.class);
                    String defaultValue = getDefaultValue(refactoring, stringAnnotation.defaultValueMethod());
                    addTextField(group, stringAnnotation.label(), defaultValue, method);
                }

                UserInputBoolean annotation = method.getAnnotation(UserInputBoolean.class);
                if (annotation != null)
                {
                    checkMethodSignature(method, Boolean.TYPE);
                    addCheckBoxField(group, annotation.label(), annotation.defaultValue(), method);
                }
            }
        }

        private void checkMethodSignature(Method method, Class<?> parameterType)
        {
            if (!method.getReturnType().equals(Void.TYPE)
                || method.getParameterTypes().length != 1
                || !method.getParameterTypes()[0].equals(parameterType))
            {
                throw new IllegalArgumentException(
                    "The method " + method.getName() + " may not have the @UserInput " +
                    "annotation unless it has the following signature:\n" +
                    "    void " + method.getName() + "(" + parameterType.getSimpleName() + ")");
            }
        }

        private String getDefaultValue(IRefactoring refactoring, String defaultValueMethod)
        {
            try
            {
                if (defaultValueMethod != null && !defaultValueMethod.equals(""))
                {
                    Method getter = refactoring.getClass().getMethod(defaultValueMethod);
                    String result = getter.invoke(refactoring).toString();
                    return result == null ? "" : result;
                }
                else return "";
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                return "";
            }
        }

        private void addTextField(Composite group, String label, String defaultValue, final Method method)
        {
            Label lbl = new Label(group, SWT.NONE);
            lbl.setText(label);

            final Text newNameField = new Text(group, SWT.BORDER);
            newNameField.setText(defaultValue);
            setString(method, defaultValue);
            newNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            newNameField.selectAll();
            newNameField.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent e)
                {
                    setString(method, newNameField.getText());
                }
            });

            if (firstField == null) firstField = newNameField;
        }

        private void setString(Method method, String value)
        {
            try
            {
                method.invoke(refactoring, value);
            }
            catch (Exception x)
            {
                throw new Error(x);
            }
        }

        private void addCheckBoxField(Composite group, String label, boolean defaultValue, final Method method)
        {
            new Label(group, SWT.NONE).setText("");

            final Button button = new Button(group, SWT.CHECK);
            button.setText(label);
            button.setSelection(defaultValue);
            setBoolean(method, defaultValue);
            button.addSelectionListener(new SelectionListener()
            {
                public void widgetDefaultSelected(SelectionEvent e)
                {
                    widgetSelected(e);
                }

                public void widgetSelected(SelectionEvent e)
                {
                    setBoolean(method, button.getSelection());
                }
            });

            if (firstField == null) firstField = button;
        }

        private void setBoolean(Method method, boolean isChecked)
        {
            try
            {
                method.invoke(refactoring, isChecked);
            }
            catch (Exception x)
            {
                throw new Error(x);
            }
        }
    }
}
