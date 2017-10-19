package com.dt.wsr;

import java.text.SimpleDateFormat
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*
import groovy.util.logging.Slf4j
import org.slf4j.Logger

@Slf4j
class Template {
    static def Utils utils = new Utils()

    Template() {
        
    }
    
    static void main(String... args) {
        def template = new Template();
        log.info "Hey, lets create/refresh " + Utils.properties.fileName + " for the next week."

        template.refreshTemplate()
    }

    def refreshTemplate() {
        def fileName = Utils.properties.fileName
        log.debug "refreshing the template"
        //Refresh the wsr template
        File newFile = new File(fileName)
        newFile.text = templateContent()
        println fileName + " has been refreshed for the next week."
        log.debug fileName + " has been refreshed for the next week."
    }

    /*
        New template for the next week.
    */
    def templateContent() {
        def monday = Utils.nextMonday
        if(Utils.isTodayMonday()) {
            monday = Utils.today
        }

        def textContent = "Highlights - (high level bullets on what you worked on or accomplished) \n\n"
        textContent += "  [" + Utils.formatFriendlyDate(monday) + "] \n\n"
        textContent += "    {start here} \n\n\n"
        textContent += "  [" + Utils.formatFriendlyDate(monday + 1) + "] \n\n"
        textContent += "    {start here} \n\n\n"
        textContent += "  [" + Utils.formatFriendlyDate(monday + 2) + "] \n\n"
        textContent += "    {start here} \n\n\n"
        textContent += "  [" + Utils.formatFriendlyDate(monday + 3) + "] \n\n"
        textContent += "    {start here} \n\n\n"
        textContent += "  [" + Utils.formatFriendlyDate(monday + 4) + "] \n\n"
        textContent += "    {start here} \n\n\n"
        textContent += "Lowlights - (any roadblocks, obstacles or impediments you encountered. Or just things that did not go well.)\n\n"
        textContent += "  {start here} \n\n"
        textContent += "Focus - Next week \n\n"
        textContent += "  {start here} \n\n"
        textContent
    }

}
