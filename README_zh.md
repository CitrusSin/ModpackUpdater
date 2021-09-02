#  Mod包更新器
## 自动管理Minecraft服务器与客户端之间mods的同步

你是不是受够了每次开个Minecraft mod服结果和蔼的玩家们一致要求加这个加那个mod  
加吧，结果一堆玩家傻fufu地没更新mod包，然后还要指点他们如何更新mod  
不加吧，玩家爸爸不高兴

不管你有没有这种问题，反正我是有了，我受够了，于是就写了这个小程序

### 如何使用

此程序基于HTTP协议之上，所以服务器那边要单独多开个端口留给此程序  
默认端口是14238。  

#### 服务端
对于服务端, 运行这个命令（工作目录下得有mods文件夹）:
``java -jar ModpackUpdater-version.jar --server``  
然后它会输出一些日志并且留下配置文件
``modupdater_server_config.json``, 
可修改的配置都在这个文件内。
##### 自定义CurseForge下载源
服务器流量挺贵的，所以你可以把一部分或全部的mod下载重定向到CurseForge，嘛，虽然国内挺抽风，但这只是可选的，且不会默认开启。
###### 快速配置CurseForge下载源 (仅限CurseForge整合包)
如果你用现成的CurseForge整合包，配置下载源可以把下载mod，配置重定向一步到位；
如果你已经下好了mod也没事，当mod文件夹下已经存在同名的mod时是不会下载的  
将CurseForge压缩包内的 ``manifest.json`` 解压至工作目录并运行：  
``java -jar ModpackUpdater-version.jar --curseforgeConfigurator manifest.json``  
或者  
``java -jar ModpackUpdater-version.jar -f manifest.json``  
接下来坐与放宽，程序会自动为你下载这个json内指定的所有mod，自动配置好重定向链接文件
``modupdater_server_curseforge_modlinks.json``。
###### 手动添加CurseForge下载源：
``modupdater_server_curseforge_modlinks.json`` 的格式是类似于这样的:  
````
[
  {
    "localMod": {
      "md5": "bd7bd8bbcc8bf69339766b3b5951b42c",
      "fileName": "jei-1.16.5-7.6.3.81.jar"
    },
    "curseForgeContext": {
      "projectID": 238222,
      "fileID": 3272082,
      "required": true,
      "url": "https://edge.forgecdn.net/files/3272/82/jei-1.16.5-7.6.3.81.jar"
    }
  },
  {
    "localMod": {
      "md5": "e892a46e806a6fcf945bfe4999665b53",
      "fileName": "Ding-1.16.5-1.3.0.jar"
    },
    "curseForgeContext": {
      "projectID": 231275,
      "fileID": 3222705,
      "required": true,
      "url": "https://edge.forgecdn.net/files/3222/705/Ding-1.16.5-1.3.0.jar"
    }
  },
  ...
]
````
其中所有的项都相当于一个本地mod文件与CurseForge上的资源的对应关系。  
配置这个文件应该~~不~~很难。  
一个一个加文件确实挺令人抓狂的，但原谅我吧，在没有CurseForge ID列表的情况下，
我也很难写出一个自动识别一个mod的CurseForge ID的逻辑啊（（（

#### 客户端
对于客户端, 无脑双击
``ModpackUpdater-version.jar`` ，它会留下这个文件
``modupdater_client_config.json``  
其中这一项
``"updateServerAddress": "http://example.com:14238"``
要改成你服务器的地址加ModpackUpdater设置的端口