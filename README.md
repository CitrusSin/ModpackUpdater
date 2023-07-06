# Modpack Updater
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/3fea904a0c874f7bb9222fc2eafc04c4)](https://www.codacy.com/gh/Micrafast/ModpackUpdater/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Micrafast/ModpackUpdater&amp;utm_campaign=Badge_Grade)    
Synchronize Minecraft mods between server and client

Are you fed up with players in your server asking you to add some mods?  
In this case if you accept, many players actually don't know how to update their mods list.  
And if you ignore their request, they might not be so happy.

All in all, I have this problem, so I make this little program to solve the problem.

## How to use

This program is based on HTTP, so you should open a port for this program on your server.  
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
##### Without modpack file (Modrinth source)
If your mod files have been already in your mods folder without setting up redirection, you should run this command:  
``java -jar ModpackUpdater-version.jar --mp``  
which will calculate the hash value for every mod file and find its download url on Modrinth.
##### Fast way to set up redirection (Modrinth Modpack)
The name of a Modrinth Modpack file ends with ".mrpack".  
Use the command below to set up Modrinth redirection.  
``java -jar ModpackUpdater-version.jar --modrinthModpack=xxx.mrpack``  
or  
``java -jar ModpackUpdater-version.jar --mrpack=xxx.mrpack``
##### Fast way to set up redirection (CurseForge Modpack)
The API is not available now, so it's not expected to work now. Might be available again someday, hopefully.  
~~There is a fast way to set up redirection when you have a CurseForge manifest of a Modpack.  
Extract ``manifest.json`` to PWD and run  
``java -jar ModpackUpdater-version.jar --curseforgeConfiguration=manifest.json``  
or  
``java -jar ModpackUpdater-version.jar --cfConfig=manifest.json``  
And it will automatically read the manifest, download these mods from CurseForge,
and create a link file called ``modupdater_server_curseforge_modlinks.json`` which contains redirecting infomation.~~
##### Manually add source
An example of ``modupdater_redirection_list.json``  
````json
[
  {
    "md5": "bd7bd8bbcc8bf69339766b3b5951b42c",
    "url": "https://edge.forgecdn.net/files/3272/82/jei-1.16.5-7.6.3.81.jar"
  },
  {
    "md5": "e892a46e806a6fcf945bfe4999665b53",
    "url": "https://edge.forgecdn.net/files/3222/705/Ding-1.16.5-1.3.0.jar"
  }
]
````
### Client side
For client side, just double-click the 
``ModpackUpdater-version.jar`` 
file and it would leave a 
``modupdater_client_config.json``  
Then the 
``"updateServerAddress": "http://example.com:14238"``
should be replaced with your server address with port set in ModpackUpdater server side.

## Protocol
HTTP Protocol, details are listed below (Assuming the server is serving at `http://example.com:14238`):
### Requesting Mod List
When the client refreshing mod list, it will send GET request to `http://example.com:14238/mods/list`,
and the server will return a json such as this: 
````json
{
  "common": [
    {
      "md5": "19601f4688469c8aab5ba3e6e0ef4e3b",
      "fileName":"architectury-4.5.75-fabric.jar"
    },
    //...
  ],
  "optional": [
    {
      "md5":"d5ec2a8babd0dbf95da81a7d7f9a3d15",
      "fileName":"iris-mc1.18.2-1.2.5.jar"
    },
    //...
  ]
}
````
"md5" is the MD5 value of the jar file.  
Mods in "common" list are required to keep up with the server,
while mods in "optional" list just literally means optional.
### Update Mod
After received mod list, the client will compare the local mod list with the remote one.   
Before that, the client calculates the MD5 value of each mod, and use MD5 value as the only identification of the mod.
So it's **unnecessary to worry that players might rename the mod file** or worry about other similar concerns.  
After comparison, the client will show three lists of mods: 
1. Mods force required: Doesn't exist in the local mod list but exists in the "common" remote mod list.
2. Mods optional: Doesn't exist in the local mod list but exists in the "optional" remote mod list.
3. Delete Mods: Exists in the local mod list but doesn't exist in the "common" remote mod list. Will be checked by default if the mod exists in the "optional" remote mod list, and vice versa.
### Download Mod
When player click the "Update Mods" button, the client will update mods according the mod list rules showed above.  
Deletion are performed before downloading.  
When downloading each mod, the client will send request to `http://example.com:14238/mods/downloads/{MD5}` 
where `{MD5}` is the MD5 value of the mod jar file.
*Note: `{MD5}` can be the file name of the mod jar file, and the server will respond correctly.
But this is not recommended because it is designed to provide support to older versions before 1.2.000.*  
If there's no redirection set for this mod, the server will respond `200 OK` and send the mod jar file, as below:
````
HTTP/1.1 200 OK
Connection: Keep-Alive
Content-Length: 15517
Content-Type: application/java-archive
Date: Thu, 18 Aug 2022 05:23:47 GMT
Server: ModpackUpdateService/1.4.000

[Data of the mod jar file]
````
If there is redirection set for this mod, the server will respond `301 Moved Permanently`
and redirect to the download url as below:
````
HTTP/1.1 301 Moved Permanently
Location: https://cdn.modrinth.com/data/hvFnDODi/versions/0.1.2/lazydfu-0.1.2.jar
Date: Thu, 18 Aug 2022 05:37:13 GMT
Server: ModpackUpdateService/1.4.000
Content-Length: 0


````