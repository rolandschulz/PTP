#*******************************************************************************
# Copyright (c) 2000, 2005 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# 
# Contributors:
#     IBM Corporation - initial API and implementation
#*******************************************************************************

# Makefile for OMPI libraries on Mac OS X

PREFIX=dbg
ARCH_PREFIX=macosx
VERSION=1.0.0

DBG_LIB=lib$(PREFIX)-$(ARCH_PREFIX)-$(VERSION).jnilib

CC = gcc
JAVAH = javah
#DEBUG = -g
CFLAGS = -c $(DEBUG) -I/System/Library/Frameworks/JavaVM.framework/Headers
LFLAGS = -dynamiclib -single_module -framework JavaVM -flat_namespace -undefined suppress
OBJECTS = dbg_jni.o
SRCS = dbg_jni.c
HDRS = dbg_jni.h

all: $(DBG_LIB)

.c.o:
	$(CC) $(CFLAGS) $*.c

$(DBG_LIB): $(OBJECTS)
	$(CC) -o $(DBG_LIB) $(LFLAGS) $(OBJECTS)

$(HDRS):
	$(JAVAH) -o $(HDRS) -classpath ../../bin/org/eclipse/ptp/debug/external/debugger org.eclipse.ptp.debug.external.debugger.ParallelDebugger
	
install: all
	cp *.jnilib $(OUTPUT_DIR)

clean:
	rm -f *.jnilib *.o
