# wsr-slack

There are 2 features in this projects
1) post content to a slack channel using the command
     "./gradlew post"
  	
     Took advantage of slack incoming-webhook integration feature

2) read channel history and parse it using the command
    "./gradlew report"
  	
     Took advantage of slack rest api and oAuth to read the channel history
3) Refresh wsr template 
    "./gradlew template"
    
    This will refresh the template for the next week
 
 
config.properties file will guide you through the steps to input slack channel details and the oAuth token.
