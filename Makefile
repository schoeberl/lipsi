# Make Lipsi

SBT = sbt

# the program
APP ?= asm/test.asm

all:
	$(SBT) "test:runMain lipsi.LipsiTester $(APP)"

hw:
	$(SBT) "runMain lipsi.LipsiMain $(APP)"

wave:
	gtkwave generated/Lipsi.vcd Lipsi.gtkw

# Danger zone, removes all unversioned files
# Including the Eclipse project fiels generated with "sbt ecplipse"
clean:
	git clean -fd
