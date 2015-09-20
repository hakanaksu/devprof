# devprof

This Application identifies API experience of developers who are involved in a software project. It analyses the API usage using the version history of a software repository like SVN or GIT.

GET STARTED

First you import this application to your IDE. Then you create a derby connection profile and select the ?Derby Embedded JDBC Driver?.
Additionally you have to choose a user name and password and set a local url.
The user name, password and local url must be applied in the persistence.xml file, too.
The current values in the properties part are:
<property name="javax.persistence.jdbc.url" value="jdbc:derby:/Users/hakanaksu/MyDB/"/>
<property name="javax.persistence.jdbc.driver" value="org.apache.derby.jdbc.ClientDriver"/>
<property name="javax.persistence.jdbc.user" value="profiler"/>
<property name="javax.persistence.jdbc.password" value="profiler"/>
The values have to be the chosen ones.

Other database types should be possible, too.

In three main steps you can extract the data of an repository and the APIs to get the API experience of every developer. Every main step has two sub steps.

In an example we apply the analysis on libGDX (https://github.com/libgdx/libgdx).
The metric results are in result/metric and the used APIs of libGDX are in apis/ (09/15).

If you want to reproduce the analysis of libGDX, you can execute org.softlang.devprof.developerprofiler.Main.java

If you want to analyze another repository, you can use the Main.java as template.