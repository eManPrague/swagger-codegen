package cz.eman.swagger.codegen.generator.java

import cz.eman.gradle.swagger.languages.AbsJavaCodegen

/**
 * TODO CLASS_DESCRIPTION
 *
 * @author david.sucharda (david.sucharda@eman.cz)
 */
class JavaRetrofitCodegen : AbsJavaCodegen() {

    // TODO: Add service to API name

    init {
        mArtifactId = "java-retrofit2-client"

        templateDir = "java-retrofit2-client"
        embeddedTemplateDir = templateDir

        setLibrary(RETROFIT_2)
    }

    override fun processOpts() {
        super.processOpts()

        // Do not generate these files.
        supportingFiles.clear()
        apiTestTemplateFiles.clear()
    }
}