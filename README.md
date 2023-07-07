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
    "hashValues": {
      "MD5": "367e4dbc9cf9c918997df0892566e491",
      "SHA-256": "0cc69ddef9f50a0a0515c2e756ae9139c32b07b101121d96c153775dcc0ad174",
      "SHA-512": "344793519691abc511ca5ea88a00c95cae67727bf00b5f34adcb07b97b24f19e034c9a12c6cfcaa2c7f43eab193e8abe28cd437aeb9148eeb1258f7c78473099"
    },
    "url": "https://cdn.modrinth.com/data/E6FUtRJh/versions/E0HCy6sV/Adorn-3.8.1%2B1.19.2-fabric.jar"
  },
  {
    "hashValues": {
      "MD5": "337332a7dcec6fa44735700bcb8b521d",
      "SHA-256": "7172ca079a4276e56bc460acf0097957116c4ae4773d890178202425ce24dfb8",
      "SHA-512": "092f55d9f46a64dbadfb78e5cf6dcc03d9d77999e52ca00d63adff8fe39166bcf24eb254a2bc6257e95a118bad6912ce56cfe8fb71793f3034226e7f0243f2ac"
    },
    "url": "https://cdn.modrinth.com/data/G1epq3jN/versions/1.19.1-fabric0.58.5-1.3.1/advancementinfo-1.19.1-fabric0.58.5-1.3.1.jar"
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
      "hashValues": {
        "MD5": "c268b1ec362c8b6ea925442cb7f707cf",
        "SHA-256": "550443b81cbd75528d67c27f82249469b2debc77ef9f7f8919f88a0320ed6f69",
        "SHA-512": "68fbc68c1118f3030ce457c5130c33ca673b8947d289a5d22b216b0b78cc5295260fe8954c9efa36d2f1274c7081bf5cdf65d9499e05f5c6e63c0be9e3e249f5"
      },
      "fileName": "carpet-fixes-1.19-1.12.2.jar"
    },
    {
      "hashValues": {
        "MD5": "9a73bb8744abd59ca9f4cdac1144d23b",
        "SHA-256": "2a41b6121065cba9f642f9476571daa7ea57808e46320683014d44e352a51056",
        "SHA-512": "8bf38f9b7212e9637ab7899cc4ad66ab55c60d9006daf4b576d3ab3881e8be82e7cecb1df85bba061a03c7ecf7210fa463034d2a586272cb2a74674af3bfb1bb"
      },
      "fileName": "Chunky-1.3.38.jar"
    }
  ],
  "optional": [
    {
      "hashValues": {
        "MD5": "07a9a306d795d4accf7f1863f904e2bc",
        "SHA-256": "806f9187d22b3dfe520b521df6fb13206eea8d4e25f4f23bbc3e5156649f6419",
        "SHA-512": "0cb441d67196700a0fd660ea0e2c8d58b37616370368558de1bf26c8bcb1db95ccba652923ed2305c8439bd83e00983d4f359659b86d72c148160464f03fc99f"
      },
      "fileName": "iris-mc1.19.2-1.5.2.jar"
    }
  ]
}
````
"hashValues" is the hash value table of the jar file.  
Mods in "common" list are required to keep up with the server,
while mods in "optional" list just literally means optional, or in other words, usually client side only mods.
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
When downloading each mod, the client will send request to `http://example.com:14238/mods/downloads/{hashName}/{hashValue}` 
where `{hashName}` is the hash algorithm name to search the mod file (supports MD5, SHA-256, SHA-512; SHA-512 is default)  
and `{hashValue}` is the hash value of the mod jar file in the corresponding hash algorithm.
*Note: `/mods/downloads/{hashName}/{hashValue}` can be replaced by `/mods/downloads/{fileName}`, and the server will respond correctly.
But this is not recommended because it is deprecated and designed to provide support to older versions before 1.2.000.*  
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