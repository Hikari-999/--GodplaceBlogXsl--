# 域名申请和解析：

## 进入域名申请购买平台（以阿里云举例）

登录进阿里云后，搜索域名控制台，搜索自己想要注册的域名（那肯定越便宜越好），点击**立即注册**，然后购买完成后 等待审核通过（一般3-5分钟 如果要求完善信息，则完善相关信息后，再进行审核等待）

<img src="/articleImg/image-20250902170119163.png" alt="image-20250902170119163" style="zoom:50%;" />

审核通过后点击**解析**后，如图编写

<img src="/articleImg/image-20250902171049559.png" alt="image-20250902171049559" style="zoom:50%;" />

```
#进入nginx配置文件
sudo nano /etc/nginx/sites-enabled/default

#修改代理的server_name

#重启服务
sudo systemctl restart nginx

```

![image-20250822183625367](/articleImg/image-20250822183625367.png)

nginx即可解析域名为当前网站了



**前端发送请求路径修改**

http协议和websocket协议发送的请求路径修改

websocket协议，在websocket.config.ts中修改路径



![image-20250902172732777](/articleImg/image-20250902172732777.png)



http协议：在main.js中修改



![image-20250902172944647](/articleImg/image-20250902172944647.png)



**后端跨域配置CorsFilters.java修改**



![image-20250902173137377](/articleImg/image-20250902173137377.png)



## ***大陆服务器需要进行域名备案**（这里使用阿里云ICP备案）

**搜索ICP备案**

点击右侧的开始备案，填写相关信息（我的服务器是西南成都地区，图片可供参考备案时间）

![image-20250902173328700](/articleImg/image-20250902173328700.png)



# 申请SSL证书（https,以阿里云免费SSL证书为例）

<img src="/articleImg/image-20250902173837672.png" alt="image-20250902171049559" style="zoom:50%;" />

<img src="/articleImg/image-20250902173915997.png" alt="image-20250902171049559" style="zoom:65%;" />

如图申请一次免费证书，默认20个（有效期限一般为3个月）

签发完成后，点击**下载 nginx证书和JKS证书**

<img src="/articleImg/image-20250902174110001.png" alt="image-20250902171049559" style="zoom:50%;" />

将nginx证书拖拽到godplace/ssl目录下

<img src="/articleImg/image-20250902174337530.png" alt="image-20250902171049559" style="zoom:50%;" />

然后注释到80端口 采用https443端口，并且配置好key和pem的路径

![image-20250902174527048](/articleImg/image-20250902174527048.png)

nginx整体代码参考：

![image-20250902175159131](/articleImg/image-20250902175159131.png)

前端请求配置

![image-20250902193457313](/articleImg/image-20250902193457313.png)

后端跨域配置：

![image-20250902193618200](/articleImg/image-20250902193618200.png)

将ca.jks放在resource包目录下

![image-20250902193707667](/articleImg/image-20250902193707667.png)

配置文件添加SSL相关配置以及路径

![image-20250902193831806](/articleImg/image-20250902193831806.png)

