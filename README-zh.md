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
##### 快速配置非整合包的模组文件 (使用Modrinth源)
如果已经有模组文件但是却没有配置下载源，你应该执行以下命令：    
``java -jar ModpackUpdater-version.jar --mp``  
这将会对所有未配置重定向的模组文件计算其哈希值，并在Modrinth上自动寻找其下载源并配置好。
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
其中，"hashValues"项为每个mod的jar文件在三种哈希算法下的哈希值。  
"common"列表中的mod是要求客户端必须安装并及时更新的mod，
而"optional"列表中的mod是允许玩家自行选择是否安装的mod（例如高清修复Optifine这类，客户端装不装都不影响进入服务器的这种）。
### 更新MOD
客户端请求到mod列表后，将会与本地的mod做比对。  
在这之前，客户端会提前计算好本地mod在多个哈希算法下的哈希值表，且以哈希值表为比对mod的**唯一**标准，
所以**不必为“玩家可能修改mod文件名导致同一个mod下载两份”或类似的潜在问题而担心**。  
做好比对后，客户端会显示出三份mod列表：
1. 必选Mods：本地没有而服务器端有的"common"列表中的Mod（无勾选选项，列表中的Mod均会被下载）
2. 可选Mods：本地没有而服务器端有的"optional"列表中的Mod（可选择其中的Mod勾选，勾选的Mod将会被下载）
3. 要删除的Mods：本地有而服务器端"common"列表中没有的Mod，若服务器端"optional"列表中也没有这个Mod，则该Mod会默认勾选；反之则默认不勾选。（勾选的Mod将会被删除）
### 下载MOD
当玩家在客户端点击“更新Mods”按钮时，程序将会按照上文中Mod列表所述的更新规则进行更新。  
程序会先删除多余的Mod，后下载需要的Mod。
下载每一个Mod时，程序都会向 `http://example.com:14238/mods/downloads/{hashName}/{hashValue}` 发送GET请求，
其中`{hashName}`为检索Mod文件所使用的哈希算法（目前支持MD5, SHA-256, SHA-512, 其中SHA-512为客户端默认使用的算法），  
而`{hashValue}`为该Mod使用`{hashName}`算法计算出的哈希值字串（不分大小写）。
*注：此处`/mods/downloads/{hashName}/{hashValue}`也可替换为`/mods/downloads/{Mod本体的文件名}`，服务端仍然会返回相应的MOD，但我不推荐这么做，此举是为了向1.2.000以前版本的客户端提供支持。*  
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