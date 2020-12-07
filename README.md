# jOre

This project is implementing the API for the SpongePowered Ore Repository.

It is implemented in a way that allows multiple sessions over a single trafic limiter.

All you need as entry point to the api is the OreApiV2 class.

**This implementation currently targets [https://staging-ore-vue.spongeproject.net/api/v2](https://staging-ore-vue.spongeproject.net/api)**

About the CompletableFutures you're getting with this API:
If you don't want to work with CompletionStages or don't want to handle the exeptions returned by get()
just use `api.method().join();` it will throw any exceptions as unchecked and just wait for the value.

In order to speed things up it also uses a cache of 5 Minutes for non-search type requests.

Use this library:  
`TODO add jitpack dependency information`