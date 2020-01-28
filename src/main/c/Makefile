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
    target = calc
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
_PARTA_SERVER_OBJ = miller-rabin.o
PARTA_SERVER_OBJ = $(patsubst %,$(OBJECTFILES)/%,$(_PARTA_SERVER_OBJ))

$(OBJECTFILES)/%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

calc: calc

calc: $(PARTA_SERVER_OBJ)
	$(CC) -o $@ $^ $(CFLAGS) $(LIBS)
	cp $@ ${BINFILES}/$@

# Helpers
package:
	make clean
	tar --exclude ".idea*" -cvzf ${ASSIGNMENT}.tar.gz ./

clean:
	rm -rf build/ core.*
	rm -f calc
