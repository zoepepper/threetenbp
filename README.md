
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
We are currently working on supporting formatting and calculations using a timezone database in JavaScript.

 - The formatting uses a lot of JDK classes, which we might not want to reimplement in Scala.js.
   We might be able to use the new `Intl` Web API.
 - The timezone information is read from a binary blob, which won't work in the browser.
   We will have a look at other projects like moment.js and decide whether we want to use the same format, or come up with our own.

Have a look at the [issues](https://github.com/soc/threetenbp/issues) to find something to work on!
Ideas, suggestions, contributions and bug reports are all welcome!

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
