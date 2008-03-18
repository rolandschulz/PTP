# Copyright (c) 2008 IBM Corporation.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

bin_PROGRAMS =			ptp_ibmpe_proxy ptp_ibmpe_miniproxy

ptp_ibmpe_proxy_SOURCES =		src/ptp_ibmpe_proxy.c
ptp_ibmpe_proxy_CFLAGS =		-g -I$(PROXY)/include -I$(UTILS)/include -I/opt/ibmll/LoadL/full/include/ -I/usr/lpp/LoadL/full/include/ -I$(PROXY)/include -I$(UTILS)/include $(AM_CFLAGS) 
ptp_ibmpe_proxy_LDFLAGS = -g
ptp_ibmpe_proxy_LDADD =		-ldl -lpthread 

ptp_ibmpe_miniproxy_SOURCES =		src/ptp_ibmpe_miniproxy.c
ptp_ibmpe_miniproxy_CFLAGS =		-g 
ptp_ibmpe_miniproxy_LDFLAGS = -g
ptp_ibmpe_miniproxy_LDADD =		-lpthread 
