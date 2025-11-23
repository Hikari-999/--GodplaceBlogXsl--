## linux常用命令

ctrl+c 终止进程

ctrl+z 挂起

```
#在linux指定目录下建立用户
sudo useradd godplace -m -s /bin/bash

#看状态
systemctl status mysql
systemctl status redis
systemctl status rabbitmq
systemctl status nginx
#重启
sudo systemctl restart sshd
sudo systemctl restart mysql
sudo systemctl restart redis
sudo systemctl restart rabbitmq
sudo systemctl restart nginx
#下载
sudo apt install nginx
sudo apt install openjdk-17-jre
sudo apt install redis
sudo apt install rabbitmq-server -y
sudo apt install mysql-server-8.0
#记得更新软件包
sudo apt update
```

```
#列出所有java进程
ps -ef | grep java
#后台运行jar包
nohup  java -jar my-project-backend-0.0.1-SNAPSHOT.jar &

#nginx配置文件
sudo nano /etc/nginx/sites-enabled/default
#redis配置文件
sudo nano /etc/redis/redis.conf
```

