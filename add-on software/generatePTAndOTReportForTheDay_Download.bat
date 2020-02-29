set mainDirectory=%~dp0
cd /d %mainDirectory%
java -cp .\software;.\software\org.json.jar;.\software\org.apache.httpclient.jar;.\software\org.apache.httpcore.jar;.\software\org.apache.commons-logging.jar UsbongHTTPConnect "download" http://localhost/ "C:/Usbong/unit/add-on software/output/SVGH/" 
cd ..
%2
PAUSE
#exit