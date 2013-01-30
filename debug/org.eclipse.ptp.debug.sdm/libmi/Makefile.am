if !AIX
  noinst_PROGRAMS =			test_mi
endif

noinst_LIBRARIES =			libmi.a

libmi_a_SOURCES =			CLICommand.c \
							CLIOutput.c \
							MIArg.c \
							MIBreakpoint.c \
							MIBreakCommand.c \
							MICommand.c \
							MIError.c \
							MIEvent.c \
							MIExecCommand.c \
							MIFrame.c \
							MIGDBCommand.c \
							MIList.c \
							MIMemory.c \
							MIMemoryCommand.c \
							MIOOBRecord.c \
							MIOutput.c \
							MIParser.c \
							MIPty.c \
							MIResult.c \
							MIResultRecord.c \
							MISession.c \
							MISignalInfo.c \
							MIStackCommand.c \
							MIString.c \
							MIThread.c \
							MIThreadCommand.c \
							MIDisassembly.c \
							MIValue.c \
							MIVar.c \
							MIVarCommand.c
					
libmi_a_CFLAGS =			$(AM_CFLAGS) 

if !AIX
  test_mi_SOURCES =			test_mi.c
  test_mi_CFLAGS =			$(AM_CFLAGS) -g
  test_mi_LDFLAGS = 			-g
  test_mi_LDADD =				libmi.a
endif
