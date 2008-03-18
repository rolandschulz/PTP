# Copyright (c) 2007 IBM Corporation.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html


bin_PROGRAMS =			ptp_ibmll_proxy

ptp_ibmll_proxy_SOURCES =		src/ptp_ibmll_proxy.c
ptp_ibmll_proxy_CFLAGS =		-g -I/opt/ibmll/LoadL/full/include/ -I/usr/lpp/LoadL/full/include/ -I$(PROXY)/include -I$(UTILS)/include $(AM_CFLAGS) 

ptp_ibmll_proxy_LDFLAGS = -g -ldl
ptp_ibmll_proxy_LDADD =		-lpthread 
