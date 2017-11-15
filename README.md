# vpn-ops
The GFW of China has adopted more and more sophisticated algorithms to detect VPN/proxy traffic. 2017 September witnessed numerous Shadowsocks, V2ray and other VPN/proxy servers being blocked only a few hours after coming online. Without an effective protocol to obfuscate VPN/proxy traffic and evade detection, the only way to keep a VPN/proxy service availabe is to change IP address. Yet manually changing IP address of a VPS and then notifying all users is a pain. Hence a tool that automates this process and minize VPN/proxy service downtime is highly desirable. 

## how vpn-ops works
vpn-ops first issues a command to start a tomcat web server on the VPS via ssh. This way, we can determine if the IP address of the VPS is blocked by checking if the web server is reachable within China. Suppose the IP address of the VPS is 1.2.3.4, and we have a domain name s3.example.com pointing to that IP address. We can go to a site availability testing tool like http://ce.cloud.360.cn/ or https://www.17ce.com/ to find out. In vpn-ops this is done with PhantomJS, a headless browser. If the domain can be resoved within China, but the web server is not reachable, that means the IP address has been blocked by GFW. In such case, vpn-ops will change the IP address of the VPS. Not every IaaS provider provides a mechanism to change IP address of a VPS. A reliable workaround is to take a snapshot of the VPS, create a new VPS based on the snapshot and then destroy the old VPS. vpn-ops accomplishes this operation utilizing the API provided by the IaaS providers. At last, with the API provided by DNS hosting providers, vps-ops updates the DNS record to make the domain name point to the new IP address. 

vpn-ops can be run as a cron job every a few minutes, which means if the IP address gets blocked, vpn-ops will performe an IP address change in a short while, users of the VPN/proxy service won't know about what's going on behind the scene except experincing a brief interruption of service.

## how to setup
1. On your VPS, install docker and copy your public key to your server.
2. On your machine(the machine from which your will frun vpn-ops), download PhantomJS from http://phantomjs.org/download.html.
3. Find out your API auth token from your VPS provider. Currently only DigitalOcean is supported. 
4. Find out your API auth token from your DNS hosting provider. Currently only Cloudflare is supported.
5. Create a config file `config.json`. Edit the config file as follow example
```json
{"servers": [{
    "sshPort": 22,
    "passphrashLocation": "/path/to/your/sshkey/passphrase",
    "privatekeyLocation": "/path/to/your/ssh/private/key",
    "hostname": "s3.example.com",
    "vpsProvider": "DigitalOcean",
    "dnsHosting": "Cloudflare",
    "subDomain": "s3",
    "domain": "example.com",
    "ipAddr": "xxx.xxx.xxx.xxx",
    "username": "the username to login your VPS"
}]}
```
where the `privatekeyLocation` where your ssh private key is stored, and the `passphrashLocation` where the passphrase for the ssh private key is stored, the `hostname` is the full qualified domain name of your VPS, and the `ipAddr` is the IP address of your VPS. You can add multiple VPS servers in the config file.
6. Clone or download vpn-ops from https://github.com/wizard-merlin/vpn-ops. 
Edit src/main/resources/app-config.xml, fill in the `authToken` for your VPS provider 
```xml
    <bean id="digitalOcean" class="tk.wizardmerlin.operations.DigitalOceanVps">
        <!--TODO set authToken-->
        <property name="authToken" value=""/>
    </bean>
```
fill in the `authToken` and `authEmail` for your DNS hosting provider
```xml
    <bean id="cloudflare" class="tk.wizardmerlin.operations.Cloudflare">
        <!--TODO set authToken-->
        <property name="authToken" value=""/>
        <!--TODO set authEmail-->
        <property name="authEmail" value=""/>
        <property name="apiBaseUrl" value="https://api.cloudflare.com/client/v4/"/>
    </bean>
```
fill in the `configFilePath` with the path to the config file you just created
```xml
    <bean id="ipRotator" class="tk.wizardmerlin.operations.IpRotator">
        <property name="availabilityTester" ref="ceCloud360"/>
        <property name="sshService" ref="sshService"/>
        <!--TODO set configFilePath-->
        <property name="configFilePath" value="/path/to/config.json"/>
        <property name="digitalOcean" ref="digitalOcean"/>
        <property name="vultr" ref="vultr"/>
        <property name="cloudflare" ref="cloudflare"/>
    </bean>
```
fill in the `phantomjs.binary.path` with the path of the downloaded phantomJS binary executable file.
```xml
    <util:map id="capabilityMap" key-type="java.lang.String" value-type="java.lang.String">
        <entry key="javascriptEnabled" value="true"/>
        <entry key="takesScreenshot" value="true"/>
        <!--TODO set phantomjs.binary.path-->
        <entry key="phantomjs.binary.path" value="/path/to/phantomjs"/>
        <entry key="phantomjs.page.settings.userAgent"
               value="Mozilla/5.0 (Windows NT 6.0) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.41 Safari/535.1"/>
    </util:map>
```
7. Build an executable jar with the command `mvn clean package`
Create a cron job to run vpn-ops periodically. On *nix systems and mac OS, edit the crontab with `crontab -e`. For example, adding the following line will run vpn-ops every hour.
```
0 * * * * java -jar /path/to/vpn-ops-1.0-SNAPSHOT.jar
```
On Windows system, you may create a scheduled take from control panel.

