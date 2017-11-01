# Lipsi: Probably the Smallest Processor in the World

This repository contains the source code of Lipsi and supports following paper,
submitted to ARCS 2018:

*Martin Schoeberl, Lipsi: Probably the Smallest Processor in the World, under submission*

While research on high-performance processors is important, it is also interesting to explore processor architectures at the other end of the spectrum: tiny processor cores for auxiliary duties. While it is common to implement small circuits for auxiliary duties, such as a serial port, in dedicated hardware, usually as a state machine or a combination of communicating state machines, these functionalities may also be implemented by a small processor. In this paper we present Lipsi a very tiny processor to enable implementing classic finite state machine logic in software without overhead. We evaluate that processor in two ways: (1) we show an implementation of a serial port completely in software; (2) as Lipsi is so small we explore a massive parallel multicore processor consisting of more than 100 Lipsi cores in a low-cost FPGA.

Lipsi is probably the smallest processor around ((c) Carlsberg). Do a massive parallel Lipsi core with simple point-to-point connections between cores. Look also into Christian Hochberger's small core and cite

## Getting Started

You need `sbt` and a `JVM` installed. Scala and Chisel are downloaded when
first used.

A plain
```bash
make
```
runs the default program as a test.

```
make hw
```

generates Verilog code, which can be synthesized. The project contains
a Quartus project in folder `quartus`.

All test cases are run with:

```
make test
```


As usual, have fun,
Martin
