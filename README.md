# Modpack Updater
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/3fea904a0c874f7bb9222fc2eafc04c4)](https://www.codacy.com/gh/Micrafast/ModpackUpdater/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Micrafast/ModpackUpdater&amp;utm_campaign=Badge_Grade)    
Synchronize Minecraft mods between server and client

Are you fed up with players in your server asking you to add some mods?  
In this case if you accept, many players actually don't know how to update their mods list.  
And if you ignore their request, they might not be so happy.

All in all, I have this problem so I make this little program to solve the problem.

## How to use

This program is based on HTTP so you should open a port for this program on your server.  
Default port is 14238.  

### Server side
For server side, run this command (mods folder under working directory is required):
``java -jar ModpackUpdater-version.jar --server``  
Then it would output logs and left a 
``modupdater_server_config.json``, 
which is the configuration file that you can modify.  
#### Redirecting to custom mod sources
If your server don't have a strong network traffic to let your players download mods,
you can redirect the download source to somewhere else so that your server can have a rest on network.  
##### Fast way to set up redirection (CurseForge Modpack only)
~~There is a fast way to set up redirection when you have a CurseForge manifest of a Modpack.  
Extract ``manifest.json`` to PWD and run  
``java -jar ModpackUpdater-version.jar --curseforgeConfigurator manifest.json``  
or  
``java -jar ModpackUpdater-version.jar -f manifest.json``  
And it will automatically read the manifest, download these mods from CurseForge,
and create a link file called ``modupdater_server_curseforge_modlinks.json`` which contains redirecting infomation.~~  
The API is not available now so it's expected not to work.
##### Manually add source
**WIP**
### Client side
For client side, just double-click the 
``ModpackUpdater-version.jar`` 
file and it would left a 
``modupdater_client_config.json``  
Then the 
``"updateServerAddress": "http://example.com:14238"``
should be replaced with your server address with port set in ModpackUpdater server side.
