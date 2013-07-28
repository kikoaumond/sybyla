#!/bin/sh
# chkconfig: 2345 95 05
# description: Starts Sybyla API
# processname: syb-api
java -Dfile.encoding=UTF-8 -Djetty.http.headerbuffersize=16384 -server -Xss4096k -Xmx4G  -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -jar /home/ec2-user/syb-web-api-0.0.1-SNAPSHOT-jar-with-dependencies.jar -graph -tag -category -why -sentiment 

# put this script as syb-api in /etc/init.d/syb-api
# sudo /sbin/chkconfig syb-api on                                                                               
~                                                                               
~                                                                               
~                                                                               
~                                                                               
~                                                                               
~                                                                               
~                                                                               
~                                                                               
~                                                                               
~                                                                               
~                                                                               
~                                                                      
