include ../makefile.define

ifeq ($(OS), AIX)
    CPPFLAGS = $(MY_FLAGS) -I../libsci
    PROG = scia
    PROG64 = scia64
else
    CPPFLAGS = $(MY_FLAGS) -I../libsci -fPIC
    ifeq ($(ARCH), i686)
        PROG = scia
        PROG64 = 
    else
        PROG = scia
        PROG64 = scia64
    endif
endif

all : $(PROG) $(PROG64)

$(PROG) : $(OBJS) ../common/libcmn.a
	@$(CC32) $(CPPFLAGS) -o $(PROG) $(OBJS) -L../common -lcmn -lrt -ldl

$(PROG64) : $(OBJS64) ../common/libcmn64.a
	@$(CC64) $(CPPFLAGS) -o $(PROG64) $(OBJS64) -L../common -lcmn64 -lrt -ldl

install : $(PROG) $(PROG64)
ifeq ($(OS), AIX)
	cp -p $(PROG) $(BIN_DIR)
	chmod 775 $(BIN_DIR)/$(PROG)
	ln -sf $(BIN_DIR)/$(PROG) /usr/bin/$(PROG)
	cp -p $(PROG64) $(BIN_DIR)
	chmod 775 $(BIN_DIR)/$(PROG64)
	ln -sf $(BIN_DIR)/$(PROG64) /usr/bin/$(PROG64)
else
	cp -p $(PROG) $(BIN_DIR)
	chmod 775 $(BIN_DIR)/$(PROG)
	ln -sf $(BIN_DIR)/$(PROG) /usr/bin/$(PROG)
ifneq ($(ARCH), i686)
	cp -p $(PROG64) $(BIN_DIR)
	chmod 775 $(BIN_DIR)/$(PROG64)
	ln -sf $(BIN_DIR)/$(PROG64) /usr/bin/$(PROG64)
endif
endif

clean :
	rm -f $(PROG) $(PROG64) $(OBJS) $(OBJS64) core *.d

