# Makex

Note: this project is not actively maintained

Makefile Extraction (Makex) analyzes KBuild files and extracts the conditions under which each C files get compiled in linux. The constraints are presented as propositional formula.

## How to run

The latest compiled version of Makex can 

To run Makex, run the following command from within a Linux tree:

```
java -classpath LinuxMakeFileParser.jar -Xms1024M -Xmx2048M makefiles.Makex
```


The constraints in Kbuild will be outputted in a "models" directory, with one .makemodel file for each Linux architecture present. Files which are compiled unconditionally will have no constraints.

In order to identify tristate features, we rely on the kconfig models extracted by [Undertaker](http://vamos.informatik.uni-erlangen.de/trac/undertaker). We basically need the list of features (i.e., which also have a _MODULE variation) defined as tristate in the Kconfig files. A list of such features for releases 2.6.34-3.6 is available [here](module-items.zip). Add the modules.txt file to your work directory before running Makex. You can simply add an empty modules.txt file, but no _MODULE variation will be generated in the make constraints which may lead to false positives.

You can use [this](undertakerAnalysis) script as an example of how to run the analysis to detect dead and undead blocks with and without the make constraints

## Related Paper

Nadi, Sarah, and Ric Holt. "[The Linux kernel: A case study of build system variability.](https://onlinelibrary.wiley.com/doi/full/10.1002/smr.1595)" Journal of Software: Evolution and Process 26.8 (2014): 730-746.
