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

PREFIX=ompi
ARCH_PREFIX=macosx
VERSION=1.0.0

OMPI_LIB=lib$(PREFIX)-$(ARCH_PREFIX)-$(VERSION).jnilib

#DEBUG = -g
CFLAGS = -c $(DEBUG) -I/System/Library/Frameworks/JavaVM.framework/Headers
LFLAGS = -dynamiclib -single_module -framework JavaVM
OBJECTS = ptp_ompi_jni.o

all: $(OMPI_LIB)

.c.o:
	mpicc $(CFLAGS) $*.c

$(OMPI_LIB): $(OBJECTS)
	mpicc -o $(OMPI_LIB) $(LFLAGS) $(OBJECTS)

install: all
	cp *.jnilib $(OUTPUT_DIR)

clean:
	rm -f *.jnilib *.o
