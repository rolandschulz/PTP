/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.ompcfg;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory.OMPCFGResult;

/**
 * Control Flow Graph related to OMP Analysis
 * @author pazel
 *
 */
public class OMPCFG
{
	private static final boolean traceOn=false;
    protected PASTOMPPragma          pragma_        = null;
    protected IASTStatement          statement_     = null;
    protected OMPCFGNode             rootNode_      = null;
    protected OMPCFGNode             termNode_      = null;
    
    protected OMPCFGResult.Chain     rootChain_ = null;
    protected LinkedList             unconnectedChains_ = null;
    protected LinkedList             unresolvedChains_  = null;
    protected Hashtable              labelMap_          = null;
    
    public OMPCFG(PASTOMPPragma      pragma, 
                  IASTStatement      statement, 
                  OMPCFGResult.Chain rootChain,
                  LinkedList         unconnectedChains, 
                  LinkedList         unresolvedChains, 
                  Hashtable          labelMap)
    {
        pragma_        = pragma;
        statement_     = statement;
        rootNode_      = rootChain.getHeadNode();
        termNode_      = rootChain.getTailNode();
        
        rootChain_         = rootChain;
        unconnectedChains_ = unconnectedChains;
        unresolvedChains_  = unresolvedChains;
        labelMap_          = labelMap;
        
        numberNodes();
    }
    
    /**
     * getStatement - accessor to statement for this cfg
     * @return IASTStatement
     */
    public IASTStatement getStatement()
    {
        return statement_;
    }
    
    /**
     * getPragma - get the pragme for the cfg
     * @return PASTOMPPragma
     */
    public PASTOMPPragma getPragma()
    {
        return pragma_;
    }
    
    /**
     * getRoot - access the root of the cfg
     * @return - OMPCFGNode
     */
    public OMPCFGNode getRoot()
    {
        return rootNode_;
    }
    
    /**
     * getTermNode - get the terminal node (assuming one)
     * @return OMPCFGNode
     */
    public OMPCFGNode getTermNode()
    {
        return termNode_;
    }
    
    // The following for printing out the graph
    
    protected Stack       graphStack_    = new Stack();
    protected HashSet     usedNodes_     = new HashSet();
    protected PrintStream graphOut_      = null;
    protected int         currentNumber_ = 0;
    
    public void printCFG(PrintStream printOut)
    {
        graphOut_ = printOut;

        usedNodes_.clear();
        
        numberNodes();
        
        visitGraph();
    }
    
    private void numberNodes()
    {
        currentNumber_ = 0;
        graphStack_.clear();
        graphStack_.push(rootNode_);
        while(!graphStack_.isEmpty()) {
            OMPCFGNode node = (OMPCFGNode)graphStack_.pop();
            node.setId(currentNumber_++);
            // Add in other nodes
            OMPCFGNode [] nodes = node.getOutNodes();
            for(int i=0; i<nodes.length; i++) {
                if (nodes[i].getId()==-1)  // only if not assigned number
                    graphStack_.push(nodes[i]);
            }
        }
    }
    
    private void visitGraph()
    {
        graphStack_.clear();
        graphStack_.push(rootNode_);
        while(!graphStack_.isEmpty()) {
            OMPCFGNode node = (OMPCFGNode)graphStack_.pop();
            printNode(node);
            usedNodes_.add(node);
            // Add in other nodes
            OMPCFGNode [] nodes = node.getOutNodes();
            for(int i=0; i<nodes.length; i++) {
                if (!(nodes[i] instanceof OMPCFGNode)) {
                    if(traceOn)System.out.println("Node " + nodes[i].getId() + 
                            " has out node of type: "+nodes[i].getClass());
                    continue;
                }
                if (!usedNodes_.contains(nodes[i]))  // only if not processed
                    graphStack_.push(nodes[i]);
            }
            
        }
    }
    
    private void printNode(OMPCFGNode node)
    {
        int num = node.getId();
        if(traceOn)graphOut_.println("Node "+num+" ------------------ "+getShortClassName(node.getClass()));
        
        // some contents of the node
        if (node instanceof OMPBasicBlock) {
            OMPBasicBlock obb = (OMPBasicBlock)node;
            IASTStatement [] stmts = obb.getStatements();
            for(int i=0; i<stmts.length; i++)
                if(traceOn)System.out.println("       -------- "+getShortClassName(stmts[i].getClass()));
        }
        
        
        
        OMPCFGNode [] nodes = node.getOutNodes();
        if(traceOn)graphOut_.print("      out to the following "+nodes.length+" nodes:");
        for(int i=0; i<nodes.length; i++) {
            int index = nodes[i].getId();
            if(traceOn)graphOut_.print(" "+index);
        }
        if(traceOn)System.out.println();
        OMPCFGNode [] inodes = node.getInNodes();
        if(traceOn)graphOut_.print("      in from the following "+inodes.length+" nodes:");
        for(int i=0; i<inodes.length; i++) {
            int index = inodes[i].getId();
            if(traceOn)graphOut_.print(" "+index);
        }
        if(traceOn)System.out.println();

    }
    
    private String getShortClassName(Class c)
    {
        String n = c.toString();
        int lastIndex = n.lastIndexOf('.');
        return n.substring(lastIndex+1);
    }

}

