# Make Lipsi

SBT = sbt

all:
	$(SBT) "test:runMain lipsi.LipsiTester"

wave:
	gtkwave generated/Lipsi.vcd Lipsi.gtkw

