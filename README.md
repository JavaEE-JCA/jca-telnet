## JCA Telnet Service With Custom Commands

Our Telnet Service is a simple service that opens a port, accepts telnet connections and treats commands typed in the terminal as method invocations. The message listener interface determines which commands are available, the MDB supplies the logic of what these commands do, the Connector takes care of the rest.

A user will connect with a telnet client which will cause our resource adapter to ask the container for an instance of the MDB. We will let the user invoke the MDB as much as he or she likes. When the telnet client exits, the connection is closed and the the MDB is destroyed.

#### build and run :     
**TomEE 8**     
`mvn clean compile install ; (cd ear-module/ ; mvn tomee:run)`

**Liberty/OpenLiberty (Not compatible yet)**     
~~mvn clean compile install ; (cd ear-module/ ; mvn liberty:run)~~