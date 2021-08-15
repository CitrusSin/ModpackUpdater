#  Modpack Updater (WIP)
## Synchronize Minecraft mods between server and client

Are you fed up with players in your server asking you to add some mods?  
In this case if you accept, many players actually don't know how to update their mods list.  
And if you ignore their request, they might not be so happy.

All in all, I have this problem so I make this little program to solve the problem.

### How to use

This program is based on HTTP so you should open a port for this program on your server.  
Default port is 14238.  
#### Server side
For server side, run this command (mods folder under working directory is required):
``java -jar ModpackUpdater-version.jar --server``  
Then it would output logs and left a 
``modupdater_server_config.json``, 
which is the configuration file that you can modify.
#### Client side (WIP), still in imagination yet :)
For client side, just double-click the 
``ModpackUpdater-version.jar`` 
file and it would left a 
``modupdater_client_config.json``  
Then the 
``"updateServerAddress": "http://example.com:14238"``
should be modified to your server address with port set in ModpackUpdater server side.
