package cz.eman.swagger.codegen.generator.java

import cz.eman.gradle.swagger.languages.AbsJavaCodegen

/**
 * TODO CLASS_DESCRIPTION
 *
 * @author david.sucharda (david.sucharda@eman.cz)
 * @since 1.0.0
 */
open class JavaRoomCodegen : AbsJavaCodegen() {

    init {
        mArtifactId = "java-room"
        modelNameSuffix = "Entity"

        templateDir = "java-room"
        embeddedTemplateDir = templateDir
    }

    override fun processOpts() {
        super.processOpts()

        // Do not generate these files.
        apiTemplateFiles.clear()
        apiTestTemplateFiles.clear()
        supportingFiles.clear()
    }


}