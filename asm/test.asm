# Minimal test program
# End simulation with IO access to address 0xf (= exit)
# Simulation succes with accumulator 0
ldi 0x12
addi 0x34
subi 0x12
andi 0xf0
ori 0x03
xori 0xff
st r2
subi 0xcc
exit
