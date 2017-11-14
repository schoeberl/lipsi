# 
# branch test program
#

ldi 0x73
br dest
ldi 0x34
dest:
subi 0x73
st r1
brz dozbr
ldi 0x11
dozbr:
add r1
st r1
ldi 0x23
brz dontzbr
subi 0x23
dontzbr:
add r1
st r1
ldi 0x55
brnz donzbr
addi 0x11
donzbr:
subi 0x55
add r1
st r1
brnz dontnzbr
ldi 0x88
dontnzbr:
subi 0x88
add r1
exit
