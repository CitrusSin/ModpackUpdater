# Mod包更新器
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/3fea904a0c874f7bb9222fc2eafc04c4)](https://www.codacy.com/gh/Micrafast/ModpackUpdater/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Micrafast/ModpackUpdater&amp;utm_campaign=Badge_Grade)  
自动管理Minecraft服务器与客户端之间mods的同步

你是不是受够了每次开个Minecraft mod服结果和蔼的玩家们一致要求加这个加那个mod  
加吧，结果一堆玩家傻fufu地没更新mod包，然后还要指点他们如何更新mod  
不加吧，玩家爸爸不高兴

不管你有没有这种问题，反正我是有了，我受够了，于是就写了这个小程序

## 如何使用

此程序基于HTTP协议之上，所以服务器那边要单独多开个端口留给此程序  
默认端口是14238。  

### 服务端
对于服务端, 运行这个命令（工作目录下得有mods文件夹）:
``java -jar ModpackUpdater-version.jar --server``  
然后它会输出一些日志并且留下配置文件
``modupdater_server_config.json``, 
可修改的配置都在这个文件内。
#### 自定义下载源
服务器流量挺贵的，所以你可以把一部分或全部的mod下载重定向到其它地方。
##### 快速配置Modrinth下载源 (Modrinth整合包)
Modrinth整合包文件的后缀名通常是".mrpack"。  
可运行如下指令以自动下载整合包中的mod并自动配置Modrinth下载源的重定向。  
``java -jar ModpackUpdater-version.jar --modrinthModpack=xxx.mrpack``  
or  
``java -jar ModpackUpdater-version.jar --mrpack=xxx.mrpack``
##### 快速配置CurseForge下载源 (CurseForge整合包)
API方跑路了，官方API又要key，这个功能现在应该不会起作用。也许哪天又恢复了呢，希望如此。  
~~如果你用现成的CurseForge整合包，配置下载源可以把下载mod，配置重定向一步到位；
如果你已经下好了mod也没事，当mod文件夹下已经存在同名的mod时是不会下载的  
将CurseForge压缩包内的 ``manifest.json`` 解压至工作目录并运行：  
``java -jar ModpackUpdater-version.jar --curseforgeConfigurator manifest.json``  
或者  
``java -jar ModpackUpdater-version.jar -f manifest.json``  
接下来坐与放宽，程序会自动为你下载这个json内指定的所有mod，自动配置好重定向链接文件
``modupdater_server_curseforge_modlinks.json``。~~
##### 手动添加下载源：
举个例子 ``modupdater_redirection_list.json``
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

### 客户端
对于客户端, 无脑双击
``ModpackUpdater-version.jar`` ，它会留下这个文件
``modupdater_client_config.json``  
其中这一项
``"updateServerAddress": "http://example.com:14238"``
要改成你服务器的地址加ModpackUpdater设置的端口

## 协议相关
此软件通过HTTP协议实现服务，具体细节如下（现假定服务端在 `http://example.com:14238` 提供服务）：
### 请求MOD列表
客户端刷新服务器MOD列表时，向 `http://example.com:14238/mods/list` 发送GET请求，
服务端将会返回如下格式的json：
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
其中，"md5"项为每个mod的jar文件的MD5哈希值。  
"common"列表中的mod是要求客户端必须安装并及时更新的mod，
而"optional"列表中的mod是允许玩家自行选择是否安装的mod（例如高清修复Optifine这类，客户端装不装都不影响进入服务器的这种）。
### 更新MOD
客户端请求到mod列表后，将会与本地的mod做比对。  
在这之前，客户端会提前计算好本地mod的MD5哈希值，且以MD5哈希值为比对mod的**唯一**标准，
所以**不必为“玩家可能修改mod文件名导致同一个mod下载两份”或类似的潜在问题而担心**。  
做好比对后，客户端会显示出三份mod列表：
1. 必选Mods：本地没有而服务器端有的"common"列表中的Mod（无勾选选项，列表中的Mod均会被下载）
2. 可选Mods：本地没有而服务器端有的"optional"列表中的Mod（可选择其中的Mod勾选，勾选的Mod将会被下载）
3. 要删除的Mods：本地有而服务器端"common"列表中没有的Mod，若服务器端"optional"列表中也没有这个Mod，则该Mod会默认勾选；反之则默认不勾选。（勾选的Mod将会被删除）
### 下载MOD
当玩家在客户端点击“更新Mods”按钮时，程序将会按照上文中Mod列表所述的更新规则进行更新。  
程序会先删除多余的Mod，后下载需要的Mod。
下载每一个Mod时，程序都会向 `http://example.com:14238/mods/downloads/{MD5}` 发送GET请求，
其中`{MD5}`为该Mod的16进制MD5哈希值字串（不分大小写）。
*注：此处`{MD5}`也可替换为Mod本体的文件名，服务端仍然会返回相应的MOD，但我极度不推荐这么做，此举是为了向1.2.000以前版本的客户端提供支持。*  
如果服务器端对该Mod没有设置下载重定向，那么服务器将会直接回应200 OK，并发送该Mod的jar文件包本体，如下：
````
HTTP/1.1 200 OK
Connection: Keep-Alive
Content-Length: 15517
Content-Type: application/java-archive
Date: Thu, 18 Aug 2022 05:23:47 GMT
Server: ModpackUpdateService/1.4.000

[该Mod本体的数据]
````
而如果服务器端对该Mod设置了下载重定向，服务器则会回应301 Moved Permanently，
并在Location请求头中附上跳转地址，如下：
````
HTTP/1.1 301 Moved Permanently
Location: https://cdn.modrinth.com/data/hvFnDODi/versions/0.1.2/lazydfu-0.1.2.jar
Date: Thu, 18 Aug 2022 05:37:13 GMT
Server: ModpackUpdateService/1.4.000
Content-Length: 0


````
浏览器以及主流的http请求库都会自动处理3xx跳转回应，所以开发时不必特别处理这种跳转请求。