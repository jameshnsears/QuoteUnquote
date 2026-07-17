import java.util.Properties

plugins {
    id("com.android.application")
    id("com.diffplug.spotless")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
    id("io.gitlab.arturbosch.detekt")
    id("jacoco")
    id("checkstyle")
}

// Config for Detekt
detekt {
    debug = true
    ignoreFailures = false
    buildUponDefaultConfig = true
    config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
    baseline = file("${rootProject.projectDir}/config/detekt/baseline.xml")
    parallel = true
}

// Config for Spotless
spotless {
    kotlin {
        target("**/*.kt")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target("**/*.gradle", "**/*.md", "**/.gitignore", "**/*.kts")
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("xml") {
        target("**/*.xml")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

composeCompiler {
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}


// Config for Checkstyle
checkstyle {
    toolVersion = libs.versions.checkstyle.get()
}
tasks.register<Checkstyle>("checkstyle") {
    group = "QuoteUnquote"
    description = "checkstyle"
    source("src/main/java", "src/main/kotlin")
    include("**/*.java", "**/*.kt")
    exclude("**/gen/**")
    classpath = files()
    ignoreFailures = false
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Config for Jacoco
jacoco {
    toolVersion = libs.versions.jacoco.get()
}

val fileExclusions = listOf(
    "**/BuildConfig.*",
    "**/*_Impl*.*",
    "**/*Binding.*",
    "**/*Double.*",
    "**/R.class",
    "**/R$*.class",
)

project.afterEvaluate {
    val android = extensions.getByType<com.android.build.api.dsl.ApplicationExtension>()
    val buildTypes = android.buildTypes.map { it.name }
    val productFlavors = android.productFlavors.map { it.name }.toMutableList()

    if (productFlavors.isEmpty()) productFlavors.add("")

    productFlavors.forEach { productFlavorName ->
        buildTypes.forEach { buildTypeName ->
            val sourceName = if (productFlavorName.isNotEmpty()) {
                "$productFlavorName${buildTypeName.replaceFirstChar { it.uppercase() }}"
            } else {
                buildTypeName
            }
            val capitalizedSourceName = sourceName.replaceFirstChar { it.uppercase() }

            fun JacocoReport.setupJacocoTask(type: String) {
                group = "quoteunquote"
                description = "Generate Jacoco $type coverage reports for $capitalizedSourceName"

                val subProjects = listOf(project, project.project(":cloudLib"), project.project(":utilsLib"))

                classDirectories.setFrom(
                    subProjects.map { subProject ->
                        val subBuildDir = subProject.layout.buildDirectory.get().asFile
                        fileTree("$subBuildDir/intermediates/kotlin_classes/$sourceName/classes") {
                            setExcludes(fileExclusions)
                        } + fileTree("$subBuildDir/intermediates/javac/$sourceName/classes") {
                            setExcludes(fileExclusions)
                        } + fileTree("$subBuildDir/intermediates/built_in_kotlinc/$sourceName/compile${capitalizedSourceName}Kotlin/classes") {
                            setExcludes(fileExclusions)
                        } + fileTree("$subBuildDir/intermediates/javac/$sourceName/compile${capitalizedSourceName}JavaWithJavac/classes") {
                            setExcludes(fileExclusions)
                        }
                    }
                )

                sourceDirectories.setFrom(
                    subProjects.flatMap { subProject ->
                        val dirs = mutableListOf(
                            "${subProject.projectDir}/src/main/java",
                            "${subProject.projectDir}/src/main/kotlin"
                        )
                        if (productFlavorName.isNotEmpty()) {
                            dirs.add("${subProject.projectDir}/src/$productFlavorName/java")
                            dirs.add("${subProject.projectDir}/src/$productFlavorName/kotlin")
                        }
                        dirs
                    }
                )

                reports {
                    xml.required.set(true)
                    html.required.set(true)
                    xml.outputLocation.set(file("${layout.buildDirectory.get().asFile}/reports/jacoco/${sourceName}_$type.xml"))
                    html.outputLocation.set(file("${layout.buildDirectory.get().asFile}/reports/jacoco/${sourceName}_$type"))
                }
            }

            tasks.register<JacocoReport>("test${capitalizedSourceName}UnitTestCoverage") {
                group = "quoteunquote"
                description = ""
                dependsOn(
                    "test${capitalizedSourceName}UnitTest",
                    ":cloudLib:test${capitalizedSourceName}UnitTest",
                    ":utilsLib:test${capitalizedSourceName}UnitTest",
                )

                setupJacocoTask("unit")
                executionData.setFrom(
                    fileTree(layout.buildDirectory) { include("outputs/unit_test_code_coverage/${sourceName}UnitTest/*.exec") },
                    fileTree(project.project(":cloudLib").layout.buildDirectory) { include("outputs/unit_test_code_coverage/${sourceName}UnitTest/*.exec") },
                    fileTree(project.project(":utilsLib").layout.buildDirectory) { include("outputs/unit_test_code_coverage/${sourceName}UnitTest/*.exec") },
                )
            }

            tasks.register<JacocoReport>("connected${capitalizedSourceName}AndroidTestCoverage") {
                group = "quoteunquote"
                description = "Generate Jacoco connected coverage reports for $capitalizedSourceName"
                dependsOn("connected${capitalizedSourceName}AndroidTest")
                setupJacocoTask("connected")
                executionData.setFrom(fileTree(layout.buildDirectory) { include("outputs/code_coverage/${sourceName}AndroidTest/**/*.ec") })
            }

            tasks.register<JacocoReport>("test${capitalizedSourceName}CombinedCoverage") {
                group = "quoteunquote"
                description = ""
                dependsOn(
                    "test${capitalizedSourceName}UnitTestCoverage",
                    "connected${capitalizedSourceName}AndroidTestCoverage",
                )
                setupJacocoTask("combined")
                executionData.setFrom(
                    fileTree(layout.buildDirectory) {
                        include(
                            "outputs/unit_test_code_coverage/${sourceName}UnitTest/*.exec",
                            "outputs/code_coverage/${sourceName}AndroidTest/**/*.ec"
                        )
                    },
                    fileTree(project.project(":cloudLib").layout.buildDirectory) { include("outputs/unit_test_code_coverage/${sourceName}UnitTest/*.exec") },
                    fileTree(project.project(":utilsLib").layout.buildDirectory) { include("outputs/unit_test_code_coverage/${sourceName}UnitTest/*.exec") },
                )
            }
        }
    }
}

// App configuration
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()

if (!localPropertiesFile.exists()) {
    localProperties.setProperty("RELEASE_STORE_PASSWORD", "")
    localProperties.setProperty("RELEASE_KEY_PASSWORD", "")
    localProperties.setProperty("RELEASE_KEY_ALIAS", "")
    localProperties.setProperty("RELEASE_STORE_FILE", "keystore.jks")
    localPropertiesFile.bufferedWriter().use { writer ->
        localProperties.store(writer, "empty, as creating the file is done manually via gpg")
    }
    file("keystore.jks").writeText("")
}
localPropertiesFile.inputStream().use<java.io.InputStream, Unit> { inputStream ->
    localProperties.load(inputStream)
}

val gitHash = providers.exec {
    commandLine("git", "rev-parse", "--short=8", "HEAD")
}.standardOutput.asText.map { it.trim() }.getOrElse("no-git")

android {
    namespace = "com.github.jameshnsears.quoteunquote"
    compileSdk = 37

    signingConfigs {
        create("googleplay") {
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
            storeFile = file(localProperties.getProperty("RELEASE_STORE_FILE"))
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
        }
    }

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf()
        applicationId = "com.github.jameshnsears.quoteunquote"
        minSdk = 31
        targetSdk = 37

        versionCode = 2013137
        versionName = "4.57.0"

        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
        buildConfigField("String", "GIT_HASH", "\"$gitHash\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes += listOf(
                "**/module-info.class",
                "LICENSE",
                "README.md",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "DATABASE_QUOTATIONS", "\"quotations.db.prod\"")
            ndk { debugSymbolLevel = "FULL" }
        }
        debug {
            buildConfigField("String", "DATABASE_QUOTATIONS", "\"quotations.db.prod\"")
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    flavorDimensions += "Version"
    productFlavors {
        create("googleplay") {
            dimension = "Version"
            versionNameSuffix = "-googleplay"
            signingConfig = signingConfigs.getByName("googleplay")
        }
        create("fdroid") {
            dimension = "Version"
            versionNameSuffix = "-fdroid"
            isDefault = true
        }
        create("espresso") {
            dimension = "Version"
            versionNameSuffix = "-espresso"
        }
        create("uiautomator") {
            dimension = "Version"
            versionNameSuffix = "-uiautomator"
        }
    }

    sourceSets {
        getByName("main") {
            java.directories.add("src/main/java")
            java.directories.add("src/main/kotlin")
            kotlin.directories.add("src/main/java")
            kotlin.directories.add("src/main/kotlin")
        }
        getByName("androidTest") {
            java.directories.add("src/androidTest/java")
            java.directories.add("src/androidTest/kotlin")
            kotlin.directories.add("src/androidTest/java")
            kotlin.directories.add("src/androidTest/kotlin")
            assets.directories.add("$projectDir/schemas")
        }
        getByName("test") {
            java.directories.add("src/test/java")
            java.directories.add("src/test/kotlin")
            kotlin.directories.add("src/test/java")
            kotlin.directories.add("src/test/kotlin")
        }
    }

    testOptions {
        animationsDisabled = true
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    tasks.withType<Test>().configureEach {
        maxHeapSize = "1024m"
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    lint {
        // Detecting "Liar APIs" is difficult...
        // This forces lint to check all libraries, not just your code
        checkDependencies = true
        // Specifically watch for NewApi issues
        fatal += "NewApi"
        // Report issues even if they are suppressed in libraries
        checkAllWarnings = true

        disable.addAll(listOf("InvalidPackage", "Instantiatable"))
        checkReleaseBuilds = false
        abortOnError = false
        warningsAsErrors = false
        baseline = file("$rootDir/config/lint/baseline.xml")
        checkAllWarnings = true
        htmlReport = true
        xmlReport = true
    }
}

configurations.all {
    // OEM liar API usage
    resolutionStrategy {
        force("androidx.core:core:${libs.versions.coreKtx.get()}")
        force("androidx.core:core-ktx:${libs.versions.coreKtx.get()}")
        force("androidx.test:core:1.7.0")
        force("androidx.test:core-ktx:1.7.0")
        force("androidx.test:runner:1.7.0")
        force("androidx.test:rules:1.7.0")
        force("androidx.test:monitor:1.7.0")
    }
}

dependencies {
    constraints {
        implementation(libs.androidx.core) {
            version { strictly(libs.versions.coreKtx.get()) }
        }
        implementation(libs.androidx.core.ktx) {
            version { strictly(libs.versions.coreKtx.get()) }
        }
    }
    // AndroidX
    implementation(libs.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)

    // UI - Material & Compose
    implementation(libs.material)
    implementation(libs.colorpickerpreference)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.rxjava2)
    implementation(libs.androidx.room.guava)
    annotationProcessor(libs.androidx.room.compiler)

    // Reactive
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxbinding)

    // Networking & Parsing
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.jsoup)
    implementation(libs.json.kotlin.schema)
    implementation(libs.commons.csv)

    // Utils
    implementation(libs.guava)
    implementation(libs.timber)
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    // Project Libraries
    implementation(project(":cloudLib"))
    implementation(project(":utilsLib"))

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.fragment.testing)
    debugImplementation(libs.leakcanary.android)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Google Play
    "googleplayImplementation"(platform(libs.firebase.bom))

    // Unit Tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.rules)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.logback.classic)

    // Android Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4.android)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.kotlin.test.junit)
}
