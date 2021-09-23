# Droid-Rust
Droid-Rust is a starter project for Android NDK with Rust

[![Output](https://github.com/1ok1/droid-rust-starter/blob/main/droid-ndk-starter.png?raw=true)]()

> Step 1: Add the Path in Zshrc or bashrc
```
# Path to NDK
export ANDROID_HOME=$HOME/Library/Android/sdk
export NDK_HOME=$ANDROID_HOME/ndk-bundle
export PATH=$NDK_HOME/toolchains/llvm/prebuilt/darwin-x86_64/bin:$PATH
export PATH="$HOME/.cargo/bin:$PATH"
```

> Step 2: Create a new Android Studio Project, For people who are using Gradle 7.0.0+ Java 11 has been made mandatory either upgrade your system JAVA or install newer version of JAVA in paraller and add the PATH in gradle.properties
```
    org.gradle.java.home=/Library/Java/JavaVirtualMachines/adoptopenjdk-11.jdk/Contents/Home
```
Followed by setting the Gradle JDK to JAVA 11
[![Output](https://github.com/1ok1/droid-rust-starter/blob/main/jvm-settings.png?raw=true)]()
> Step 3: Install rust in your system.
```
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

> Step4: Verify rust installation
```
   rustc --version
```

> Step 5: Setup a rust project
```
cargo new <rust> // Rust is the project name
```
>Step 6: Add the below rust ndk config to Cargo.toml
```
[package]
name = "rust"
version = "0.1.0"
edition = "2018"

[target.'cfg(target_os="android")'.dependencies]
jni = { version = "0.5", default-features = false }
# See more keys and their definitions at https://doc.rust-lang.org/cargo/reference/manifest.html
[lib]
name = "rust"
crate-type = ["dylib"]

[dependencies]
// To Configure Rust libs
```

> Step 7: Intergrate the Rust project as NDK in Android Project
```
In root build.gradle add
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath 'org.mozilla.rust-android-gradle:plugin:0.9.0'
    }
}
```

> Step 8: In App build.gralde add Rust
```
plugins {
    id 'org.mozilla.rust-android-gradle.rust-android'
}
```

> Step 9: cargo gradle configuration
```
  cargo {
    module  = "../rust"       // directory containing your Cargo.toml
    libname = "rust"          // Cargo.toml's [package] name.
    targets = ['arm', 'arm64', 'x86', 'x86_64']
    prebuiltToolchains = true
    verbose = true
}
```

> Step 10: Install the rust toolchains for your platforms
```
rustup target add armv7-linux-androideabi   # for arm
rustup target add i686-linux-android        # for x86
rustup target add aarch64-linux-android     # for arm64
rustup target add x86_64-linux-android      # for x86_64
rustup target add x86_64-unknown-linux-gnu  # for linux-x86-64
rustup target add x86_64-apple-darwin       # for darwin (macOS)
rustup target add x86_64-pc-windows-gnu     # for win32-x86-64-gnu
rustup target add x86_64-pc-windows-msvc    # for win32-x86-64-msvc
```

> Step 11: To generate the .so files with gradle,
```
./gradlew cargoBuild

or

// Added the compile stage for both debug and release of JAVA and KOTLIN
tasks.whenTaskAdded { task ->
    println(task.name)
    if ((task.name == 'javaPreCompileDebug' || task.name == 'javaPreCompileRelease'
            || task.name == 'compileDebugKotlin' || task.name == 'compileReleaseKotlin')) {
        task.dependsOn 'cargoBuild'
    }
}
```

> Step 12: Create an Android Application class and configure in <application> tag of your manifest file, then add System.loadLibrary(LIBRARY_NAME) in the respective ways
```
For JAVA:
 In onCreate of Application class    
        System.loadLibrary(LIBRARY_NAME)

For Kotlin:
    companion object {
        private val LIBRARY_NAME = "rust"

        init {
            print(loadNativeLibrary())
        }

        fun loadNativeLibrary(): Boolean {
            try {
                Log.i(TAG, "Attempting to load library: $LIBRARY_NAME")
                System.loadLibrary(LIBRARY_NAME)
            } catch (e: Exception) {
                Log.i(TAG, "Exception loading native library: $e")
                return false
            }
            return true
        }
    }
```

Step 13: Rename the main.rs to lib.rs
```
use std::os::raw::{c_char};
use std::ffi::{CString, CStr};


/// Expose the JNI interface for android below
#[cfg(target_os="android")]
#[allow(non_snake_case)]
pub mod android {
    extern crate jni;

    use super::*;
    use self::jni::JNIEnv;
    use self::jni::objects::{JClass, JString};
    use self::jni::sys::{jstring};

    #[no_mangle]
    pub unsafe extern fn Java_com_lok1_rustndkexample_Greetings_greeting(env: JNIEnv, _: JClass, java_pattern: JString) -> jstring {
        let world = rust_greeting(env.get_string(java_pattern).expect("invalid pattern string").as_ptr());
        let world_ptr = CString::from_raw(world);
        let output = env.new_string(world_ptr.to_str().unwrap()).expect("Couldn't create java string!");
        output.into_inner()
    }

    #[no_mangle]
    pub unsafe extern fn Java_Greetings_welcome(env: JNIEnv, _: JClass, java_pattern: JString) -> jstring {
        let world = rust_greeting(env.get_string(java_pattern).expect("invalid pattern string").as_ptr());
        let world_ptr = CString::from_raw(world);
        let output = env.new_string(world_ptr.to_str().unwrap()).expect("Couldn't create java string!");
        output.into_inner()
    }

    #[no_mangle]
    pub extern fn rust_greeting(to: *const c_char) -> *mut c_char {
        let c_str = unsafe { CStr::from_ptr(to) };
        let recipient = match c_str.to_str() {
            Err(_) => "there",
            Ok(string) => string,
        };
        CString::new("Hello There - ".to_owned() + recipient).unwrap().into_raw()
    }
}
```

Step 14: For setup of rust function in JAVA or Kotlin
```
For JAVA:
public class Greetings {
    private static native String greeting(final String pattern);

    public String sayHello(String to) {
        return greeting(to);
    }
}
For Kotlin:
/** This file is used as a namespace for all the exported Rust functions. */
@file:JvmName("Greetings")

external fun welcome(pattern: String): String?
```

Step 15: To call the function in Activity or fragment
```
val g = Greetings()
val r: String = g.sayHello("Loki")
binding.iclContentMain.textView.text = welcome(" Lokesh \n") + r
```

After Integration your result will be like the image shown in the beginning.