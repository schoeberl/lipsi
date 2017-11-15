# Make Lipsi

SBT = sbt

# the program
APP ?= asm/test.asm

test:
	$(SBT) "test:runMain lipsi.LipsiTester $(APP)"

hw:
	$(SBT) "runMain lipsi.LipsiMain $(APP)"

sim:
	$(SBT) "test:runMain lipsi.sim.LipsiSim $(APP)"

cosim:
	$(SBT) "test:runMain lipsi.LipsiCoSim $(APP)"

wave:
	gtkwave generated/Lipsi.vcd Lipsi.gtkw

test-all:
	make test APP=asm/immop.asm
	make test APP=asm/aluop.asm
	make test APP=asm/ldstind.asm
	make test APP=asm/branch.asm

test-cosim:
	make cosim APP=asm/immop.asm
	make cosim APP=asm/aluop.asm
	make cosim APP=asm/ldstind.asm
	make cosim APP=asm/branch.asm

# Danger zone, removes all unversioned files
# Including the Eclipse project fiels generated with "sbt ecplipse"
clean:
	git clean -fd
