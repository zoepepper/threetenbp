
## Scala ThreeTen
JSR-310 provided a new date and time library for Java SE 8.
This project ports the original reference implementation (before it was contributed to OpenJDK) from Java to Scala.

This GitHub repository is a fork of that originally used to create JSR-310.
That repository used the same BSD 3-clause license as this repository.

[![Build Status](https://travis-ci.org/soc/threetenbp.svg?branch=master)](https://travis-ci.org/soc/threetenbp)

#### Building
This project builds using sbt.
Run `sbt test` to run the test suite.

#### Status & Contributing
We are currently working on eliminating the last few test suite failures which were introduced during the conversion from Java to Scala.
Run the test suite or have a look at the [issues](https://github.com/soc/threetenbp/issues) to find something to work on! Contributions and bug reports are welcome!

#### Time-zone data
The time-zone database is stored as a pre-compiled dat file that is included in the built jar.
The version of the time-zone data used is stored within the dat file (near the start).
Updating the time-zone database involves using the `TzdbZoneRulesCompiler` class
and re-compiling the jar file.
Pull requests with later versions of the dat file will be accepted.

#### FAQs

##### Is this project derived from OpenJDK?

No. This project is derived from the Reference Implementation previously hosted on GitHub.
That project had a BSD license, which has been preserved here.
Thus, this project is a fork of the original code before entry to OpenJDK.
