#!/usr/bin/env python
#
# Copyright (c) 2005 The Regents of the University of California. 
# This material was produced under U.S. Government contract W-7405-ENG-36 
# for Los Alamos National Laboratory, which is operated by the University 
# of California for the U.S. Department of Energy. The U.S. Government has 
# rights to use, reproduce, and distribute this software. NEITHER THE 
# GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
# ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
# to produce derivative works, such modified software should be clearly marked, 
# so as not to confuse it with the version available from LANL. LA-CC 04-115
# 
# Additionally, this program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# 
#

from time import ctime
__author__ = "Greg Watson"
__date__ = ctime()
__version__ = "1.0"
__credits__ = ""

import os, sys, socket, binascii

PROXY_EV_OK		        	= 0
PROXY_EV_ERROR			    = 1

RTEV_BASE		        	= 200

RTEV_OK			        	= RTEV_BASE
RTEV_ERROR			        = RTEV_BASE + 1
RTEV_JOBSTATE		    	= RTEV_BASE + 2
RTEV_PROCS		        	= RTEV_BASE + 4
RTEV_PATTR		        	= RTEV_BASE + 5
RTEV_NODES			        = RTEV_BASE + 7
RTEV_NATTR			        = RTEV_BASE + 8
RTEV_NEWJOB			        = RTEV_BASE + 12
RTEV_PROCOUT			    = RTEV_BASE + 13
RTEV_NODECHANGE			    = RTEV_BASE + 14

RTEV_ERROR_BASE			    = RTEV_BASE + 1000
RTEV_ERROR_RUN			    = RTEV_ERROR_BASE + 2
RTEV_ERROR_TERMINATE_JOB	= RTEV_ERROR_BASE + 3
RTEV_ERROR_PATTR		    = RTEV_ERROR_BASE + 4
RTEV_ERROR_PROCS		    = RTEV_ERROR_BASE + 5
RTEV_ERROR_NODES		    = RTEV_ERROR_BASE + 6
RTEV_ERROR_NATTR		    = RTEV_ERROR_BASE + 7
RTEV_ERROR_SIGNAL		    = RTEV_ERROR_BASE + 9

global ptpCmds

class PTPProxy:
    def __init__(self, host, port, cmds, debug=0):
        self.sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        self.host = host
        self.port = port
        self.cmds = cmds
        self.debug = debug
    def getsocket(self):
        return self.sock
    def close(self):
        self.sock.close()
    def connect(self):
        self.sock.connect((self.host, self.port))
    def read_commands(self,*args):
        if len(args) > 0:
            sock = args[0]
        else:
            sock = self.sock
        cmd_str = self.read_cmd(sock)
        if cmd_str == '':      
            if self.cmds.has_key('EXIT'):
        	self.cmds['EXIT']([])
            return
        cmd = cmd_str.rstrip(' \n').split(' ')
        pos = 1
        args = []
        while pos < len(cmd):
            args.append(from_ptp_string(cmd[pos]))
            pos += 1
        if self.cmds.has_key(cmd[0]):
            res = []
            func = self.cmds[cmd[0]]
            res = func(args)
            if res[0] < RTEV_ERROR_BASE: # success
                self.send_ok_event(res[0], res[1], sock)
            else:
                self.send_err_event(res[0], res[1], sock)
    def read_cmd(self, *args):
        if len(args) > 0:
            sock = args[0]
        else:
            sock = self.sock
        hdr = sock.recv(9)
        if hdr == '':
            return ''
        self.debug_print('received hdr <%s>' % hdr)
        msg_len = int(hdr.rstrip(' '), 16)
        msg = ''
        while msg_len > 0:
            try:
                c = sock.recv(1)
            except socket.error, errinfo:
                if errinfo[0] == EINTR:
                    return msg
                elif errinfo[0] == ECONNRESET:
                    return msg
                else:
                    return ''
            except Exception:
                c = ''
                msg = ''
                break
            msg += c
            msg_len -= 1
        self.debug_print('received msg <%s>' % msg)
        return msg
    def send_msg(self, str, *args):
        if len(args) > 0:
            sock = args[0]
        else:
            sock = self.sock
        msg = '%s %s' % (hex(len(str))[2:].zfill(8),str)
        self.debug_print('sending msg <%s>' % msg)
        sock.sendall(msg)
    def send_event(self, code, res, *args):
        if len(args) > 0:
            sock = args[0]
        else:
            sock = self.sock
        msg = '%d %s' % (code,res)
        self.send_msg(msg, sock)
    def send_ok_event(self, code, res, *args):
        if len(args) > 0:
            sock = args[0]
        else:
            sock = self.sock
        ret_str = '%d' % code
        for v in res:
            if isinstance(v, str):
                ret_str += ' ' + to_ptp_string(v)
            else:
                ret_str += ' ' + str(v)
        self.debug_print('sending event %s' % ret_str)
        self.send_event(PROXY_EV_OK, ret_str, sock)
    def send_err_event(self, code, res, *args):
        if len(args) > 0:
            sock = args[0]
        else:
            sock = self.sock
        self.send_event(PROXY_EV_OK, '%d %d %s' % \
        		(RTEV_ERROR, code, to_ptp_string(res)), sock)
    def debug_print(self, str):
        if self.debug:
            print str

def to_ptp_string(s):
    if s == '':
        return '1:00'
    res = hex(len(s)+1)[2:] + ':'
    return res + binascii.b2a_hex(s) + '00'

def from_ptp_string(s):
    val = s.split(':')
    if val[0] == '1':
        return ''
    return binascii.a2b_hex(val[1][0:-2])

def ptp_print(str):
    sys.stdout.write(str + '\n')
    sys.stdout.flush()