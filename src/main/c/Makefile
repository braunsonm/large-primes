# Braunson Mazoka, bdm642, 11204517

# Some info from http://www.cs.colby.edu/maxwell/courses/tutorials/maketutor/

# Include directory (perspective from the .c file)
IDIR =include
LDIR =lib
CC = gcc
# Flags for the C preprocessor
CPPFLAGS = -L.
# Flags for the C compiler
CFLAGS = -Wall -Wextra -g -std=c11 -pedantic -I${IDIR}
# Extra libraries to load
LIBS=-lm -lgmp
# Flags to pass to the linker
LDFLAGS =
LOADLIBES =
LDLIBS =

# Platform and Arch
PLATFORM = $(shell uname -s)
ARCH = $(shell uname -m)
# Build directories
OBJECTFILES = build/obj/$(PLATFORM)$(ARCH)
BINFILES = build/bin/$(PLATFORM)$(ARCH)
LIBFILES = build/lib/$(PLATFORM)$(ARCH)

## OBJECT DEFINITIONS FOR LINKERS

# Prevent clean getting mixed up with depenencies
.PHONY: clean all package

# TARGETS
ifeq ($(OS),Windows_NT)
    target =
else
    target = calc-target
endif

# MACRO DEFS
ifeq ($(PLATFORM),Linux)
    CCFLAGS += -D LINUX
endif
ifeq ($(PLATFORM),Darwin)
    CCFLAGS += -D OSX
endif
ifeq ($(ARCH),x86_64)
    CCFLAGS += -D AMD64
endif
ifneq ($(filter %86,$(ARCH)),)
    CCFLAGS += -D IA32
endif
ifneq ($(filter arm%,$(ARCH)),)
    CCFLAGS += -D ARM
endif

all: directories $(target)

directories:
	@mkdir -p $(OBJECTFILES)
	@mkdir -p $(BINFILES)
	@mkdir -p $(LIBFILES)

# TAB IS FOR RECIPES ONLY
# SPACES IS FOR ANYTHING ELSE
_MILLER = miller-rabin.o
MILLER = $(patsubst %,$(OBJECTFILES)/%,$(_MILLER))
_VERIFY = verify-prime.o
VERIFY = $(patsubst %,$(OBJECTFILES)/%,$(_VERIFY))
_NEXTPRIME = nextprime.o
NEXTPRIME = $(patsubst %,$(OBJECTFILES)/%,$(_NEXTPRIME))


$(OBJECTFILES)/%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

calc-target: calc verify nextprime


calc: $(MILLER)
	$(CC) -o $@ $^ $(CFLAGS) $(LIBS)
	cp $@ ${BINFILES}/$@

verify: $(VERIFY)
	$(CC) -o $@ $^ $(CFLAGS) $(LIBS)
	cp $@ ${BINFILES}/$@

nextprime: $(NEXTPRIME)
    $(CC) -o $@ $^ $(CFLAGS) $(LIBS)
    cp $@ ${BINFILES}/$@

# Helpers
clean:
	rm -rf build/ core.*
	rm -f calc
	rm -f verify
	rm -f nextprime
