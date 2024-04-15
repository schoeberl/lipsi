# Lipsi: Probably the Smallest Processor in the World

This repository contains the source code of Lipsi and supports following paper:
 
*Martin Schoeberl,
[Lipsi: Probably the Smallest Processor in the World](https://www.jopdesign.com/doc/lipsi.pdf),
Architecture of Computing Systems -- ARCS 2018,
Springer International Publishing, 2018, 18-30*

While research on high-performance processors is important, it is also interesting to explore processor architectures at the other end of the spectrum: tiny processor cores for auxiliary tasks. While it is common to implement small circuits for auxiliary duties, such as a serial port, in dedicated hardware, usually as a state machine or a combination of communicating state machines, these functionalities may also be implemented by a small processor. In this paper we present Lipsi a very tiny processor to enable implementing classic finite state machine logic in software without overhead.

Lipsi is probably the smallest processor around. Possible evaluations of Lipsi: (1) an implementation of a serial port completely in software; (2) as Lipsi is so small we can explore a massive parallel multicore processor consisting of more than 100 Lipsi cores in a low-cost FPGA (with simple point-to-point connections between cores).

Lipsi is written in [Chisel](https://chisel.eecs.berkeley.edu/) and contains:
(1) the hardware description, (2) an assembler, (3) a software simulator, and
(4) testers for individual testing and for co-simulation, all written in
Chisel/Scala and combined in a single program.
Chisel made it possible that the design of all of the above took less than
[14 hours](log.md).

## Tapeout

The Lipsi processor is beeing taped out [with Tiny Tapeout](https://github.com/schoeberl/tt06-lipsi).
See a rendering of the actual [GDS II](https://schoeberl.github.io/tt06-lipsi/) file.

## Getting Started

You need `sbt` and a `JVM` installed. Scala and Chisel are downloaded when
first used.

A plain
```bash
make
```
runs the default program as a test.
The wave form can then be viewed with:
```
make wave
```
The default program can be overwritten with the variable `APP`:
```
make APP=asm/immop.asm
```

Lipsi executing the embedded hello world program, blinking and counting LEDs, can be
generated as follows:
```
make hw APP=asm/blink.asm
```
The project contains a Quartus project in folder `quartus`.

All test cases are run with:

```
make test-all
```
The SW simulator of Lipsi is run with:
```
make sim
```

The co-simulation (for all tests) with the processor description in hardware and
the SW simulator are run with:
```
make test-cosim
```

Folder `asm` contains various assembler program. E.g., `echo.asm` reads the keys from
the FPGA board, adds 1, and puts out the result on the LEDs (on the DE2-115).
Default IO devices are an 8-bit input port connected to the keys and 8-bit output
port connected to the LEDs

To build a 432 cores manycore version of Lipsi, change the value `many` to
`val many = true` in `LipsiTop`. The cores are then connected in a pipeline.
The `echo.asm` program can be used to execute 432 additions and show the result
on the LEDs.

As usual, have fun and feedback is appreciated,

Martin
