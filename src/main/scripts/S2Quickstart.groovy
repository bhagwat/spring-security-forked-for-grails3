import grails.util.GrailsNameUtils

description("Creates artifacts for the Spring Security plugin") {
    usage """
Usage: grails s2-quickstart <domain-class-package> <user-class-name> <role-class-name> [requestmap-class-name] [--groupClassName=group-class-name]
       or grails s2-quickstart --uiOnly

Creates a user and role class (and optionally a requestmap class) in the specified package.
If you specify a role-group name with the groupClassName argument, role/group classes will also be generated.
If you specify the uiOnly flag, no domain classes are created but the plugin settings are initialized (useful with LDAP, Mock, Shibboleth, etc.)

Example: grails s2-quickstart com.yourapp User Role
Example: grails s2-quickstart com.yourapp User Role --groupClassName=RoleGroup
Example: grails s2-quickstart com.yourapp Person Authority Requestmap
Example: grails s2-quickstart --uiOnly
"""

}
String groupClassName = argsMap.groupClassName
Boolean uiOnly = argsMap.uiOnly

String packageName = args.getAt(0);
String userClassName = args.getAt(1);
String roleClassName = args.getAt(2);
String requestmapClassName = args.getAt(3);
String groupClassNameMessage = groupClassName ? ", and role/group classes for $groupClassName" : ''

if (args.size() == 4) {
    consoleLogger.log "Creating User class ${args[1]}, Role class ${args[2]}, and Requestmap class ${args[3]}$groupClassNameMessage in package ${args[0]}"
    (packageName, userClassName, roleClassName, requestmapClassName) = args
} else {
    consoleLogger.log "Creating User class ${args[1]} and Role class ${args[2]}$groupClassNameMessage in package ${args[0]}"
    (packageName, userClassName, roleClassName) = args
}

updateConfig(uiOnly, packageName, userClassName, roleClassName, groupClassName, requestmapClassName)

if (uiOnly) {
    consoleLogger.log """
*******************************************************
* Your grails-app/conf/Config.groovy has been updated *
* with security settings; please verify that the      *
* values are correct.                                 *
*******************************************************
"""
} else {
    Map templateAttributes = [packageName        : packageName,
                              userClassName      : userClassName,
                              userClassProperty  : GrailsNameUtils.getPropertyName(userClassName),
                              roleClassName      : roleClassName,
                              roleClassProperty  : GrailsNameUtils.getPropertyName(roleClassName),
                              requestmapClassName: requestmapClassName,
                              groupClassName     : groupClassName,
                              groupClassProperty : groupClassName ? GrailsNameUtils.getPropertyName(groupClassName) : null]


    String artifactFolder = "grails-app/domain"
    String packagePath = packageName.split("\\.").join("/");

    [
            "Authority"      : roleClassName,
            "Person"         : userClassName,
            "PersonAuthority": "$userClassName$roleClassName",
    ].each { templateName, className ->
        render template: "${templateName}.groovy.template",
                destination: file("$artifactFolder/$packagePath/${className}.groovy"),
                model: templateAttributes

    }
    if (groupClassName) {
        [
                "AuthorityGroup"         : groupClassName,
                "AuthorityGroupAuthority": "$groupClassName$roleClassName",
                "PersonAuthorityGroup"   : "${userClassName}${groupClassName}"
        ].each { templateName, className ->
            render template: "${templateName}.groovy.template",
                    destination: file("$artifactFolder/$packagePath/${className}.groovy"),
                    model: templateAttributes

        }
    }
    if (requestmapClassName) {
        render template: "Requestmap.groovy.template",
                destination: file("$artifactFolder/$packagePath/${requestmapClassName}.groovy"),
                model: templateAttributes

    }

    consoleLogger.log """
*******************************************************
* Created security-related domain classes. Your       *
* grails-app/conf/Config.groovy has been updated with *
* the class names of the configured domain classes;   *
* please verify that the values are correct.          *
*******************************************************
"""
}

private void updateConfig(Boolean uiOnly, String packageName, String userClassName, String roleClassName, String groupClassName, String requestmapClassName) {
    def configFile = new File(baseDir, 'grails-app/conf/application.groovy')
    if (!configFile.exists()) {
        consoleLogger.log "Creating application.groovy file...."
        configFile.createNewFile()
    } else {
        consoleLogger.log "Updating application.groovy file...."
    }

    configFile.withWriterAppend { BufferedWriter writer ->
        writer.newLine()
        writer.newLine()
        writer.writeLine '// Added by the Spring Security Core plugin:'
        if (!uiOnly) {
            writer.writeLine "grails.plugin.springsecurity.userLookup.userDomainClassName = '${packageName}.$userClassName'"
            writer.writeLine "grails.plugin.springsecurity.userLookup.authorityJoinClassName = '${packageName}.$userClassName$roleClassName'"
            writer.writeLine "grails.plugin.springsecurity.authority.className = '${packageName}.$roleClassName'"
        }
        if (groupClassName) {
            writer.writeLine "grails.plugin.springsecurity.authority.groupAuthorityNameField = 'authorities'"
            writer.writeLine "grails.plugin.springsecurity.useRoleGroups = true"
        }
        if (requestmapClassName) {
            writer.writeLine "grails.plugin.springsecurity.requestMap.className = '${packageName}.$requestmapClassName'"
            writer.writeLine "grails.plugin.springsecurity.securityConfigType = 'Requestmap'"
        }
        writer.writeLine 'grails.plugin.springsecurity.controllerAnnotations.staticRules = ['
        writer.writeLine "\t'/':                              ['permitAll'],"
        writer.writeLine "\t'/index':                         ['permitAll'],"
        writer.writeLine "\t'/index.gsp':                     ['permitAll'],"
        writer.writeLine "\t'/assets/**':                     ['permitAll'],"
        writer.writeLine "\t'/**/js/**':                      ['permitAll'],"
        writer.writeLine "\t'/**/css/**':                     ['permitAll'],"
        writer.writeLine "\t'/**/images/**':                  ['permitAll'],"
        writer.writeLine "\t'/**/favicon.ico':                ['permitAll']"

        writer.writeLine ']'
        writer.newLine()
    }
}


