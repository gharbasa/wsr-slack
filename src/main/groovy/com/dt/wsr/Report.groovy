package com.dt.wsr;

import java.text.SimpleDateFormat
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.slf4j.Logger

@Slf4j
class Report {   

    static def Utils utils = new Utils()
    static def channelParser = new ChannelParser()
    static def postings = [:] //Username: WSR
    static def startDate = Utils.thisMonday - 7
    static def endDate = Utils.thisMonday + 1

    static  def ReportTemplate = "<html><head><title>Weekly Status Report</title></head><body>" +
                                        "<h2>Weekly Status Report ({STARTDATE}&nbsp;&nbsp;TO&nbsp;&nbsp;{ENDDATE})</h2>" +
                                        "<dl>{ORDEREDLIST}</dl>" +
                                        "</body></html>"
    
    static final def ListTemplate = "<br/><dt><b>{RESOURCE}</b></dt><br/><dd>{CONTENT}</dd>"
    
    static final def reportFileName = Utils.properties.reportFile
    Report() {
    
    }

    static void main(String... args) {
        def report = new Report()
        log.debug "Hey, lets generate the report from the author desk " + Utils.properties.myName
        def resp = report.readChannelHistory()
        if(resp.statusLine.statusCode != 200) {//OK
        	println "Problem reading the channel history"
        	log.error "Problem reading the channel history"
            System.exit(1)
        }

		def data = resp.data
        postings = channelParser.parseChannelData(data)
		log.debug "report=" + postings
		//println postings

        //Generate report from postings
        report.generateReport(postings)


    }

    def readChannelHistory() {
        log.debug "reading chennel history=" + Utils.properties.channelHistoryUrl + ", channel=" + Utils.properties.chennelId + 
    				" channel path " + Utils.properties.channelHistoryUrlPath + ", between the dates " + 
                    Utils.formatFriendlyDate(startDate) + " and " + Utils.formatFriendlyDate(endDate)

        def http = new RESTClient(Utils.properties.channelHistoryUrl)
        
        def payload = [
    		token  		: Utils.properties.token,
    		inclusive   : true,
    		count   	: 100,
    		channel    	: Utils.properties.chennelId,
    		oldest		: startDate.getTime() / 1000,
    		latest		: endDate.getTime() / 1000
		]

		log.debug "Payload for reading chennel history=" + payload

        def resp = http.post(
                    path: Utils.properties.channelHistoryUrlPath,
                    body: payload,
                    requestContentType: URLENC,
        			headers: ['Content-Type': 'application/x-www-form-urlencoded']
            )
        println "GET Success: ${resp.statusLine}"
        resp
        //assert resp.statusLine.statusCode == 201
    }

    def generateReport(postings) {
        log.debug "Generating the report " + reportFileName
        ReportTemplate = ReportTemplate.replace("{STARTDATE}", Utils.formatFriendlyDate(startDate))
        ReportTemplate = ReportTemplate.replace("{ENDDATE}", Utils.formatFriendlyDate(endDate))
        def body = ""
        postings.each { posting ->
            def resourceName = posting.key
            def wsr = posting.value
            wsr = wsr.replaceAll("\n", "<br/>")
            def template = ListTemplate.replace("{RESOURCE}", resourceName)
            template = template.replace("{CONTENT}", wsr)
            body += template
        }
        ReportTemplate = ReportTemplate.replace("{ORDEREDLIST}", body)

        File newFile = new File(reportFileName)
        newFile.text = ReportTemplate
        println reportFileName + " has been generated."
        log.debug reportFileName + " has been generated."
    }

}
