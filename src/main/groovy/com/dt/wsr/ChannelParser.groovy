package com.dt.wsr

import java.text.SimpleDateFormat
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*
import groovy.util.logging.Slf4j
import org.slf4j.Logger
import groovy.json.JsonOutput

@Slf4j
class ChannelParser {   

    ChannelParser() {
     	
    }

    def parseChannelData(data) {
    	def postings = [:]
    	def jsonSlurper = new groovy.json.JsonSlurper()
		def jsonResponse = jsonSlurper.parseText(JsonOutput.toJson(data)) 
		if (jsonResponse.has_more == true)  { 
			log.debug "Hey, there is still more data to fetch."
			println "Hey, there is still more data to fetch."
		}

		def messages = jsonResponse.messages
		if(messages == null)
		{
			log.info "No report is found"
			println "No report is found"
			return
		}

		log.debug "Found " + messages.size()  + " wsrs"
		messages.each { message ->
			//println message.username //TODO: Check to see only incoming-webhooks
			def attachments = message.attachments
			if(attachments != null) {
				log.debug "Found " + attachments.size()  + " attachments"
				attachments.each { attachment ->
					def auth_name = attachment.author_name
					def text = attachment.text
					//println auth_name + ":" + attachment.ts
					if (auth_name != null && auth_name.trim() != "") {//both are not empty
						if(postings[auth_name] == null) {
							postings[auth_name] = text //most recent post comes first
							//return
						} else {
							log.debug auth_name + " latest WSR has been received, ignoring this."	
						}
						//println "Saving the posting"
					} else {
						log.debug "Author is missing  in the attachments response json, ignoring this."
					}
				}
			}
		}
		postings
    }

}