import com.android.build.api.dsl.LibraryExtension
import com.znaky.buildlogic.configureKotlinAndroid
import com.znaky.buildlogic.lib
import com.znaky.buildlogic.libs
import com.znaky.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("android-library"))
        pluginManager.apply(libs.pluginId("kotlin-android"))

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            // Namespace derived from the module path, e.g. :core:data -> com.znaky.core.data
            namespace = "com.znaky" + path.replace(':', '.')
            testOptions.unitTests.isReturnDefaultValues = true
        }
        dependencies {
            add("testImplementation", libs.lib("junit"))
            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
        }
    }
}
