import com.android.build.api.artifact.SingleArtifact
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
}

fun getGitHash(shortHash: Boolean): String {
    val stdout = ByteArrayOutputStream()
    exec {
        if (shortHash)
            commandLine("git", "rev-parse", "--short", "HEAD")
        else
            commandLine("git", "rev-parse", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

val isSigningFileExist: Boolean = rootProject.file("signing.properties").exists()
var signingProperties = Properties()
if (isSigningFileExist) {
    signingProperties.load(FileInputStream(rootProject.file("signing.properties")))
}

android {
    compileSdk = 32
    buildToolsVersion = "32.0.0"
    namespace = "player.phonograph"

    val appName = "Phonograph Plus"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        minSdk = 24
        targetSdk = 32

        renderscriptTargetApi = 29
        vectorDrawables.useSupportLibrary = true

        applicationId = "player.phonograph"
        versionCode = 226
        versionName = "0.2.6.1.LTS"

        buildConfigField("String", "GIT_COMMIT_HASH", "\"${getGitHash(false)}\"")
        setProperty("archivesBaseName", "PhonographPlus_$versionName")
    }

    signingConfigs {
        create("release") {
            if (isSigningFileExist) {
                storeFile = File(signingProperties["storeFile"] as String)
                storePassword = signingProperties["storePassword"] as String
                keyAlias = signingProperties["keyAlias"] as String
                keyPassword = signingProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        getByName("release") {
            // signing
            if (isSigningFileExist) signingConfig = signingConfigs.getByName("release")

            // shrink
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(File("proguard-rules-base.pro"), File("proguard-rules-app.pro"))
        }
        getByName("debug") {
            // signing as well
            if (isSigningFileExist) signingConfig = signingConfigs.getByName("release")

            // package name
            applicationIdSuffix = ".debug"
        }
    }

    flavorDimensions += listOf("purpose")
    productFlavors {
        create("common") {
            dimension = "purpose"

            applicationIdSuffix = ".plus"
            resValue("string", "app_name", appName)
        }
        create("preview") {
            dimension = "purpose"
            matchingFallbacks.add("common")

            resValue("string", "app_name", "$appName Preview")
            applicationIdSuffix = ".plus.preview"
        }
        // test Proguard
        create("proguardTest") {
            dimension = "purpose"
            matchingFallbacks.add("common")

            resValue("string", "app_name", "$appName ProguardTest")
            applicationIdSuffix = ".plus.proguard"
        }
        // for checkout to locate a bug etc.
        create("checkout") {
            dimension = "purpose"
            matchingFallbacks.add("common")

            resValue("string", "app_name", "$appName Checkout")
            applicationIdSuffix = ".plus.checkout"
        }
        // for ci
        create("ci") {
            dimension = "purpose"
            matchingFallbacks.add("common")

            applicationIdSuffix = ".plus.ci"
            resValue("string", "app_name", "$appName CI Build")
        }
    }
    androidComponents {
        beforeVariants(selector().withBuildType("release")) { variantBuilder ->
            val favors = variantBuilder.productFlavors
            // no "release" type
            if (favors.contains("purpose" to "checkout") || favors.contains("purpose" to "ci")) {
                variantBuilder.enable = false
            }
        }
        beforeVariants(selector().withBuildType("debug")) { variantBuilder ->
            val favors = variantBuilder.productFlavors
            // no "debug" type
            if (favors.contains("purpose" to "proguardTest")) {
                variantBuilder.enable = false
            }
        }
        onVariants(selector().withBuildType("release")) { variant ->
            val productsDirectory = File(rootDir, "products").apply { mkdir() }
            val variantDirectory = File(productsDirectory, variant.name).apply { mkdir() }

            val version = android.defaultConfig.versionName
            val currentTimeString = SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().time)
            val gitHash = getGitHash(true)
            val apkName =
                when (variant.buildType) {
                    "release" -> "PhonographPlus_$version.apk"
                    else -> "PhonographPlus_${version}_${getGitHash(true)}_$currentTimeString.apk"
                }

            val loader = variant.artifacts.getBuiltArtifactsLoader()

            val apkOutputDirectory = variant.artifacts.get(SingleArtifact.APK)
            val mappingFile = variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE)

            afterEvaluate {
                loader.load(apkOutputDirectory.get())?.apply {
                    elements.forEach {
                        File(it.outputFile).copyTo(
                            File(variantDirectory, apkName), true
                        )
                    }
                }
                mappingFile.orNull?.asFile?.apply {
                    if (exists()) copyTo(
                        File(variantDirectory, "mapping_$gitHash.txt"), true
                    )
                }
            }
        }
    }

//    val productsDirectory = File(rootDir, "products").apply { mkdir() }
//
//    afterEvaluate {
//        applicationVariants.forEach { variant ->
//            val variantDirectory: File = File(productsDirectory, variant.name).apply { mkdir() }
//            variant.assembleProvider.orNull?.doLast {
//
//                // rename apk
//                val currentTimeString = SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().time)
//                val apkName =
//                    when (variant.buildType.name.toLowerCase()) {
//                        "release" -> "PhonographPlus_${variant.versionName}.apk"
//                        else -> "PhonographPlus_${variant.versionName}_${getGitHash(true)}_$currentTimeString.apk"
//                    }
//                variant.outputs.all {
//                    this.outputFile.copyTo(File(variantDirectory, apkName), true)
//                }
//                // copy shrink output file if available
//                tasks.filterIsInstance<R8Task>()
//                    .firstOrNull { it.name.contains(variant.name, ignoreCase = true) && it.enabled }
//                    ?.also {
//                        it.mappingFile.asFile.orNull
//                            ?.copyTo(File(variantDirectory, "mapping_${getGitHash(true)}.txt"))
//                    }
//            }
//        }
//    }

    lint {
        abortOnError = false
        disable.add("MissingTranslation")
        disable.add("InvalidPackage")

        checkReleaseBuilds = false
    }

    /*
    afterEvaluate {
        tasks.withType(JavaCompile::class) {
            options.compilerArgs.add(" -Xlint:deprecation")
            options.compilerArgs.add(" -Xlint:unchecked")
        }
    }
    */
}

@Suppress("SpellCheckingInspection")
dependencies {

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")

    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.media:media:1.5.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.percentlayout:percentlayout:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.annotation:annotation:1.3.0")
    implementation("com.google.android.material:material:1.4.0")

    implementation("com.github.chr56:mdUtil:0.0.1")
    implementation("com.github.chr56:mdColor:0.0.1")

    implementation("com.github.kabouzeid:RecyclerView-FastScroll:1.0.16-kmod")
    implementation("com.github.chr56:SeekArc:c5ae37866e")
    implementation("com.github.kabouzeid:AndroidSlidingUpPanel:6")
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    implementation("com.afollestad.material-dialogs:input:3.3.0")
    implementation("com.afollestad.material-dialogs:color:3.3.0")
    implementation("com.afollestad.material-dialogs:files:3.3.0")
    implementation("com.afollestad:material-cab:2.0.1")

    implementation("com.github.ksoichiro:android-observablescrollview:1.6.0")
    implementation("com.heinrichreimersoftware:material-intro:2.0.0")
    implementation("com.h6ah4i.android.widget.advrecyclerview:advrecyclerview:1.0.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.5.0")
    implementation("com.google.code.gson:gson:2.8.6")

    implementation("de.psdev.licensesdialog:licensesdialog:2.1.0")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.12.0")
    implementation("org.eclipse.mylyn.github:org.eclipse.egit.github.core:2.1.5")

    implementation("com.github.AdrienPoupa:jaudiotagger:2.2.3")
}
