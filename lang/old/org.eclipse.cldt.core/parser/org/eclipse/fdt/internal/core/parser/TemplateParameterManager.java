/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class TemplateParameterManager
{	
	protected void reset()
	{
		list = Collections.EMPTY_LIST;
		emptySegmentCount = 0;
	}
	
	private TemplateParameterManager(int i)
	{
		reset();
		counterId = i;
	}
	
	private final int counterId;
	private List list;
	private int emptySegmentCount;

	public List getTemplateArgumentsList()
	{
		return list;
	}
	
	public void addSegment( List inputSegment )
	{
		if( inputSegment == null )
		{
			if( list == Collections.EMPTY_LIST )
				++emptySegmentCount;
			else
				list.add( null );
		}
		else
		{
			if( list == Collections.EMPTY_LIST )
			{
				list = new ArrayList();
				for( int i = 0; i < emptySegmentCount; ++i )
					list.add( null );
			}
			list.add( inputSegment );
		}
	}

	private static final int NUMBER_OF_INSTANCES = 8;
	private static final boolean [] instancesUsed = new boolean[ NUMBER_OF_INSTANCES ];
	private static final TemplateParameterManager [] counters = new TemplateParameterManager[ NUMBER_OF_INSTANCES ];
	private static int counter = 8;
	static
	{
		for( int i = 0; i < NUMBER_OF_INSTANCES; ++i )
		{
			instancesUsed[ i ] = false;
			counters[ i ] = new TemplateParameterManager( i );
		}
	}
	/**
	 * @return
	 */
	public synchronized static TemplateParameterManager getInstance() {
		int index = findFreeCounter();
		if( index == -1 )
			return new TemplateParameterManager(++counter);
		instancesUsed[ index ] = true;
		return counters[ index ];
	}

	public synchronized static void returnInstance( TemplateParameterManager c )
	{
		if( c.counterId > 0 && c.counterId < NUMBER_OF_INSTANCES )
			instancesUsed[ c.counterId ] = false;
		c.reset();
	}
	
	/**
	 * @return
	 */
	private static int findFreeCounter() {
		for( int i = 0; i < NUMBER_OF_INSTANCES; ++i )
			if( instancesUsed[i] == false )
				return i;
		return -1;
	}
	
}