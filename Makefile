# Make Lipsi

SBT = sbt

# the program
APP ?= asm/test.asm

lipsi_test:
	$(SBT) "test:runMain lipsi.LipsiTester $(APP)"

hw:
	$(SBT) "runMain lipsi.LipsiMain $(APP)"

sim:
	$(SBT) "test:runMain lipsi.sim.LipsiSim $(APP)"

cosim:
	$(SBT) "test:runMain lipsi.LipsiCoSim $(APP)"

wave:
	gtkwave generated/Lipsi.vcd Lipsi.gtkw

test:
	make lipsi_test APP=asm/immop.asm
	make lipsi_test APP=asm/aluop.asm
	make lipsi_test APP=asm/ldstind.asm

test-cosim:
	make cosim APP=asm/immop.asm
	make cosim APP=asm/aluop.asm
	make cosim APP=asm/ldstind.asm

# Danger zone, removes all unversioned files
# Including the Eclipse project fiels generated with "sbt ecplipse"
clean:
	git clean -fd
