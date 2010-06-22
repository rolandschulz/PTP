/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.analysis.flow;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rephraserengine.core.util.ReverseIterator;

/**
 * A node in a control flow graph.
 *
 * @author Jeff Overbey
 *
 * @param T type of data associated with each flowgraph node
 *
 * @since 3.0
 */
public final class FlowGraphNode<T>
{
    private final String name;
    private final T data;
    private final List<FlowGraphNode<T>> successors, precedessors;

    public FlowGraphNode(String name, T data)
    {
        this.name = name;
        this.data = data;
        this.successors = new LinkedList<FlowGraphNode<T>>();
        this.precedessors = new LinkedList<FlowGraphNode<T>>();
    }

    public String getName()
    {
        return name;
    }

    public T getData()
    {
        return data;
    }

    public void connectTo(FlowGraphNode<T> node)
    {
        this.successors.add(node);
        node.precedessors.add(this);
    }

    public void connectTo(FlowGraphNode<T>... nodes)
    {
        for (FlowGraphNode<T> n : nodes)
            connectTo(n);
    }

    public List<FlowGraphNode<T>> getSuccessors()
    {
        return successors;
    }

    public Iterable<FlowGraphNode<T>> getSuccessorsInReverse()
    {
        return new Iterable<FlowGraphNode<T>>()
        {
            public Iterator<FlowGraphNode<T>> iterator()
            {
                return new ReverseIterator<FlowGraphNode<T>>(successors);
            }
        };
    }

    public List<FlowGraphNode<T>> getPrecedessors()
    {
        return precedessors;
    }

    Iterable<FlowGraphNode<T>> subgraphInReversePostOrder()
    {
        final LinkedList<FlowGraphNode<T>> result = new LinkedList<FlowGraphNode<T>>();

        this.inReversePostOrder(new Callback()
        {
            @Override public void handle(FlowGraphNode<T> node)
            {
                result.add(node);
            }
        });

        return result;
    }

    Iterable<T> subgraphDataInReversePostOrder()
    {
        final LinkedList<T> result = new LinkedList<T>();

        this.inReversePostOrder(new Callback()
        {
            @Override public void handle(FlowGraphNode<T> node)
            {
                result.add(node.getData());
            }
        });

        return result;
    }

    private void inReversePostOrder(Callback callback)
    {
        this.inReversePostOrder(new HashSet<FlowGraphNode<T>>(), callback);
    }

    private void inReversePostOrder(HashSet<FlowGraphNode<T>> visited, Callback callback)
    {
        visited.add(this);

        for (FlowGraphNode<T> n : getSuccessorsInReverse())
            if (!visited.contains(n))
                n.inReversePostOrder(visited, callback);

        callback.handle(this);
    }

    Iterable<FlowGraphNode<T>> subgraphInPreOrder()
    {
        final LinkedList<FlowGraphNode<T>> result = new LinkedList<FlowGraphNode<T>>();

        this.inPreOrder(new Callback()
        {
            @Override public void handle(FlowGraphNode<T> node)
            {
                result.add(node);
            }
        });

        return result;
    }

    Iterable<T> subgraphDataInPreOrder()
    {
        final LinkedList<T> result = new LinkedList<T>();

        this.inPreOrder(new Callback()
        {
            @Override public void handle(FlowGraphNode<T> node)
            {
                result.add(node.getData());
            }
        });

        return result;
    }

    private void inPreOrder(Callback callback)
    {
        this.inPreOrder(new HashSet<FlowGraphNode<T>>(), callback);
    }

    private void inPreOrder(HashSet<FlowGraphNode<T>> visited, Callback callback)
    {
        visited.add(this);
        callback.handle(this);

        for (FlowGraphNode<T> n : getSuccessors())
            if (!visited.contains(n))
                n.inPreOrder(visited, callback);
    }

    private abstract class Callback { public abstract void handle(FlowGraphNode<T> node); }

    @Override public String toString()
    {
        return String.format("%s: %s", name, data.toString()); //$NON-NLS-1$
    }
}