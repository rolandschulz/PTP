#!/bin/bash

COMMAND="java -cp /data/lnj/RDT/org.eclipse.ptp.rdt.sync.unison.core/bin/ org.eclipse.ptp.rdt.sync.unison.core.FakeSSH"
args=("$@")
for((i=0;i<=${#args[*]};i++))
  do
    COMMAND="${COMMAND} "
    COMMAND="${COMMAND}${args[i]}"
  done
$COMMAND
