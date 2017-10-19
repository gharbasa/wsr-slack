package com.dt.wsr;

import java.text.SimpleDateFormat
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*
import groovy.util.logging.Slf4j
import org.slf4j.Logger

@Slf4j
class Post {   
    static def Utils utils  = new Utils()
    static def channelParser = new ChannelParser()

    Post() {
        
    }
    
    static void main(String... args) {
        def post = new Post();

        if(Utils.properties.myName == null || Utils.properties.myName == "") {
            log.error "Problem reading myName from config.properties"
            println "Problem reading myName from config.properties"
            System.exit(1)
        }

        log.info "Hey, lets read and post wsr of " + Utils.properties.myName

        
        
        def fileContent = ""
        try {
            fileContent = post.readFile()
            log.debug "Successfully read the file content " + Utils.properties.fileName
        }
        catch(FileNotFoundException e1) {
            log.error Utils.properties.fileName + " File Not found"
            println Utils.properties.fileName + " File Not found"// + e1.printStackTrace()
            System.exit(1)
        }
        catch(Exception e2) {
            log.error "Problem reading the file " + Utils.properties.fileName + ", File may be empty."
            println "Problem reading the file " + Utils.properties.fileName + ", File may be empty."//e2.printStackTrace()
            System.exit(1)
        }

        def payLoad = post.preparePayload(fileContent)

        //println "Payload content=" +  payLoad.toString()
        def resp = post.postPayload(payLoad)
        if(resp.statusLine.statusCode == 200) //OK
        {
            log.debug "Succefully posted your weekly status report(" + Utils.properties.myName + ")"
            println "Succefully posted your weekly status report(" + Utils.properties.myName + ")"
            post.refreshTemplate(fileContent)
        } else {
            log.error "Problem posting your weekly status report. Try again after sometime"
            println "Problem posting your weekly status report. Try again after sometime"
            System.exit(1) 
        }
    }

    def readFile() {
        def fileName = Utils.properties.fileName
        String currentDir = new File(".").getAbsolutePath()
        def fileURL = currentDir.substring(0, currentDir.length() - 2) + "/" + fileName
        println 'Reading the file '  + fileURL
        log.debug 'Reading the file '  + fileURL
        def fileContent = new File(fileURL).getText()
        if (!fileContent?.trim())
            throw new Exception("Empty file")
        fileContent 
    }

    def preparePayload(fileContent) {
        log.debug "preparing payload"
        def postHeading = "Weekly status report (" + Utils.thisMondayStr + " ~ " + Utils.thisFridayStr + ")"
        def paddingColor = "#36a64f"
        def boundaryLine = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
        
        def payLoad = new groovy.json.JsonBuilder()
        def json = new groovy.json.JsonBuilder()
        json pretext: postHeading, color: paddingColor, 
                    title: boundaryLine, text: fileContent, 
                    footer: boundaryLine, ts: Utils.today.getTime()/1000,
                    author_name: Utils.properties.myName
        payLoad attachments: json
        payLoad
    }

    def postPayload(payload) {
        log.debug "posting payload url=" + Utils.properties.slackIncomingHookUrl + ", channel=" + Utils.properties.channel
        def http = new RESTClient(Utils.properties.slackIncomingHookUrl)
        def resp = http.post(
                    path: Utils.properties.channel, 
                    body: payload.toString(), 
                    requestContentType: JSON )
        println "POST Success: ${resp.statusLine}"
        resp
        //assert resp.statusLine.statusCode == 201
    }

    /**
        1. Take a backup of the wsr.txt file in history folder
        2. Refresh wsr.txt for the next week
    */
    def refreshTemplate(fileContent) {
        log.debug "refreshing the template"
        def fileName = Utils.properties.fileName
        def backupFileName = fileName.take(fileName.lastIndexOf('.')) +
                    "_" + Utils.todayDateStr +
                    fileName.substring(fileName.lastIndexOf('.'), fileName.length())

        String currentDir = new File(".").getAbsolutePath()
        def historyDirectory = currentDir.substring(0, currentDir.length() - 2) + "/" + Utils.properties.folder
        log.debug historyDirectory + " trying to create directory if it doesn't exists."
        new File(historyDirectory).mkdir() //create one if it doesn't exists
        
        backupFileName = historyDirectory + "/" + backupFileName
        //take backeup
        log.debug backupFileName + " backup file."
        File backupFile = new File(backupFileName)
        backupFile.text = fileContent
        println fileName + " has been taken backup to " + backupFileName
        log.debug fileName + " has been taken backup to " + backupFileName
        //Refresh the wsr template
        def template = new Template()
        template.refreshTemplate()
    }
}
