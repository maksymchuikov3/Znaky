import androidx.room.gradle.RoomExtension
import com.znaky.buildlogic.lib
import com.znaky.buildlogic.libs
import com.znaky.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("room"))
        pluginManager.apply(libs.pluginId("ksp"))

        extensions.configure<RoomExtension> {
            // Exported schemas are committed so migrations can be tested.
            schemaDirectory("$projectDir/schemas")
        }
        dependencies {
            add("implementation", libs.lib("room-runtime"))
            add("implementation", libs.lib("room-ktx"))
            add("ksp", libs.lib("room-compiler"))
        }
    }
}
