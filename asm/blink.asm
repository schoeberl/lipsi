#
# A blinking and counting LED as hello world
#

ldi 0x00
st r1
outer:
ld r1
addi 0x01
st r1
io 0x0

ldi 0x7f
st r3
loop3:
ldi 0xff
st r2
loop2:
ldi 0xff
loop:
subi 0x01
brnz loop
ld r2
subi 0x01
st r2
brnz loop2
ld r3
subi 0x01
st r3
brnz loop3

br outer
