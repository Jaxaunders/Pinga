import java.net.URL

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.pinga"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pinga"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.recyclerview)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.play.services.location)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

tasks.register("updateSig") {
    group = "other"
    description = "Download full SIG company IDs and service UUIDs, write to assets."

    doLast {
        val assetsDir = file("src/main/assets").apply { mkdirs() }

        val companyUrl = "https://raw.githubusercontent.com/NordicSemiconductor/bluetooth-numbers-database/master/v1/company_ids.json"
        val serviceUrl = "https://raw.githubusercontent.com/NordicSemiconductor/bluetooth-numbers-database/master/v1/service_uuids.json"

        val companiesRaw = URL(companyUrl).readText()
        val companyPairs = Regex("""\{[^}]*"code"\s*:\s*(\d+)[^}]*"name"\s*:\s*"([^"]+)"""")
            .findAll(companiesRaw)
            .map { it.groupValues[1] to it.groupValues[2] }
            .toList()

        val companiesJson = buildString {
            append("{\n")
            companyPairs.forEachIndexed { i, (code, name) ->
                if (i > 0) append(",\n")
                append("  \"").append(code).append("\": \"")
                    .append(name.replace("\"", "\\\"")).append("\"")
            }
            append("\n}\n")
        }
        file("$assetsDir/sig_companies.json").writeText(companiesJson)
        println("✅ sig_companies.json with ${companyPairs.size} entries")

        val servicesRaw = URL(serviceUrl).readText()
        val baseTail = "-0000-1000-8000-00805f9b34fb"

        val objRegex = Regex("""\{[^}]*}""")
        val nameRegex = Regex(""""name"\s*:\s*"([^"]+)"""")
        val uuidRegex = Regex(""""uuid"\s*:\s*"([^"]+)"""")

        val seen = HashSet<String>()
        val servicesJson = buildString {
            append("{\n")
            var first = true
            fun put(key: String, name: String) {
                val k = key.uppercase()
                if (!seen.add(k)) return
                if (!first) append(",\n") else first = false
                append("  \"").append(k).append("\": \"")
                    .append(name.replace("\"", "\\\"")).append("\"")
            }

            objRegex.findAll(servicesRaw).forEach { m ->
                val body = m.value
                val name = nameRegex.find(body)?.groupValues?.get(1) ?: return@forEach
                val uuidRaw = uuidRegex.find(body)?.groupValues?.get(1) ?: return@forEach
                val u = uuidRaw.removePrefix("0x").removePrefix("0X").lowercase()

                if (Regex("^[0-9a-f]{4}$").matches(u)) {
                    put(u, name)
                    put("0000$u$baseTail", name)
                } else {
                    put(u, name)
                    Regex("^0000([0-9a-f]{4})$baseTail$").find(u)?.let {
                        put(it.groupValues[1], name)
                    }
                }
            }
            append("\n}\n")
        }

        file("$assetsDir/sig_services.json").writeText(servicesJson)
        println("✅ sig_services.json with ${seen.size} keys")
        println("All done → ${assetsDir.absolutePath}")
    }
}
