/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Fotzler, UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.parser.ASTSubroutineParNode;
import org.eclipse.photran.internal.core.refactoring.PermuteSubroutineArgsRefactoring;
import org.eclipse.rephraserengine.ui.refactoring.CustomUserInputPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Input page for the change subroutine signature refactoring.
 * 
 * @author Matthew Fotzler
 */
public class PermuteSubroutineArgsInputPage extends CustomUserInputPage<PermuteSubroutineArgsRefactoring>
{
    private Composite top;
    private Composite parent;
    private Group parameterGroup;
    private Button upButton;
    private Button downButton;
    private Table parameterTable;
    private Label statusLabel;
    
    @Override
    public void createControl(Composite parent)
    {
        this.parent = parent;
        top = new Composite(parent, SWT.NONE);
        initializeDialogUnits(top);
        setControl(top);
        top.setLayout(new GridLayout(1,false));

        parameterGroup = new Group(top, SWT.NONE);
        parameterGroup.setLayout(new GridLayout(2,false));
        parameterGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parameterGroup.setText(Messages.PermuteSubroutineArgsInputPage_parameterGroupLabel);
        
        parameterTable = createParameterTable(parameterGroup);
        
        Composite buttonComposite = new Composite(parameterGroup, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(1,false));
        
        upButton = new Button(buttonComposite, SWT.NONE);
        upButton.setText(Messages.PermuteSubroutineArgsInputPage_upButtonLabel);
        upButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        downButton = new Button(buttonComposite, SWT.NONE);
        downButton.setText(Messages.PermuteSubroutineArgsInputPage_downButtonLabel);
        downButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        statusLabel = new Label(top, SWT.NONE);
        statusLabel.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,false,false));
        
        for(ASTSubroutineParNode parameterNode : getRefactoring().getSubroutineParameters())
            createParameterTableItem(parameterNode);
        
        parameterTable.addSelectionListener(new ParameterTableSelectionListener());
        upButton.addSelectionListener(new UpButtonSelectionListener());
        downButton.addSelectionListener(new DownButtonSelectionListener());
        tableSelected();
    }

    protected void createParameterTableItem(ASTSubroutineParNode parameterNode)
    {
        TableItem newItem = new TableItem(parameterTable, SWT.NONE);
        if(parameterNode.isAsterisk())
        {
            newItem.setText(0, "*"); //$NON-NLS-1$
            newItem.setText(3, Boolean.toString(true));
            newItem.setText(4, Boolean.toString(false));
        }
        else
        {
            List<Definition> definitionList = parameterNode.getVariableName().resolveBinding();
            if(definitionList.size() == 1)
            {
                Definition definition = definitionList.get(0);
                newItem.setText(0, definition.getDeclaredName());
                newItem.setText(1, definition.getType().toString());
                
                String intent = new String();
                if(definition.isIntentIn())
                    intent += Messages.PermuteSubroutineArgsInputPage_intentInLabel;
                if(definition.isIntentOut())
                    intent += Messages.PermuteSubroutineArgsInputPage_intentOutLabel;
                
                newItem.setText(2, intent);
                newItem.setText(3, Boolean.toString(false));
                newItem.setText(4, Boolean.toString(definition.isOptional()));
                newItem.setText(5, Boolean.toString(getRefactoring().isUsedWithKeywordInCallStmt(parameterNode)));
            }
        }
        newItem.setData(parameterNode);
        parameterTable.pack();
    }

    protected Table createParameterTable(Group parameterGroup)
    {
        final Table parameterTable = new Table(parameterGroup, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        parameterTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parameterTable.setLinesVisible(true);
        parameterTable.setHeaderVisible(true);
        
        TableColumn nameColumn = new TableColumn(parameterTable, SWT.NONE);
        nameColumn.setText(Messages.PermuteSubroutineArgsInputPage_nameLabel);
        nameColumn.setWidth(100);
        TableColumn typeColumn = new TableColumn(parameterTable, SWT.NONE);
        typeColumn.setText(Messages.PermuteSubroutineArgsInputPage_typeLabel);
        typeColumn.setWidth(100);
        TableColumn intentColumn = new TableColumn(parameterTable, SWT.NONE);
        intentColumn.setText(Messages.PermuteSubroutineArgsInputPage_intentLabel);
        intentColumn.setWidth(100);
        TableColumn alternateReturnColumn = new TableColumn(parameterTable, SWT.NONE);
        alternateReturnColumn.setText(Messages.PermuteSubroutineArgsInputPage_alternateReturnColumnLabel);
        alternateReturnColumn.setWidth(100);
        TableColumn optionalColumn = new TableColumn(parameterTable, SWT.NONE);
        optionalColumn.setText(Messages.PermuteSubroutineArgsInputPage_optionalColumnLabel);
        optionalColumn.setWidth(100);
        TableColumn keywordColumn = new TableColumn(parameterTable, SWT.NONE);
        keywordColumn.setText(Messages.PermuteSubroutineArgsInputPage_keywordedColumnLabel);
        keywordColumn.setWidth(100);
        parameterTable.pack();
        return parameterTable;
    }
    
    protected void tableSelected()
    {
        statusLabel.setText(""); //$NON-NLS-1$
        upButton.setEnabled(true);
        downButton.setEnabled(true);
        setPageComplete(true);
        String statusText = new String();
        
        List<ASTSubroutineParNode> tableNodes = new ArrayList<ASTSubroutineParNode>();
        for(int i = 0; i < parameterTable.getItemCount(); i++)
            tableNodes.add((ASTSubroutineParNode)parameterTable.getItem(i).getData());
        
        int selectedIndex = parameterTable.getSelectionIndex();
        if(parameterTable.getItemCount() == 0)
            return;
        if(selectedIndex < 0 || selectedIndex > parameterTable.getItemCount())
            selectedIndex=0;
        if(selectedIndex == 0)
            upButton.setEnabled(false);
        if(selectedIndex == tableNodes.size()-1)
            downButton.setEnabled(false);
        
        statusText = checkPreconditions(statusText, tableNodes, selectedIndex);
        
        statusLabel.setText(statusText);
        statusLabel.pack(true);
        parameterGroup.pack(true);
        top.pack(true);
        parent.pack(true);
    }

    protected String checkPreconditions(String statusText, List<ASTSubroutineParNode> tableNodes,
        int selectedIndex)
    {
        if(tableNodes.get(selectedIndex).isAsterisk())
        {
            if(selectedIndex-1 > 0 && tableNodes.get(selectedIndex-1).isAsterisk())
            {
                upButton.setEnabled(false);
                statusText += Messages.PermuteSubroutineArgsInputPage_alternateReturnErrorLabel;
            }
            if(selectedIndex+1 < tableNodes.size() && tableNodes.get(selectedIndex+1).isAsterisk())
            {
                downButton.setEnabled(false);
                statusText += Messages.PermuteSubroutineArgsInputPage_alternateReturnErrorLabel;
            }
        }
        
        if(optionalArgumentIsBeforeAlternateReturn(tableNodes))
        {
            setPageComplete(false);
            statusText += Messages.PermuteSubroutineArgsInputPage_optionalArgumentErrorLabel;
        }
        
        if(keywordedArgumentIsBeforeAlternateReturn(tableNodes))
        {
            setPageComplete(false);
            statusText += Messages.PermuteSubroutineArgsInputPage_keywordErrorLabel;
        }
        
        if(tableNodes.size() < 2)
        {
            setPageComplete(false);
            statusText += Messages.PermuteSubroutineArgsInputPage_notEnoughArgumentsErrorLabel;
        }
        return statusText;
    }

    private boolean optionalArgumentIsBeforeAlternateReturn(List<ASTSubroutineParNode> tableNodes)
    {
        for(int i = 0; i < tableNodes.size(); i++)
        {
            if(tableNodes.get(i).getVariableName() != null)
            {
                List<Definition> definitionList = tableNodes.get(i).getVariableName().resolveBinding();
                
                if(definitionList.size() == 1)
                {
                    Definition definition = definitionList.get(0);
                    
                    if (definition.isOptional())
                        for(int j = i+1; j < tableNodes.size(); j++)
                            if(tableNodes.get(j).isAsterisk())
                                return true;
                }
            }
        }
        return false;
    }
    
    private boolean keywordedArgumentIsBeforeAlternateReturn(List<ASTSubroutineParNode> tableNodes)
    {
        for(int i = 0; i < tableNodes.size(); i++)
            if (getRefactoring().isUsedWithKeywordInCallStmt(tableNodes.get(i)))
                for(int j = i+1; j < tableNodes.size(); j++)
                    if(tableNodes.get(j).isAsterisk())
                        return true;
        
        return false;
    }

    private final class ParameterTableSelectionListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent e)
        {
            tableSelected();
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
            widgetSelected(e);
        }
    }

    private final class DownButtonSelectionListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent e)
        {
            if(parameterTable.getSelectionCount() == 1)
            {
                List<ASTSubroutineParNode> originalParameters = getRefactoring().getSubroutineParameters();
                TableItem selectedItem = parameterTable.getSelection()[0];
                int oldIndex = parameterTable.indexOf(selectedItem);
                if(oldIndex < parameterTable.getItemCount()-1)
                {
                    TableItem newItem = new TableItem(parameterTable, SWT.NONE, oldIndex+2);                    
                    for(int i = 0; i < parameterTable.getColumnCount(); i++)
                        newItem.setText(i,selectedItem.getText(i));
                    newItem.setData(selectedItem.getData());
                    parameterTable.remove(oldIndex);
                    parameterTable.select(oldIndex+1);
                    
                    ArrayList<Integer> sigma = new ArrayList<Integer>();
                    for(TableItem item : parameterTable.getItems())
                        sigma.add(originalParameters.indexOf(item.getData()));
                    getRefactoring().setSigma(sigma);
                    
                    tableSelected();
                }
            }
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
            widgetSelected(e);
        }
    }

    private final class UpButtonSelectionListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent e)
        {
            if(parameterTable.getSelectionCount() == 1)
            {
                List<ASTSubroutineParNode> originalParameters = getRefactoring().getSubroutineParameters();
                TableItem selectedItem = parameterTable.getSelection()[0];
                int oldIndex = parameterTable.indexOf(selectedItem);
                if(oldIndex > 0)
                {
                    TableItem newItem = new TableItem(parameterTable, SWT.NONE, oldIndex-1);
                    for(int i = 0; i < parameterTable.getColumnCount(); i++)
                        newItem.setText(i,selectedItem.getText(i));
                    newItem.setData(selectedItem.getData());
                    parameterTable.remove(oldIndex+1);
                    parameterTable.select(oldIndex-1);

                    ArrayList<Integer> sigma = new ArrayList<Integer>();
                    for(TableItem item : parameterTable.getItems())
                        sigma.add(originalParameters.indexOf(item.getData()));
                    getRefactoring().setSigma(sigma);
                    tableSelected();
                }
            }
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
            widgetSelected(e);
        }
    }
}
