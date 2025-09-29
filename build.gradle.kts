plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.github.leo_proger"
version = "1.0.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2025.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.github.leo_proger.tab_sorter"
        name = "Tab Sorter"
        version = "1.0.3"
        vendor {
            name = "Leo Proger"
            email = "leoproger11@gmail.com"
            url = "https://github.com/leo-proger"
        }

        ideaVersion {
            sinceBuild = "242"
        }

        description = """
            <h1>TabSorter</h1>

            <h2>Overview</h2>
            <p>Tired of JetBrains IDEs opening files wherever it wants? <strong>You can forget about that.</strong></p>
    
            <p>This plugin allows you to sort open tabs in the editor window.</p>
    
            <p>Put your workplace in order with this addon!</p>
    
            <p><strong>Available criteria for sorting</strong>:</p>
            <ul>
              <li>By name</li>
              <li>By extension</li>
              <li>By package</li>
            </ul>
            <p>That's all you need.</p>
            <p>Otherwise, I am glad to see the feedback about that you would like to add.</p>
        """.trimIndent()

        changeNotes = """
            Attempt to update description on JetBrains Marketplace one more time. I would appreciate your feedback.
        """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}