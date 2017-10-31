# Make Lipsi

SBT = sbt

all:
	$(SBT) "test:runMain lipsi.LipsiTester"

hw:
	$(SBT) "runMain lipsi.LipsiMain"

wave:
	gtkwave generated/Lipsi.vcd Lipsi.gtkw

