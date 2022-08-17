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