md-mapper
=========

This is a Java implementation of a semantic mapper for XML source
files, intended in particular for conversion of metadata records
harvested using the OAI-PMH protocol.


# Building

This is a Maven project, and therefore requires Apache Maven. It can
be built simply using the command:

  mvn clean package assembly:assembly

Running the above creates a package named
'converter-0.1.1024-deploy.tar.gz' (subject to changing version number
of course). See below for installation details.

If you use a Java IDE, it is highly likely it offers a simple way to
do the above. Note that this project requires Java Development Kit
version 1.7 or above to build.

## Troubleshooting

If you experience problems with the build process:

  - check you have JDK 1.7 or above

  - check you have defined the environment variable JAVA_HOME


# Configuration

Configuration parameters for the converter are set in a configuration
file and can also be given on the command line. (In case of overlap,
command line parameters override settings from the configuration
file.)

The command line argument 'config=xyz' can be used to set the
configuration file; otherwise the default file config.xml will be
used. See the default file for a list of all available configuration
parameters.


# Implementation Details

The reason JDK 1.7 is needed is due to the use of
java.nio.file.FileVisitor to traverse source files. This is necessary
because of the extremely poor performance of the file handling classes
in the java.io package in the situation where a directory contains a
large number of files (on the order of hundreds of thousands).

Saxon is used as the XPath engine, but only via standard APIs.


# License

GPL v3; see file LICENSE for full text of the license.
