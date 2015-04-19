description('Creates the persistent token domain class for the Spring Security Core plugin') {
    usage """
	Usage: grails s2-create-persistent-token <domain-class-name>

	Creates a persistent token domain class

	Example: grails s2-create-persistent-token com.yourapp.PersistentLogin
"""
    argument name: 'Domain class', description: "Fully packaged domain class Name"
}

model = model(args[0])
println model.packagePath
println model.simpleName

String artifactFolder = "grails-app/domain"
String packageName = model.packagePath.replace("/", ".")
String packagePath = packageName.split("\\.").join("/");

render template: "PersistentLogin.groovy.template",
        destination: file("${artifactFolder}/${packagePath}/${model.simpleName}.groovy"),
        model: [packageName: packageName, className: model.simpleName]

updateConfig(packageName, model.simpleName)

private void updateConfig(packageName, className) {
    def configFile = new File(baseDir, 'grails-app/conf/application.groovy')
    if (!configFile.exists()) {
        consoleLogger.log "Creating application.groovy file...."
        configFile.createNewFile()
    } else {
        consoleLogger.log "Updating application.groovy file...."
    }

    configFile.withWriterAppend { BufferedWriter writer ->
        writer.writeLine "grails.plugin.springsecurity.rememberMe.persistent = true"
        writer.writeLine "grails.plugin.springsecurity.rememberMe.persistentToken.domainClassName = '${packageName}.${className}'"
        writer.newLine()
    }
}