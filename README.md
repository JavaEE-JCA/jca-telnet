## JCA Tail Log files 

Custom JavaEE 8 jca resource adapter for tail log files like linux tail command.

#### build and run :     
**TomEE 8**     
`mvn clean compile install ; (cd ear-module/ ; mvn tomee:run)`

**Liberty/OpenLiberty**     
`mvn clean compile install ; (cd ear-module/ ; mvn liberty:run)`


***Example Implementation :***      
```java
@MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "file", propertyValue = "/var/log/syslog")})
public class TailSyslog implements TailListener {

    @Filter(".*mah454:.*")
    public void readMah454SyslogMessages(String message) {
        System.out.println("Mah454 send a message: " + message);
    }

    @Filter(".*root:.*")
    public void readRootSyslogMessages(String message) {
        System.out.println("Root send a message: " + message);
    }

    @Filter(".*custom:.*")
    public void readCustomSyslogMessages(String message) {
        System.out.println("Custom send a message: " + message);
    }

    @Filter("^((?!root|mah454|custom).)*$")
    public void readOtherMessages(String message) {
        System.out.println("Other processor send a message: " + message);
    }
}
```

***test :***
```shell script
logger -t mah454 "Hello dear"
logger -t custom "Wowwww"
logger -t root "I am ready for you."
sudo systemctl restart cron
```

***output***
```text
[INFO] Mah454 send a message: Jul 24 14:54:24 skynet-laptop mah454: Hello dear    
[INFO] Custom send a message: Jul 24 14:54:24 skynet-laptop custom: Wowwww    
[INFO] Root send a message: Jul 24 14:54:24 skynet-laptop root: I am ready for you.    
[INFO] Other processor send a message: Jul 24 14:54:24 skynet-laptop systemd[1]: Stopping Regular background program processing daemon...    
[INFO] Other processor send a message: Jul 24 14:54:24 skynet-laptop systemd[1]: cron.service: Succeeded.    
[INFO] Other processor send a message: Jul 24 14:54:24 skynet-laptop systemd[1]: Stopped Regular background program processing daemon.     
[INFO] Other processor send a message: Jul 24 14:54:24 skynet-laptop systemd[1]: Started Regular background program processing daemon.     
[INFO] Other processor send a message: Jul 24 14:54:24 skynet-laptop cron[123458]: (CRON) INFO (pidfile fd = 3)     
[INFO] Other processor send a message: Jul 24 14:54:24 skynet-laptop cron[123458]: (CRON) INFO (Skipping @reboot jobs -- not system startup)      
```