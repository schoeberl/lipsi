# 
# Register ALU operations
#

ldi 0x12
st r1
ldi 0x34
st r2
ldi 0
add r1
add r2
# now it is 0x46
st r15
or r2 # 0x76
st r14
ldi 0xf0
st r13
ld r14 # 0x76
and r13  # 0x70
addi 0x0f # 0x7f
xor r1 # 0x6d
subi 0x6d
add r15
subi 0x46
exit
