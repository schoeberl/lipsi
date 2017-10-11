# Make Lipsi

SBT = sbt

alu-test:
	$(SBT) "test:runMain lipsi.AluTester"

