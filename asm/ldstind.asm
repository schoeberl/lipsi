# 
# Test indirect load and store
#

ldi 0x73
st r1
ldi 0x34
stind (r1)
ldi 0xff
ldi 0xff
st r1
ldi 0x73
st r2
ldind (r2)
subi 0x34
exit
