jrebel.jar破解版，让你在编写j2ee不必多次重启tomcat
Javarebel和Tomcat开发部署应用 
1:下载javarebel（略）。
2：安装tomcat略。个人使用tomcat5.0.19
3：在tomcat中进行javarebel配置：打开{tomcat}\bin\ catalina.bat,找到set JAVA_OPTS处，在“=”右边添加如下代码：
-noverify -javaagent:d:\javarebel-2.0\javarebel.jar
个人设置后结果如下：
set JAVA_OPTS=-noverify -javaagent:d:\javarebel-2.0\javarebel.jar-Xms1024m -Xmx1024m
4：启动tomcat，
我的详细介绍配置，请访问
http://zzb20081225.blog.163.com/blog/#m=0&t=1&c=fks_087068080094083075080084094095085085088069087080095074080

