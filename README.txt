

Built as a completely open architecture, the OpenGamma Platform is designed so
that every component can be individually used, or individually replaced, based
on customer requirements. We don't believe in forklift upgrades, and we built
the OpenGamma Platform so that they're never necessary: individual projects can
use OpenGamma components when they provide a clear advantage, and later migrate
additional portions of their infrastructure if and when time and resources
permit.

Visit the developer website at http://developers.opengamma.com for more
information, downloads, docs and more.


Installing and building OpenGamma
---------------------------------
Firstly you need to make sure Apache Maven and Git are installed and working.
Version 3.0.4 or later of Maven is required.

Obtaining the source code
-------------------------

The OpenGamma Platform is open source software using the Apache License v2.
The [company](http://www.opengamma.com/) behind OpenGamma also offers support
and some additional commercial components (The commercial components typically
have dependencies with restrictive licensing incompatible with open source.)
This README only refers to the open source components.

The source code can be cloned using git from GitHub:

  git clone https://github.com/OpenGamma/OG-Platform.git

A source tarball can also be downloaded from http://developers.opengamma.com.

Building
--------
The source code must be compiled before use. This will build multiple jar
files and install them into your local Maven repository.
Simply run this command from the root directory of the source code:

  mvn install

The command above will run unit tests.
These can be skipped to save time if desired:

  mvn install -DskipTests



Running the OpenGamma engine
----------------------------
The primary program in the OpenGamma platform is known as the "engine".
For production, the engine is typically customized, however two example engine
configurations are pre-supplied, one with Bloomberg support and one with
simulated market data.

To run the example engine, change to the examples/examples-simulated directory
and run the following commands:

  cd examples/examples-simulated
  mvn opengamma:server-init -Dconfig=fullstack
  mvn opengamma:server-run -Dconfig=fullstack

Wait for the components to load and then point your browser at
`http://localhost:8080` to see the web user interface.  
Go to `http://localhost:8080/jax/components` to get a sense of
the underlying power of the system, available via REST.
Note that the "server-init" command only needs to be run once.

___

Eclipse
-------
Importing the projects into Eclipse requires following a very specific set of
instructions to work properly.
Full details are in the README of the eclipse subdirectory.


More information
----------------
For more information go to http://developers.opengamma.com
