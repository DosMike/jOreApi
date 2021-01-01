# jOreApi

This project is implementing the API for the SpongePowered Ore Repository.

It is implemented in a way that allows multiple sessions over a single trafic limiter.

All you need as entry point to the api is the OreApiV2 class.

**This implementation currently
targets [https://staging-ore-vue.spongeproject.net/api/v2](https://staging-ore-vue.spongeproject.net/api)**

About the CompletableFutures you're getting with this API:
If you don't want to work with CompletionStages or don't want to handle the exeptions returned by get()
just use `api.method().join();` it will throw any exceptions as unchecked and just wait for the value.

In order to speed things up it also uses a cache of 5 Minutes for non-search type requests.

Entrypoint into the api:

```java
//You entry into the api:
OreApiV2 api=OreApiV2.builder()
		.setApiKey(/*use your api key if you're interested in more than public info*/)
		.setApplication("MyApplication/1.0 (by username) jOreApi/1.2 (by DosMike; Ore API V2)")
		.build();
```

Gradle Dependency:

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
dependencies {
    implementation 'com.github.DosMike:jOreApi:development-SNAPSHOT'
}
```