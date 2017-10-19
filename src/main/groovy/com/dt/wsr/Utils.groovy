package com.dt.wsr
import java.text.SimpleDateFormat
import groovy.util.logging.Slf4j
import org.slf4j.Logger

@Slf4j
public class Utils {
	static final def configFileName = "/config.properties"
	
	static String dateFormatStr = "MM-dd-yyyy"
    static Date today = new Date()
    static String todayDateStr = new SimpleDateFormat("MM-dd-yyyy-HH.mm.ss").format(today)
    static Date nextMonday = today + 6 - ((5 + (today.format("u") as int)) % 7)

    static String nextMondayStr
    static Date thisMonday
    static String thisMondayStr
    static Date thisFriday
    static String thisFridayStr

    static def properties = null

    static {
        if (formatFriendlyDate(nextMonday) == formatFriendlyDate(today)) //If Today is Monday
            nextMonday = nextMonday + 7
        nextMondayStr = new SimpleDateFormat("MM-dd-yyyy-HH.mm.ss").format(nextMonday)
        thisMonday = nextMonday - 7
        thisMondayStr = new SimpleDateFormat(dateFormatStr).format(thisMonday)
        //println "This Monday=" + thisMondayStr
        thisFriday = nextMonday - 3
        thisFridayStr = new SimpleDateFormat(dateFormatStr).format(thisFriday)
    }

    static def isTodayMonday() {
        return (formatFriendlyDate(thisMonday) == formatFriendlyDate(today)) //If Today is Monday
    }

    /**
        C'tor
    */
    Utils() {
        if(properties == null) {
            properties = new Properties()
        	this.getClass().getResource(configFileName).withInputStream {
            	properties.load(it)
        	}
            log.info "username from config file " + properties.myName
            if(properties.myName == null || properties.myName == "") {
                log.info "Missing user name in config file, reading it from system login."
                setUserName()
            }
        }
    }

    static def formatFriendlyDate(date) {
        def friendlyDateFormat = "MM-dd-yyyy - EEEE"
        def dateStr = new SimpleDateFormat(friendlyDateFormat).format(date)
        dateStr
    }

    def setUserName() {
        log.info "Reading username from system login."
        String osName = System.getProperty( "os.name" ).toLowerCase();
        String className = null;
        String methodName = "getUsername";

        if( osName.contains( "windows" ) ){
            log.info "You are in Windows platform."
            className = "com.sun.security.auth.module.NTSystem";
            methodName = "getName";
        }
        else if( osName.contains( "linux" ) ){
            log.info "You are in Linux platform."
            className = "com.sun.security.auth.module.UnixSystem";
        }
        else if( osName.contains( "solaris" ) || osName.contains( "sunos" ) ){
            log.info "You are in Solaris platform."
            className = "com.sun.security.auth.module.SolarisSystem";
        }
        else if ((osName.indexOf("mac") >= 0) || (osName.indexOf("darwin") >= 0)) {
            log.info "You are in Mac platform."
            className = "com.sun.security.auth.module.UnixSystem";
        }

        if( className != null ){
            Class<?> c = Class.forName( className );
            java.lang.reflect.Method method = c.getDeclaredMethod( methodName );
            Object o = c.newInstance();
            properties.myName = method.invoke( o );
            log.info "Login username = " + properties.myName
        }
    }


}
