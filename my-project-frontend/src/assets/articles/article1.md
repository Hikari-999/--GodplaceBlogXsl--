# 如何在Linux部署博客项目

## 服务器基础信息：

操作系统：Ubuntu 22.04 64位

配置：2核2G 3M带宽

具体可以参考阿里云个人开发99计划

JDK >= 17

Mysql >= 8.0

Redis = latest

RabbitMQ = latest

## 服务器技术栈介绍：

前端：Vue3+ElementUI+Fontawsome

后端：WebSocket+Mybatis-Plus+Redis+RabbitMQ+SpringSecurity+JWT+SpringBoot+Lombok工具库+FastJSON2+JavaMailSender+mysql等等

## 部署步骤：

### 1.阿里云面板配置：

（1）打开控制台面板并找到远程连接，创建服务器非root外个人账号

```
sudo useradd godplace -m -s /bin/bash

ls /home  #检查一下home目录下是否创建好了

sudo passwd godplace #配置密码
xxxxxxxxx  #设置密码
xxxxxxxxx  #重新输入密码

给godplace添加一下root权限
#打开文件夹 这里用nano/vim看个人喜好
sudo nano /etc/sudoers  

#在当前文件下面最后一行添加 执行sudo不添加密码
godplace ALL=(ALL) NOPASSWD:ALL

ctrl+x -> Y -> enter退出编辑页面

```

<img src="/articleImg/image-20250822134925707.png" alt="image-20250822134925707" style="zoom:50%;" />

（2）配置安全组(开放如图所示6个端口)**如果不知道怎么配置就默认开发全部端口0.0.0.0/0 后续再慢慢学习怎么开放端口**

![image-20250822165000659](/articleImg/image-20250822165000659.png)

### 2.配置FinalShell操作（具体配置看个人习惯XShell或者远程SSH操作或者其他的都行）

<img src="/articleImg/image-20250822142105062.png" alt="image-20250822142105062" style="zoom:50%;" />

如果你输入了正确的密码还是不能登录，那么可能服务器默认配置了禁止SSH通过密码登录（只能用私钥登录），那么还是在远程连接 进入文件夹

```
sudo nano /etc/ssh/sshd_config

#找到代码行 修改为
PasswordAuthentication yes

#重启服务
sudo systemctl restart sshd

#此时就可以成功登录了
```

通过FinalSHell进入到服务器后，先更新一下服务器软件包

`sudo apt update`

### 3.部署前端模版

（1）通过nginx反向代理部署前端项目,先下载nginx

```
sudo apt install nginx

sudo systemctl status nginx #可以检查一下nginx服务器的状态
```

（2）前端部分修改配置文件

**在App.vue中：**

修改ws.url为你当前后端的公网地址：

![image-20250822143640918](/articleImg/image-20250822143640918.png)

在main.js中：

修改axios请求发送地址为当前公网后端地址：

![image-20250822143820695](/articleImg/image-20250822143820695.png)

（3）打包项目（在package.json中 点击build打包成dist文件，直接拖拽到FinalSHell中/home/godplace中去）

![image-20250822144302634](/articleImg/image-20250822144302634.png)

（4）修改nginx的前端代理路径

```
#进入nginx配置文件

sudo nano /etc/nginx/sites-enabled/default
```

![](/articleImg/image-20250822145742597.png)

重启nginx服务器

`sudo systemctl restart nginx`



**如果修改文件并且重启后页面还是500报错 那就是权限问题**

**添加读取权限**

```
sudo chmod -R 755 /home/godplace
```

**一般到现在前端网页就能实现了**

### 4.部署后端内容

#### 4.1后端项目代码一些修改的内容

##### 4.1.1 解决跨域问题

在CorsFilters.java中添加 你的公网ip就行了

![image-20250822151641079](/articleImg/image-20250822151641079.png)

##### 4.1.2 配置文件修改

在配置文件中 修改自己的邮件smtp账号密码，以及对应的mysql账号密码

![image-20250822151705642](/articleImg/image-20250822151705642.png)

##### 4.1.3 maven打包，同理拖拽到/home/godplace目录下



#### 4.2 安装java运行环境

```
sudo apt install openjdk-17-jre
```



#### 4.3 安装并配置Redis

##### 4.3.1安装Redis

```
sudo apt install redis
```

##### 4.3.2配置Redis远程连接

```
sudo nano /etc/redis/redis.conf

#找到相关代码 进行修改
# 允许任何主机连接、访问
bind 127.0.0.1 ::1改为 bind 0.0.0.0 ::1
 
# 关闭保护模式
protected-mode yes 改为 protected-mode no
 
# 允许启动后在后台运行，即关闭命令行窗口后仍能运行
daemonize no 改为 daemonize yes

#退出编辑状态后 重启redis服务
sudo systemctl restart redis-server
```



#### 4.4安装并配置RabbitMQ

##### 4.4.1更新系统并安装 RabbitMQ

```
# 1. 更新软件包列表

sudo apt update

# 2. 直接安装 RabbitMQ 服务器

# Ubuntu 官方源中的 RabbitMQ 会自动处理 Erlang 依赖

sudo apt install rabbitmq-server -y
```

##### 4.4.2管理 RabbitMQ 服务

```
# 启动 RabbitMQ 服务

sudo systemctl start rabbitmq-server

# 设置 RabbitMQ 开机自启

sudo systemctl enable rabbitmq-server

# 检查服务状态，确认它正在运行

sudo systemctl status rabbitmq-server
```

##### 4.4.3配置 RabbitMQ

```
sudo rabbitmq-plugins enable rabbitmq_management   #开启rabbitmq可视化操作页面


# 创建用户：用户名 admin，密码 admin
sudo rabbitmqctl add_user admin admin

# 设置用户标签为 administrator（管理员角色）
sudo rabbitmqctl set_user_tags admin administrator

# 授予该用户对默认虚拟主机 "/" 的所有权限
sudo rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

#重启rabbitmq
sudo systemctl restart rabbitmq-server
```

#### 4.5 安装并配置mysql

##### 4.5.1 安装mysql

```
sudo apt install mysql-server-8.0
```

##### 4.5.2 配置mysql

```
sudo mysql -u root -p

#配置一下mysql的临时密码
xxxxxxx

#进入到mysql界面
use mysql

#配置可远程访问mysql
update user set host = '%' where user='root';

#刷新
flush privileges;

#修改设置一下root的密码
alter user 'root'@'%' identified with mysql_native_password by 'xxxxxx';

#退出mysql
exit

#修改mysql配置文件
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf;

#注释掉bing-address
#bind-address            = 127.0.0.1

#重启mysql
sudo systemctl restart mysql
```

##### 4.5.3 设置导入数据库表

Navicat中创建数据库

![image-20250822162518628](/articleImg/image-20250822162518628.png)

导入对应的sql文件即可。