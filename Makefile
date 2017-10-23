# Make Lipsi

SBT = sbt

all:
	$(SBT) "test:runMain lipsi.LipsiTester"

alu-test:
	$(SBT) "test:runMain lipsi.AluTester"

