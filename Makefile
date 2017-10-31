# Make Lipsi

SBT = sbt

all:
	$(SBT) "test:runMain lipsi.LipsiTester"

hw:
	$(SBT) "runMain lipsi.LipsiMain"

wave:
	gtkwave generated/Lipsi.vcd Lipsi.gtkw

# Danger zone, removes all unversioned files
# Including the Eclipse project fiels generated with "sbt ecplipse"
clean:
	git clean -fd
