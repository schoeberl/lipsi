# Make Lipsi

SBT = sbt

# the program
APP ?= asm/test.asm

lipsi_test:
	$(SBT) "test:runMain lipsi.LipsiTester $(APP)"

hw:
	$(SBT) "runMain lipsi.LipsiMain $(APP)"

wave:
	gtkwave generated/Lipsi.vcd Lipsi.gtkw

test:
	make lipsi_test APP=asm/immop.asm
	make lipsi_test APP=asm/aluop.asm

# Danger zone, removes all unversioned files
# Including the Eclipse project fiels generated with "sbt ecplipse"
clean:
	git clean -fd
