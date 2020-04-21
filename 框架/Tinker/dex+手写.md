## 一、市场



### 1.1 为什么会有热修复？作用/特点

- 重新打包发布消耗的人力物力比较大
- 新版本发布需要一段时间，如果这段时间 bug 一直存在，用户体验会很差
- 版本升级效率相对低，如果用户不愿意下载新版本，则 bug 得不到解决
- 热修复可以用来实现轻量级的更新，比如节日活动换肤换色等等



### 1.2 流程对比普通版本发布和热修复

- 正常发布，用户得重新下载安装，代价太大
- 热修复之前的流程和发布一样，但后面是用户程序自动去拉取补丁（用户没有感知）

- ![](https://user-gold-cdn.xitu.io/2020/4/14/1717725ce1b930c9?w=1151&h=509&f=png&s=291154)



### 1.3 市面热修复框架对比

- 实时修复就是用的过程修复，冷启动就是重启修复
- 在 GitHub 上能看源码的算开源的，开源的免费，不开源的收费
- ![image-20200414132933129](C:\Users\30797\AppData\Roaming\Typora\typora-user-images\image-20200414132933129.png)



## 二、原理

 

### 3.1 底层替换方案（NDK - AndFix）

- 替换原有类中的方法（指向新的方法），但这样一来无法添加新方法（会破坏原有类的结构）

- 稳定性差（最大的毛病），因为他的底层是直接通过 JNI 去修改 ArtMethod（结构体）的字段，由于 Android 是开源的，所以不同厂家对结构体可能会做不同的修改（就会导致 AndFix 这种，有的机子有效有的无效）
- 但是 Sophix 例外，它是将底层整个结构体替换，所以没有上诉问题



### 3.2 类加载方案（Tinker）

- 原理就是 App 重新启动后，让 classLoader 去加载新的类（修复包的类）




### 3.4 为什么需要冷启动

- 启动的 App    中的类已经被加载了，被加载的类我们无法将其卸载掉
- ![](https://user-gold-cdn.xitu.io/2020/4/14/17177c343818a285?w=689&h=465&f=png&s=193922)




### 3.3 Tinker 原理

- 在 DexPathList.findClass() 过程中，一个Classloader可以包含多个dex文件，每个dex文件被封装到一个Element对象，这些Element对象排列成有序的数组dexElements。当查找某个类时，会遍历所有的dex文件，如果找到则直接返回，不再继续遍历dexElements。也就是说当两个类不同的dex中出现，会优先处理排在前面的dex文件，这便是热修复的核心精髓，将需要修复的类所打包的dex文件插入到dexElements前面。




### 2.4 BaseDexClassLoder

- 转 java 类加载总结
- 对象.getClassLoader 源码往下追踪到看不了
- [AndroidXRef - 8 查看该类源码](http://androidxref.com/8.1.0_r33/xref/libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java)
- 首先该类有一个 DexPathList 
	![](https://user-gold-cdn.xitu.io/2020/4/14/1717888b547d77f5?w=576&h=76&f=png&s=31676)
- 构造方法中实例化
	![](https://user-gold-cdn.xitu.io/2020/4/14/171788bb36354dfe?w=1034&h=510&f=png&s=429764)
- findClass() 方法中会调用 DexPathList 的 findClass(,) 方法
	![](https://user-gold-cdn.xitu.io/2020/4/14/171789001d4ef074?w=929&h=348&f=png&s=33956)
	


### 2.5 DexPathList
- DexPathList 中有个 dexElements 数组 
	![](https://user-gold-cdn.xitu.io/2020/4/14/171789377e4fb25c?w=812&h=341&f=png&s=29118)
- 在 findClass 中循环去找 Element ，调用 Element 的 findClass 找了 Class 文件，先看 dexElements
	![](https://user-gold-cdn.xitu.io/2020/4/14/17178962c4b85745?w=1110&h=364&f=png&s=253961)

- 在看看 dexElements 源码，首先他的构造方法中 有个 makeDexElements 方法

  ![](https://user-gold-cdn.xitu.io/2020/4/14/17178ef860fdc6ca?w=1036&h=271&f=png&s=27099)

- 转到 makeDexElements  中 我们看到如果文件以 DEX_SUFFIX (.dex) 结尾的话就去加载这个文件

  ![](https://user-gold-cdn.xitu.io/2020/4/14/17178f5762caa973?w=1008&h=604&f=png&s=57396)

- dexElements 数组里存折一大堆 .dex 文件

  ![](https://user-gold-cdn.xitu.io/2020/4/14/17178f73b94280ea?w=1452&h=576&f=png&s=541562)

- 原理概括

  ![](https://user-gold-cdn.xitu.io/2020/4/14/17179096ed08bca3?w=1370&h=753&f=png&s=337128)





## 三、DEX 分包



#### 3.1 由来 

- Dalvik 虚拟机 65536 限制（ByteCode - 16 bits - 2^16 = 65536）

- ![](https://user-gold-cdn.xitu.io/2020/4/14/1717919519cf36b3?w=874&h=127&f=png&s=135324)



#### 3.2 LinearAlloc 限制

- ![image-20200414223920715](C:\Users\30797\AppData\Roaming\Typora\typora-user-images\image-20200414223920715.png)



#### 3.3 什么是分包机制

- ![](https://user-gold-cdn.xitu.io/2020/4/14/1717921d78191bf5?w=704&h=138&f=png&s=159450)



#### 3.4 配置 Gralde

- ![](https://user-gold-cdn.xitu.io/2020/4/14/171792328b8a8709?w=697&h=520&f=png&s=193741)



#### 3.5 multiDex 配置主包

- 设置哪些类放在主包里面
- 一般就是主活动和 Application 这些运行必要的类
- ![](https://user-gold-cdn.xitu.io/2020/4/14/17179265495e8260?w=704&h=219&f=png&s=111721)



#### 3.6 次包自动分包

- 由于我们 build.gradle 设置了 --set-max-idx-number=50000，所以除主包外，所有次包方法数超过 50000 的自动分包（这里剩下类的方法数不到 50000 所以只有一个 claaes2.dex 包，自动分包是自动的无法人为干扰）
- ![](https://user-gold-cdn.xitu.io/2020/4/14/1717939720fd6c80?w=1041&h=715&f=png&s=412307)



#### 3.7 放置位置

- 虽然我们可能有很多包，只要我们把修复的 Dex 文件放在一个，就不会有问题



## 四、修复



#### 4.1 生成 bug 的 apk

- ![](https://user-gold-cdn.xitu.io/2020/4/14/171794fdec2c4f29?w=529&h=408&f=png&s=229156)



#### 4.2 获得修复后的 classes2.dex

- ![](https://user-gold-cdn.xitu.io/2020/4/14/1717951aa26e3596?w=990&h=348&f=png&s=232814)



#### 4.3 模拟服务器下载到 SD 卡

- ![](https://user-gold-cdn.xitu.io/2020/4/14/1717954d17e451bb?w=1092&h=456&f=png&s=250524)



#### 4.4 模拟修复



##### 4.4.1 把文件从 SD 卡加载到应用私有目录

- ![](https://user-gold-cdn.xitu.io/2020/4/14/171795dc918e9fba?w=957&h=243&f=png&s=153028)



##### 4.4.2 getDir() 方法

- 它会返回：data/user/0/包名/app_odex 目录（odex 就是我们输的 ‘odex’ 参数）
- ![](https://user-gold-cdn.xitu.io/2020/4/14/171796489fe60407?w=712&h=129&f=png&s=91246)



##### 4.4.3 下载目录转存到目标目录：

- 获得下载的文件
- 新建目标文件夹下的文件，若已存在则删除（说明是旧版需要更新）
- 调用 FileUtils(自定义类) 的copyFile() 方法拷贝文件

![](https://user-gold-cdn.xitu.io/2020/4/14/1717675c00686b3c?w=845&h=551&f=png&s=292543)

##### 4.4.4 FileUtils 实现：

- 就一个方法，目的把文件拷贝到目标文件夹
- 把要导出的文件放到输入流里输入到缓冲区
- 把要导入的文件放到输出流里，从缓冲区输出数据

![](https://user-gold-cdn.xitu.io/2020/4/14/171767a49db17dfa?w=602&h=648&f=png&s=253778)



#### 4.5 FixDexUtils 修复功能类实现



##### 4.5.1 写一个 Set 用来存放待修复的 Dex 文件 

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717973fd4baff37?w=680&h=280&f=png&s=67166)



##### 4.5.2 每次修复前先清空集合

- ![](https://user-gold-cdn.xitu.io/2020/4/15/17179760fc7e67e2?w=731&h=399&f=png&s=149258)



##### 4.5.3 在 Fix 方法中调用修复方法

- ![](https://user-gold-cdn.xitu.io/2020/4/15/17179774af5bf682?w=1173&h=679&f=png&s=478987)



##### 4.5.4 获得目录，遍历得到除主 dex 外，所有待修复的 dex 文件

- ![](https://user-gold-cdn.xitu.io/2020/4/15/171797f50ad6578f?w=1064&h=508&f=png&s=267449)



##### 4.5.5 创建类加载器

- 循环遍历 Set ，获得类加载器

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717985e6e6af0d6?w=1128&h=311&f=png&s=208892)





##### 4.5.6 准备

- 创建临时解压目录、初始化类加载器

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717bc5a12a0836b?w=920&h=548&f=png&s=312960)



#### 4.6 热修复前的准备



##### 4.6.1 获得系统类加载器

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717bc8264cf8f0b?w=896&h=205&f=png&s=79069)



##### 4.6.2 编写数组合并工具类

- 类中只有一个数组合并方法

![](https://user-gold-cdn.xitu.io/2020/4/15/1717bca9bdae2b1c?w=1034&h=670&f=png&s=495127)



#### 4.7 反射工具类 ReflectUtils



##### 4.7.1 获取对象的 getFiled 方法

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717bd5c11a40c5b?w=894&h=437&f=png&s=192761)



##### 4.7.2 getPathList 方法

- 获得 BaseDexClassLoader 中的 pathList 对象

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717bda3a9ebf6b9?w=915&h=272&f=png&s=173568)



##### 4.7.3 setField 方法

- 拿到属性，进行设置

- ![image-20200415112333542](C:\Users\30797\AppData\Roaming\Typora\typora-user-images\image-20200415112333542.png)



##### 4.7.4 getDexElements 方法

- 获得 dexElements[]

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717bdee22dff966?w=983&h=301&f=png&s=175512)



#### 4.8 进行修复



##### 4.8.1 获得 ‘ 自己 ’ 的 dexElements 数组

- 通过自己 new 的 DexClassLoader 获得带修复的 dexElements 

- dexElements[] 在 pathList 对象里，所以要先获得它

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717be852489a010?w=1083&h=392&f=png&s=213340)



##### 4.8.2 获取 ‘ 系统 ’ 的 dexElements 对象

- 通过 context.getClassLoader 获得的系统 PathClassLoder 获得 dexElements

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717bebbbbaf4704?w=962&h=516&f=png&s=262068)



##### 4.8.3 调用合并方法进行合并

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717bf1c4e00f10d?w=981&h=425&f=png&s=262439)



##### 4.8.4 获得系统 pathList 重新赋值

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717bf3a7d6639ba?w=1037&h=523&f=png&s=364004)



#### 4.9 测试



##### 4.9.1  载入修复包进行修复

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717c023facf76b6?w=654&h=387&f=png&s=134851)



##### 4.9.2 载入修复包进行修复

- 由于每次都在一起动时就加载，所以后面都是用正确的

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717c046131f1ce6?w=672&h=225&f=png&s=108499)



##### 4.9.3 载入测试成功

- 打包测试成功

- ![](https://user-gold-cdn.xitu.io/2020/4/15/1717c0613bd9ba2a?w=427&h=666&f=png&s=159694)



## 五、so 和 资源文件修复



#### 5.1 