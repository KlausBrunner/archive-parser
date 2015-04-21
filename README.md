archive-parser
==============

Tools to parse a set of EARs (and potentially other zip-based archives) and nicely report which archives are contained where (e.g. which JARs are used in which EARs). The original usecase was to get an overview of which JARs are used in which EARs in a large ecosystem of JEE applications.

The current implementation of the reporting tool is somewhat specific as it assumes certain filename patterns. Most of the code is fairly generic though, and should be easily reusable.

