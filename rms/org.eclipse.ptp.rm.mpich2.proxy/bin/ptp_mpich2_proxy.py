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

"""
usage: ptp_mpd_proxy [--host=hostname --port=port]
"""

from time import ctime
__author__ = "Greg Watson"
__date__ = ctime()
__version__ = "1.0"
__credits__ = ""

import sys, os
import signal, socket, binascii, pwd, grp
import imp

#
# Try and locate MPICH2 installation
#
try:
    fp, path, desc = imp.find_module('mpdlib', os.environ['PATH'].split(':'))
    imp.load_module('mpdlib', fp, path, desc)
except ImportError:
    print 'Could not locate MPICH2 installation. Please check your PATH.'
    sys.exit(1)

from  sets     import  Set
from  re       import  sub
from  urllib   import  unquote
from  mpdlib   import  mpd_set_my_id, mpd_uncaught_except_tb, mpd_print, \
                       mpd_handle_signal, mpd_get_my_username, mpd_version, \
                       MPDConClientSock, MPDParmDB, MPDListenSock, \
                       MPDStreamHandler, mpd_get_my_username, \
                       mpd_get_groups_for_username
from  ptplib   import  PTPProxy, to_ptp_string, from_ptp_string, \
                       RTEV_OK, RTEV_NEWJOB, RTEV_NODES, RTEV_NATTR, \
                       RTEV_PATTR, RTEV_JOBSTATE, RTEV_PROCOUT, \
                       RTEV_ERROR_PROCS, RTEV_ERROR_PATTR, \
                       RTEV_ERROR_NODES, RTEV_ERROR_NATTR, \
                       RTEV_ERROR_RUN, RTEV_ERROR_TERMINATE_JOB

global exit, parmdb, myHost, myIP, cliMode, runningJobs, jobIDMap
global sigOccurred, streamHandler, manSocks, ptpProxy, extraEvent, debug

recvTimeout = 20  # const
runningJobs = {}
jobIDMap = []
extraEvent = ()
ptpProxy = None
debug = 0

def ptp_mpd_proxy():
    global exit, parmdb, myHost, myIP, streamHandler
    global manSocks, runningJobs, ptpProxy, extraEvent, debug

    sys.excepthook = mpd_uncaught_except_tb

    ptpHost = ''
    ptpPort = 0
    sigOccurred = 0
    manSocks = []
    exit = 0
    debug = 0

    ptpProxyCmds = {
	'QUI'		: finish,
	'STARTDAEMON'	: startdaemon,
	'RUN'	 	: run,
	'TERMJOB'	: kill,
	'GETNODES'	: getnodes,
	'GETNATTR'	: getnodeattr,
	'GETPROCS'	: listprocs,
	'GETPATTR'	: procattrs,
    }

    if len(sys.argv) > 1:
        if (sys.argv[1] == '-h' or sys.argv[1] == '--help'):
            usage()
            
    arg = 1
    while arg < len(sys.argv):
        args = sys.argv[arg].split('=')
        if args[0] == '--host':
        	if len(args) < 2:
        	    usage()
        	ptpHost = args[1]
        elif args[0] == '--port':
        	if len(args) < 2 or not args[1].isdigit():
        	    usage()
        	ptpPort = int(args[1])
        elif args[0] == '--debug':
            debug = 1
        else:
        	usage()
        arg += 1

	if (ptpHost == '' or ptpPort == 0):
	    usage()

    streamHandler = MPDStreamHandler()

    #
    # Connect to PTP
    #
    if ptpHost != '':
        ptpProxy = PTPProxy(ptpHost, ptpPort, ptpProxyCmds, debug=debug)
    	try:
    	    ptpProxy.connect()
    	except:
    	    print 'ptp_mpd_proxy: could not connect to %s:%d' % (ptpHost,ptpPort)
    	    sys.exit(-1)
    	streamHandler.set_handler(ptpProxy.getsocket(),ptpProxy.read_commands,args=())

    #
    # Set up MPD stuff
    #
    if hasattr(signal,'SIGINT'):
        signal.signal(signal.SIGINT, sig_handler)
    if hasattr(signal,'SIGALRM'):
        signal.signal(signal.SIGALRM,sig_handler)

    mpd_set_my_id(myid='ptp_mpd_proxy')

    myHost = socket.gethostname();
    try:
    	hostinfo = socket.gethostbyname_ex(myHost)
    except:
    	print 'ptp_mpd_proxy failed: gethostbyname_ex failed for %s' % (myHost)
    	sys.exit(-1)
    myIP = hostinfo[2][0]

    parmdb = MPDParmDB(orderedSources=['cmdline','xml','env','rcfile','thispgm'])
    parmsToOverride = {
                        'MPD_USE_ROOT_MPD'            :  0,
                        'MPD_SECRETWORD'              :  '',
                      }
    for (k,v) in parmsToOverride.items():
        parmdb[('thispgm',k)] = v
    parmdb.get_parms_from_env(parmsToOverride)
    parmdb.get_parms_from_rcfile(parmsToOverride)

    parmdb[('thispgm','mship')] = ''
    parmdb[('thispgm','rship')] = ''
    parmdb[('thispgm','userpgm')] = ''
    parmdb[('thispgm','nprocs')] = 0
    parmdb[('thispgm','ecfn_format')] = ''
    parmdb[('thispgm','gdb_attach_jobid')] = ''
    parmdb[('thispgm','singinitpid')] = 0
    parmdb[('thispgm','singinitport')] = 0
    parmdb[('thispgm','ignore_rcfile')] = 0
    parmdb[('thispgm','ignore_environ')] = 0
    parmdb[('thispgm','inXmlFilename')] = ''
    parmdb[('thispgm','print_parmdb_all')] = 0
    parmdb[('thispgm','print_parmdb_def')] = 0

    if ptpHost == '':
    	sys.stdout.write('>>> ')
    	sys.stdout.flush()
    	streamHandler.set_handler(sys.stdin,do_input,args=())

    #
    # Main Loop
    #

    while not exit:
    	if sigOccurred:
    	    for sock in manSocks:
        		handle_sig_occurred(sock)
    	rv = streamHandler.handle_active_streams(timeout=0.1)
    	if rv[0] < 0:  # will handle some sigs at top of next loop
    	    pass       # may have to handle some err conditions here
    	#
    	# Send fake event to handle job state changes
    	#
    	if extraEvent != ():
    	    ptpProxy.send_ok_event(*extraEvent)
    	    extraEvent = ()

def do_input(fd):
    global exit
    line = fd.readline()
    if line == '':
    	exit = 1
    	return
    if line == '\n':
    	sys.stdout.write('>>> ')
    	sys.stdout.flush()
    	return
    line = line.rstrip(' \n').split(' ')
    if line[0] == 'exit' or line[0] == 'quit' or line[0] == 'q':
    	exit = 1
    elif line[0] == 'getnodes':
    	getnodes([])
    elif line[0] == 'getnodeattr':
    	getnodeattr([-1])
    elif line[0] == 'listjobs':
    	listjobs([])
    elif line[0] == 'listprocs':
    	if len(line) < 2:
    	    print 'listprocs <jobid>'
    	else:
    	    listprocs([line[1]])
    elif line[0] == 'procattrs':
    	if len(line) < 2:
    	    print 'procattrs <jobid>'
    	else:
    	    procattrs([line[1]])
    elif line[0] == 'kill':
    	if len(line) < 2:
    	    print 'kill <jobid>'
    	else:
    	    kill([line[1]])
    elif line[0] == 'run':
    	if len(line) < 3:
    	    print 'run <nprocs> <cmd> [<args>]'
    	args = []
    	args.append('execName')
    	args.append(line[2])
    	args.append('numOfProcs')
    	args.append(line[1])
    	args.append('workingDir')
    	args.append(os.path.abspath(os.getcwd()))
    	pos = 3
    	while pos < len(line):
    	    args.append('progArg')
    	    args.append(line[pos])
    	    pos += 1
    	run(args)
    else:
    	print 'Unknown command: ', line[0]
    sys.stdout.write('>>> ')
    sys.stdout.flush()

def open_mpd_console():
    global parmdb
    if (hasattr(os,'getuid')  and  os.getuid() == 0)  or  parmdb['MPD_USE_ROOT_MPD']:
        fullDirName = os.path.abspath(os.path.split(sys.argv[0])[0])  # normalize
        mpdroot = os.path.join(fullDirName,'mpdroot')
        conSock = MPDConClientSock(mpdroot=mpdroot,secretword=parmdb['MPD_SECRETWORD'])
    else:
        conSock = MPDConClientSock(secretword=parmdb['MPD_SECRETWORD'])
    return conSock

def make_jobid(str):
    smjobid = str.split('  ')  # jobnum, mpdid, and alias (if present)
    return smjobid[0] + '@' + smjobid[1]

def getnodes(args):
    conSock = open_mpd_console()
    if conSock == 0:
    	return (RTEV_ERROR_NODES, 'Could not connect to MPD')
    msgToSend = { 'cmd' : 'mpdtrace' }
    conSock.send_dict_msg(msgToSend)
    num_nodes = 0
    done = 0
    while not done:
    	msg = conSock.recv_dict_msg(timeout=5.0)
    	if not msg:    # also get this on ^C
    	    conSock.close()
    	    return (RTEV_ERROR_NODES, 'No message recvd from MPD before timeout')
    	elif not msg.has_key('cmd'):
    	    conSock.close()
            return (RTEV_ERROR_NODES, 'Invalid message: %s:' % (msg))
    	if msg['cmd'] == 'mpdtrace_info':
    	    num_nodes += 1
    	elif msg['cmd'] == 'mpdtrace_trailer':
    	    done = 1
    conSock.close()
    return (RTEV_NODES, [num_nodes])

def getnodeattr(args):
    if len(args) < 2 or args[1] != '-1':
    	return (RTEV_ERROR_NATTR, 'Invalid arguments')
    if not 'ATTRIB_NODE_NAME' in args[2:]:
    	return (RTEV_NATTR, [])
    conSock = open_mpd_console()
    if conSock == 0:
    	return (RTEV_ERROR_NATTR, 'Could not connect to MPD')
    msgToSend = { 'cmd' : 'mpdtrace' }
    conSock.send_dict_msg(msgToSend)
    node_attr = []
    done = 0
    while not done:
    	msg = conSock.recv_dict_msg(timeout=5.0)
    	if not msg:    # also get this on ^C
    	    conSock.close()
    	    return (RTEV_ERROR_NATTR, 'No message recvd from MPD before timeout')
    	elif not msg.has_key('cmd'):
    	    conSock.close()
            return (RTEV_ERROR_NATTR, 'Invalid message: %s:' % (msg))
    	if msg['cmd'] == 'mpdtrace_info':
    	    pos = msg['id'].rfind('_')
    	    for attr in args[2:]:
        		if attr == 'ATTRIB_NODE_NAME':
        		    node_attr.append(str(msg['id'][:pos]))  # strip off port
        		elif attr == 'ATTRIB_NODE_USER':
        		    node_attr.append(mpd_get_my_username())
        		elif attr == 'ATTRIB_NODE_GROUP':
        		    group = pwd.getpwnam(mpd_get_my_username())[3]
        		    group_name = grp.getgrgid(group)[0]
        		    node_attr.append(group_name)
        		elif attr == 'ATTRIB_NODE_STATE':
        		    node_attr.append('up')
        		elif attr == 'ATTRIB_NODE_MODE':
        		    node_attr.append('72')
    	elif msg['cmd'] == 'mpdtrace_trailer':
    	    done = 1
    conSock.close()
    return (RTEV_NATTR, node_attr)

def finish(args):
    global exit
    exit = 1
    return (RTEV_OK, [])

def startdaemon(args):
    return (RTEV_OK, [])

def listjobs(args):
    conSock = open_mpd_console()
    if conSock == 0:
    	return (0, 'Could not connect to MPD')
    msgToSend = { 'cmd' : 'mpdlistjobs' }
    conSock.send_dict_msg(msgToSend)
    msg = conSock.recv_dict_msg(timeout=5.0)
    if not msg:
    	conSock.close()
    	return (0, 'No message recvd from MPD before timeout')
    if msg['cmd'] != 'local_mpdid':     # get full id of local mpd for filters later
    	conSock.close()
    	return (0, 'Did not recv local_mpdid msg from local mpd; instead, recvd: %s' % msg)
    jobids = Set()
    done = 0
    while not done:
        msg = conSock.recv_dict_msg()
        if not msg.has_key('cmd'):
    	    conSock.close()
            return (0, 'Invalid message: %s:' % (msg))
        if msg['cmd'] == 'mpdlistjobs_info':
    	    jobids.add(make_jobid(msg['jobid']))
        else:  # mpdlistjobs_trailer
            done = 1
    conSock.close()
    print jobids
    return (1, jobids)

def listprocs(args):
    if len(args) != 1:
    	return (RTEV_ERROR_PROCS, 'Invalid arguments')
    jid = int(args[0])
    if jid >= len(jobIDMap):
    	return (RTEV_ERROR_PROCS, 'No such job')
    jobid = jobIDMap(jid)
    conSock = open_mpd_console()
    if conSock == 0:
    	return (RTEV_ERROR_PROCS, 'Could not connect to MPD')
    msgToSend = { 'cmd' : 'mpdlistjobs' }
    conSock.send_dict_msg(msgToSend)
    msg = conSock.recv_dict_msg(timeout=5.0)
    if not msg:
    	conSock.close()
    	return (RTEV_ERROR_PROCS, 'No message recvd from MPD before timeout')
    if msg['cmd'] != 'local_mpdid':     # get full id of local mpd for filters later
    	conSock.close()
    	return (RTEV_ERROR_PROCS, 'Did not recv local_mpdid msg from local mpd; instead, recvd: %s' % msg)
    procs = []
    done = 0
    while not done:
        msg = conSock.recv_dict_msg()
        if not msg.has_key('cmd'):
    	    conSock.close()
            return (RTEV_ERROR_PROCS, 'Invalid message: %s:' % (msg))
        if msg['cmd'] == 'mpdlistjobs_info':
    	    if make_jobid(msg['jobid']) == jobid:
        		procs.append(str(msg['rank']))
        else:  # mpdlistjobs_trailer
            done = 1
    conSock.close()
    print procs
    return (RTEV_PROCS, procs)

def procattrs(args):
    global jobIDMap, extraEvent
    if len(args) < 2 or args[1] != '-1':
    	return (RTEV_ERROR_NATTR, 'Invalid arguments')
    jid = int(args[0])
    if jid >= len(jobIDMap):
        return (RTEV_ERROR_PROCS, 'No such job')
    jobid = jobIDMap[jid]
    conSock = open_mpd_console()
    if conSock == 0:
    	return (RTEV_ERROR_PATTR, 'Could not connect to MPD')
    msgToSend = { 'cmd' : 'mpdlistjobs' }
    conSock.send_dict_msg(msgToSend)
    msg = conSock.recv_dict_msg(timeout=5.0)
    if not msg:
    	conSock.close()
    	return (RTEV_ERROR_PATTR, 'No message recvd from MPD before timeout')
    if msg['cmd'] != 'local_mpdid':     # get full id of local mpd for filters later
    	conSock.close()
    	return (RTEV_ERROR_PATTR, 'Did not recv local_mpdid msg from local mpd; instead, recvd: %s' % msg)
    pattr = []
    done = 0
    while not done:
	msg = conSock.recv_dict_msg()
        if not msg.has_key('cmd'):
    	    conSock.close()
            return (RTEV_ERROR_PATTR, 'Invalid message: %s:' % (msg))
        if msg['cmd'] == 'mpdlistjobs_info':
    	    pid = msg['rank']
    	    if make_jobid(msg['jobid']) == jobid:
        		attrs = []
        		for v in args[2:]:
        		    if v == 'ATTRIB_PROCESS_PID':
            			attrs.append(str(msg['clipid']))
        		    elif v == 'ATTRIB_PROCESS_NODE_NAME':
            			attrs.append(str(msg['host']))
        		pattr.insert(pid, attrs)
        else:  # mpdlistjobs_trailer
            done = 1
    conSock.close()
    #
    # Flatten out the pattr list
    #
    attrs = []
    for v1 in pattr:
    	for v2 in v1:
    	    attrs.append(v2)
    extraEvent = (RTEV_JOBSTATE, [jid, 4])
    return (RTEV_PATTR, attrs)

def kill(args):
    global jobIDMap, extraEvent
    if len(args) != 1:
    	return (RTEV_ERROR_PROCS, 'Invalid arguments')
    jid = int(args[0])
    if jid >= len(jobIDMap):
    	return (RTEV_ERROR_PROCS, 'No such job')
    jobid = jobIDMap[jid]
    mpdid = ''
    sjobid = jobid.split('@')
    jobnum = sjobid[0]
    if len(sjobid) > 1:
    	mpdid = sjobid[1]

    conSock = open_mpd_console()
    if conSock == 0:
    	return (RTEV_ERROR_TERMINATE_JOB, 'Could not connect to MPD')
    msgToSend = { 'cmd':'mpdkilljob', 'jobnum' : jobnum, 'mpdid' : mpdid,
                  'jobalias' : '', 'username' : mpd_get_my_username() }
    conSock.send_dict_msg(msgToSend)
    msg = conSock.recv_dict_msg(timeout=5.0)
    if not msg:
    	conSock.close()
    	return (RTEV_ERROR_TERMINATE_JOB, 'No message recvd from MPD before timeout')
    if msg['cmd'] != 'mpdkilljob_ack':
        if msg['cmd'] == 'already_have_a_console':
    	    err_msg = 'Someone already connected to the MPD console'
        else:
            err_msg = 'Unexpected message from mpd: %s' % (msg)
    	conSock.close()
        return (RTEV_ERROR_TERMINATE_JOB, err_msg)
    conSock.close()
    if not msg['handled']:
        return (RTEV_ERROR_TERMINATE_JOB, 'job not found')
    extraEvent = (RTEV_JOBSTATE, [jid, 9])
    return (RTEV_OK, [])

def run(args):
    global parmdb, sigOccurred, streamHandler, manSocks
    global tmpJobID, runningJobs, jobIDMap, ringInfo
    currumask = os.umask(0); os.umask(currumask)

    pgm_name = ''
    pgm_args = []
    pgm_env = {}
    exec_path = ''
    nprocs = 0
    cwd = ''
    debug_exec_path = ''
    debug_args = []

    pos = 0
    while pos < len(args):
    	if args[pos] == 'execName':
    	    pgm_name = args[pos+1]
    	elif args[pos] == 'pathToExec':
    	    exec_path = args[pos+1]
    	elif args[pos] == 'numOfProcs':
    	    nprocs = int(args[pos+1])
    	elif args[pos] == 'procsPerNode':
    	    pass
    	elif args[pos] == 'firstNodeNum':
    	    pass
    	elif args[pos] == 'workingDir':
    	    cwd = args[pos+1]
    	elif args[pos] == 'progArg':
    	    pgm_args.append(args[pos+1])
    	elif args[pos] == 'progEnv':
    	    env = args[pos+1].split('=')
    	    if len(env) == 2:
        		pgm_env[env[0]] = env[1]
    	elif args[pos] == 'debuggerPath':
    	    debug_exec_path = args[pos+1]
    	elif args[pos] == 'debuggerArg':
    	    debug_args.append(args[pos+1])
    	else:
    	    return (RTEV_ERROR_RUN, 'Invalid argument: %s' % args[pos])
    	pos += 2

    if pgm_name == '':
    	return (RTEV_ERROR_RUN, 'Must specify a program name')

    if nprocs <= 0:
    	return (RTEV_ERROR_RUN, 'Invalid number of processes')

    if cwd == '':
    	return (RTEV_ERROR_RUN, 'Must specify a working directory')

    conSock = open_mpd_console()
    if conSock == 0:
    	return (RTEV_ERROR_RUN, 'Could not connect to MPD')

    listenSock = MPDListenSock('',0,name='socket_to_listen_for_man')
    listenPort = listenSock.getsockname()[1]

    parmdb[('thispgm','nprocs')] = nprocs

    msgToMPD = { 'cmd'            : 'mpdrun',
                 'conhost'        : myHost,
                 'conip'	      : myIP,
                 'spawned'        : 0,
                 'nstarted'       : 0,
                 'hosts'          : {},
                 'execs'          : {},
                 'users'          : {},
                 'cwds'           : {},
                 'umasks'         : {},
                 'paths'          : {},
                 'args'           : {},
                 'limits'         : {},
                 'envvars'        : {},
               }

    msgToMPD['nprocs'] = nprocs
    msgToMPD['conport'] = listenPort
    msgToMPD['limits'][(0,nprocs-1)]  = {}
    msgToMPD['users'][(0,nprocs-1)]   = mpd_get_my_username()
    msgToMPD['execs'][(0,nprocs-1)]   = pgm_name
    msgToMPD['args'][(0,nprocs-1)]    = pgm_args
    msgToMPD['paths'][(0,nprocs-1)]   = os.environ['PATH']
    msgToMPD['cwds'][(0,nprocs-1)]    = cwd
    msgToMPD['umasks'][(0,nprocs-1)]  = str(currumask)
    msgToMPD['hosts'][(0,nprocs-1)]   = '_any_'
    msgToMPD['envvars'][(0,nprocs-1)] = pgm_env
    msgToMPD['conifhn'] = ''
    msgToMPD['jobalias'] = ''
    msgToMPD['try_1st_locally'] = 1
    msgToMPD['line_labels'] = '%r'
    msgToMPD['stdin_dest'] = '0'
    msgToMPD['gdb'] = 0
    msgToMPD['totalview'] = 0
    msgToMPD['singinitpid'] = parmdb['singinitpid']
    msgToMPD['singinitport'] = parmdb['singinitport']
    msgToMPD['host_spec_pool'] = []

    msgToSend = { 'cmd' : 'get_mpdrun_values' }
    conSock.send_dict_msg(msgToSend)
    msg = conSock.recv_dict_msg(timeout=recvTimeout)
    if not msg:
        conSock.close()
        listenSock.close()
    	return (RTEV_ERROR_RUN, 'Communication with MPD failed')
    elif msg['cmd'] != 'response_get_mpdrun_values':
        conSock.close()
        listenSock.close()
    	return (RTEV_ERROR_RUN, 'Unexpected msg from MPD :%s:' % (msg))
    if msg['mpd_version'] != mpd_version():
        conSock.close()
        listenSock.close()
    	return (RTEV_ERROR_RUN, 'MPD version %s does not match mpiexec version %s' % \
                      (msg['mpd_version'],mpd_version()))

    # make sure to do this after nprocs has its value
    linesPerRank = {}  # keep this a dict instead of a list
    for i in range(msgToMPD['nprocs']):
        linesPerRank[i] = []

    conSock.send_dict_msg(msgToMPD)
    msg = conSock.recv_dict_msg(timeout=recvTimeout)
    if not msg:
        conSock.close()
        listenSock.close()
    	return (RTEV_ERROR_RUN, 'No msg recvd from MPD when expecting ack of request')
    elif msg['cmd'] == 'mpdrun_ack':
        currRingSize = msg['ringsize']
        currRingNCPUs = msg['ring_ncpus']
    else:
        if msg['cmd'] == 'already_have_a_console':
    	    err_msg = 'Someone already connected to the MPD console'
        elif msg['cmd'] == 'job_failed':
            if  msg['reason'] == 'some_procs_not_started':
                err_msg = 'Unable to start all procs; may have invalid machine names'
            elif  msg['reason'] == 'invalid_username':
                err_msg =  'Invalid username %s at host %s' % \
                      (msg['username'],msg['host'])
            else:
                err_msg = 'Job failed; reason=:%s:' % (msg['reason'])
        else:
            err_msg = 'Unexpected message from mpd: %s' % (msg)
    	conSock.close()
    	listenSock.close()
    	return (RTEV_ERROR_RUN, err_msg)

    conSock.close()

    (manSock,addr) = listenSock.accept()
    if not manSock:
    	listenSock.close()
    	return (RTEV_ERROR_RUN, 'Failed to obtain sock from MPD manager')

    # first, do handshaking with man 
    msg = manSock.recv_dict_msg()  
    if (not msg  or  not msg.has_key('cmd') or msg['cmd'] != 'man_checking_in'):
    	return (RTEV_ERROR_RUN, 'Invalid handshake msg: %s' % (msg) )

    msgToSend = { 'cmd' : 'ringsize', 'ring_ncpus' : currRingNCPUs,
                  'ringsize' : currRingSize }
    manSock.send_dict_msg(msgToSend)
    msg = manSock.recv_dict_msg()
    if (not msg  or  not msg.has_key('cmd')):
    	return (RTEV_ERROR_RUN, 'Invalid reply to ringsize msg: %s' % (msg) )
    if (msg['cmd'] == 'job_started'):
        jobid = make_jobid(msg['jobid'])
    	runningJobs[jobid] = nprocs
    	jobIDMap.append(jobid)
    	debug_print('mpiexec: job %s started' % jobid)
    else:
    	return (RTEV_ERROR_RUN, 'Unknown msg: %s' % (msg) )

    streamHandler.set_handler(manSock,handle_man_input,args=(streamHandler,jobid))

    (manCliStdoutSock,addr) = listenSock.accept(name='stdout_sock')
    streamHandler.set_handler(manCliStdoutSock,
			  handle_cli_stdout_input,
			  args=(streamHandler,jobid))
    (manCliStderrSock,addr) = listenSock.accept(name='stderr_sock')
    streamHandler.set_handler(manCliStderrSock,
			  handle_cli_stderr_input,
			  args=(streamHandler,jobid))

    manSocks.append(manSock)

    return (RTEV_NEWJOB, [jobIDMap.index(jobid)])

def handle_man_input(sock,streamHandler,jobid):
    global manSocks, ptpProxy, jobIDMap
    msg = sock.recv_dict_msg()
    if not msg:
        streamHandler.del_handler(sock)
    elif not msg.has_key('cmd'):
        mpd_print(1,'mpiexec: from man, invalid msg=:%s:' % (msg) )
    elif msg['cmd'] == 'execution_problem':
        print 'rank %d (%s) in job %s failed to find executable %s' % \
              ( msg['rank'], msg['src'], msg['jobid'], msg['exec'] )
        host = msg['src'].split('_')[0]
        reason = unquote(msg['reason'])
        print 'problem with execution of %s  on  %s:  %s ' % \
              (msg['exec'],host,reason)
        # keep going until all man's finish
    	if ptpProxy != None:
    	    ptpProxy.send_ok_event(RTEV_JOBSTATE, [jobIDMap.index(jobid), 9])
    	return
    elif msg['cmd'] == 'job_aborted_early':
        print 'rank %d in job %s caused collective abort of all ranks' % \
              ( msg['rank'], msg['jobid'] )
        status = msg['exit_status']
        if hasattr(os,'WIFSIGNALED')  and  os.WIFSIGNALED(status):
            killed_status = status & 0x007f  # AND off core flag
            print '  exit status of rank %d: killed by signal %d ' % \
                  (msg['rank'],killed_status)
        elif hasattr(os,'WEXITSTATUS'):
            exit_status = os.WEXITSTATUS(status)
            print '  exit status of rank %d: return code %d ' % \
                  (msg['rank'],exit_status)
    	if ptpProxy != None:
    	    ptpProxy.send_ok_event(RTEV_JOBSTATE, [jobIDMap.index(jobid), 8])
    elif msg['cmd'] == 'job_aborted':
        print 'job aborted; reason = %s' % (msg['reason'])
    	if ptpProxy != None:
    	    ptpProxy.send_ok_event(RTEV_JOBSTATE, [jobIDMap.index(jobid), 9])
    elif msg['cmd'] == 'client_exit_status':
        print "exit info: rank=%d  host=%s  pid=%d  status=%d" % \
              (msg['cli_rank'],msg['cli_host'],
               msg['cli_pid'],msg['cli_status'])
        status = msg['cli_status']
        if hasattr(os,'WIFSIGNALED')  and  os.WIFSIGNALED(status):
            killed_status = status & 0x007f  # AND off core flag
            print 'exit status of rank %d: killed by signal %d ' % \
                   (msg['cli_rank'],killed_status)
        elif hasattr(os,'WEXITSTATUS'):
            exit_status = os.WEXITSTATUS(status)
            print 'exit status of rank %d: return code %d ' % \
                  (msg['cli_rank'],exit_status)
    	if ptpProxy != None:
    	    ptpProxy.send_ok_event(RTEV_JOBSTATE, [jobIDMap.index(jobid), 8])
    else:
        print 'unrecognized msg from manager :%s:' % msg

    runningJobs[jobid] -= 1

    if runningJobs[jobid] <= 0:
    	streamHandler.del_handler(sock)
    	manSocks.remove(sock)
    	sock.close()

def handle_cli_stdout_input(sock,streamHandler,jobid):
    global ptpProxy
    msg = sock.recv_one_line()
    if not msg:
    	streamHandler.del_handler(sock)
    else:
    	try:
    	    (rank,rest) = msg.rstrip().split(':',1)
    	    rank = int(rank)
    	except:
    	    rest = msg.rstrip()
    	    rank = 0
    	if ptpProxy != None:
    	    ptpProxy.send_ok_event(RTEV_PROCOUT, [jobIDMap.index(jobid), rank, rest])
    	else:
    	    sys.stdout.write(msg)
    	    sys.stdout.flush()

def handle_cli_stderr_input(sock,streamHandler,jobid):
    msg = sock.recv(1024)
    if not msg:
        streamHandler.del_handler(sock)
    else:
        sys.stderr.write(msg)
        sys.stderr.flush()

def handle_sig_occurred(manSock):
    global sigOccurred, exit
    if sigOccurred == signal.SIGINT:
        if manSock:
            msgToSend = { 'cmd' : 'signal', 'signo' : 'SIGINT' }
            manSock.send_dict_msg(msgToSend)
            manSock.close()
        exit = 1
    elif sigOccurred == signal.SIGALRM:
        if manSock:
            msgToSend = { 'cmd' : 'signal', 'signo' : 'SIGKILL' }
            manSock.send_dict_msg(msgToSend)
            manSock.close()
        mpd_print(1,'job ending due to env var MPIEXEC_TIMEOUT=%s' % \
                  os.environ['MPIEXEC_TIMEOUT'])
        exit = 1

def sig_handler(signum,frame):
    global sigOccurred
    sigOccurred = signum
    mpd_handle_signal(signum,frame)

def debug_print(str):
    global debug
    if debug:
    	print str

def usage():
    print __doc__
    sys.exit(-1)

if __name__ == '__main__':
    ptp_mpd_proxy()
    sys.exit(0)
