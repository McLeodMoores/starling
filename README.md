![Starling](https://github.com/McLeodMoores/starling/raw/mcleodmoores/starling-inline.png)

Welcome to the Starling Platform!
---------------------------------
Starling is McLeod Moores' technology platform for market risk and analytics.
It is a fork of the OpenGamma platform (2.2) that has been independently 
developed by us in private.  We are releasing our changes back to the 
community and will now work in public.

Our aims and principles:
 * We won't break APIs unless we're correcting behaviour and when we replace
   APIs we will provide a clear migration path.  We value your participation.
 * We want to make the common use cases much easier to use, and the more 
   complex configurations still possible.
 * We want to promote the use of the Analytics library as a stand-alone 
   component for when existing infrastructure is already in place in your
   application.
 * We want to improve the documentation and invite others to participate.
   
Starling continues support for fully streaming, real-time analytics, while 
simplifying the API particularly for the more common use-cases like on-demand 
calculations using daily snapshots and historical data provided by the user.

Additionally, while preserving the abililty to scale to a multi-node micro-
services style configuration, we have focussed on making simpler, single-node
setups easier to run by separating the micro-services dependencies into 
separate packages.  This yields large savings in memory and extraneous 
dependencies in simple use-cases that make it much easier to embed Starling 
in existing applications.

Installing and building Starling
---------------------------------
Firstly you need to make sure Apache Maven and Git are installed and working.
Version 3.0.4 or later of Maven is required.

### Obtaining the source code

The Starling Platform is open source software using the Apache License v2.
The [company](http://www.mcleodmoores.com/) behind Starling also offers support,
consultancy and some additional commercial components.

The source code can be cloned using git from GitHub:
```
  git clone https://github.com/McLeodMoores/starling.git
```

A source tarball can also be downloaded from 
http://github.com/McleodMoores/starling

### Building

The source code must be compiled before use. This will build multiple jar
files and install them into your local Maven repository.
Simply run this command from the root directory of the source code:
```
  mvn install
```
The command above will run unit tests.
These can be skipped to save time if desired:
```
  mvn install -DskipTests
```


Running the Calculation engine
------------------------------
A core component in Starling is known as the "engine".
For production, the engine is typically customized, however two example engine
configurations are pre-supplied, one with Bloomberg support and one with
simulated market data.

To run the example engine, change to the examples/examples-simulated directory
and run the following commands:
```
  cd examples/examples-simulated
  mvn starling:server-init -Dconfig=fullstack
  mvn starling:server-run -Dconfig=fullstack
```

Note that the "server-init" command only needs to be run once - the server can
be stopped and restarted without having to repopulate the databases.

Wait for the server to start:

![Server startup](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/example-server-startup.png)

and then point your browser at
`http://localhost:8080` to see the web user interface:

![UI landing page](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/example-server-web-ui.png)

Go to `http://localhost:8080/jax/components` to get a sense of
the underlying power of the system, available via REST. Some of the components that are available can be seen below:

![Rest](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/example-server-rest-interface.png)

Clicking on "Analytics" opens a UI that shows calculation results when a view is selected:

![Analytics results](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/docs/images/example-server-analytics-ui.png)

An explanation of what the example views calculate can be found 
[here](https://github.com/McLeodMoores/starling/blob/mcleodmoores/examples/examples-simulated/README.md)

___

Eclipse
-------
Importing the projects into Eclipse requires following a very specific set of
instructions to work properly.
Full details are in the [README of the eclipse subdirectory](https://github.com/McLeodMoores/starling/blob/mcleodmoores/eclipse/README.md).


More information
----------------
For more information go to http://www.mcleodmoores.com
