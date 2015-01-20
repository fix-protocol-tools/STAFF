# STAFF 
STAFF (Software Testing Automation Framework for [FIX Protocol](http://www.fixtradingcommunity.org/pg/structure/tech-specs/fix-protocol)) 
is a set of tasks, functions and methods, built on top of Apache cross-platform Java-based tool called 
[Ant](http://ant.apache.org/) and developed for automated testing of FIX applications.

STAFF tasks allow performing various operations with FIX sessions and messages, such as:
* create a session with user defined parameters, start and stop it;
* send messages of defined structure to the session
* expect messages from the session, verify message structure, specific tags and/or their values
* convert and save sent/received messages, use them for re-sending
* store values ​​of certain tags, convert and re-use them  (for example, save tag Symbol of NewOrderSingle (D) and use it in ExecutionReport (8))

Each testing script is a user-defined sequence of instructions and commands written into the XML file which are consistently interpreted and executed. Tests are easily combined into suites, each test can be referenced from another one as a library and re-used in different suites.

Along with STAFF all Ant libraries can be used as a powerful tool for operating with files and services, accessing database etc.

##License (See LICENSE file for full license)
Copyright © 2011-2014 EPAM Systems/B2BITS® (http://www.b2bits.com).

STAFF is free software: you can redistribute it and/or modify
it under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl.html) as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

STAFF is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

##Binaries

##Build
