Quandl4OpenGamma
================

Quandl4OpenGamma is a library to allow [Quandl](http://www.quandl.com) data to be imported and maintained in an OpenGamma
environment.

# Table of Contents
 - [Quick start](#quick-start)
 - [Design Principles](#design-principles)
 - [Tutorial](#tutorial)
 - [Documentation](#documentation)
 - [Roadmap](#roadmap)
 - [Contributing](#contributing)
 - [Community](#community)
 - [Versioning](#versioning)
 - [Bugs and feature requests](#bugs-and-feature-requests)
 - [Creator](#creator)
 - [Copyright and license](#copyright-and-license)

### Quick Start
The minimum pre-requisites are:
 - OpenJDK 7, Oracle JDK 7 & 8 are tested.
 - Maven 3.

Three options are available:
 - ~~[Download the latest release](https://github.com/McLeodMoores/quandl4opengamma/something)
 - Clone the repository: `git clone https://github.com/McLeodMoores/quandl4opengamma.git`
   - Run `mvn install` to build the libray and install to your local Maven repository.
   - Run `mvn javadoc:javadoc` to build the documentation.
 - ~~Add the following fragment to your Maven POM file~~ **COMING SOON**
```xml
<dependency>
  <groupId>com.mcleodmoores</groupId>
  <artifactId>quandl4opengamma</artifactId>
  <version>0.1</version>
</dependency>
```
### Design Principles
 - Thorough unit and integration test support.
 - Publish maven artifacts on [Maven Central](http://search.maven.org/).
 - Provide concrete examples.
 - Provide comprehensive documentation and JavaDocs.

## Tutorial
### A First Taste of the API
### Documentation
An addition to the tutorial, there is extra documentation at the package and class level within the [JavaDocs, which are hosted in GitHub Pages](http://mcleodmoores.github.io/quandl4opengamma/apidocs).

### Roadmap
Some future plans for incorporation include:

### Contrubutions
Contributions are welcome!  Please read through the [contributing guidelines](http://github.com/McLeodMoores/quandl4opengamma/blob/master/CONTRIBUTING.md).  This gives guidelines on opening issues, coding standards and testing requirements.

### Community
Follow development here via
 - Twitter (@jim_moores)
 - Email (jim@mcleodmoores.com)

### Versioning
Releases will be numbered with the format `<major>.<minor>.<patch-level>`.  When to bump a version number will be dictated by:
 - Breaking backwards API compatibility will mean a bump in the major version number.
 - New features that retain backwards compatibility will require a minor version number bump.
 - Pure bug fixes will bump the patch-level.
 
### Creators
**McLeod Moores Software Limited**
 - <http://www.mcleodmoores.com>

**Jim Moores**
 - <http://twitter.com/jim_moores>
 - <http://github.com/jimmoores>
 - <https://www.linkedin.com/pub/jim-moores/0/442/841>
 
**Elaine McLeod**
 - <http://github.com/emcleod>
 - <https://www.linkedin.com/profile/view?id=70356218>

### Copyright and license

Code and documentation Copyright (C) 2014 McLeod Moores Software Limited.  Code and documentation released under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
 

