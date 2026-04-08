package me.kavishdevar.librepods.utils

import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.lang.reflect.Method

class KotlinModule : XposedModule() {
    private val hookCore = ModuleHookCore()
    private val hookRegistrar = Api101HookRegistrar()

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        super.onModuleLoaded(param)
        hookCore.onModuleLoaded(param.processName)
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        super.onPackageLoaded(param)
        hookCore.onPackageLoaded(param.packageName, param.isFirstPackage)
    }

    override fun onPackageReady(param: PackageReadyParam) {
        super.onPackageReady(param)
        hookCore.onPackageReady(param.packageName, param.classLoader, hookRegistrar)
    }

    private inner class Api101HookRegistrar : ModuleHookCore.HookRegistrar {
        override fun hookAfter(method: Method, handler: ModuleHookCore.AfterHookHandler) {
            hook(method).intercept { chain ->
                val result = chain.proceed()
                handler.onAfter(chain.thisObject, chain.args)
                result
            }
        }
    }
}
