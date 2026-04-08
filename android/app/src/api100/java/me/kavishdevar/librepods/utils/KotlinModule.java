package me.kavishdevar.librepods.utils;

import android.annotation.SuppressLint;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam;
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;
import io.github.libxposed.api.annotations.AfterInvocation;
import io.github.libxposed.api.annotations.XposedHooker;

@SuppressLint({"DiscouragedApi", "PrivateApi"})
public class KotlinModule extends XposedModule {
    private final ModuleHookCore hookCore = new ModuleHookCore();
    private final Api100HookRegistrar hookRegistrar = new Api100HookRegistrar(this);

    public KotlinModule(XposedInterface base, ModuleLoadedParam param) {
        super(base, param);
        hookCore.onModuleLoaded(param.getProcessName());
    }

    @Override
    public void onPackageLoaded(PackageLoadedParam param) {
        super.onPackageLoaded(param);
        hookCore.onPackageLoaded(param.getPackageName(), param.isFirstPackage());
        hookCore.onPackageReady(param.getPackageName(), param.getClassLoader(), hookRegistrar);
    }

    private static final class Api100HookRegistrar implements ModuleHookCore.HookRegistrar {
        private final XposedModule module;

        private Api100HookRegistrar(XposedModule module) {
            this.module = module;
        }

        @Override
        public void hookAfter(Method method, ModuleHookCore.AfterHookHandler handler) {
            if (Api100HookDispatcher.register(method, handler)) {
                module.hook(method, Api100HookDispatcher.class);
            }
        }
    }

    @XposedHooker
    public static final class Api100HookDispatcher implements XposedInterface.Hooker {
        private static final ConcurrentMap<Method, ModuleHookCore.AfterHookHandler> AFTER_HANDLERS =
                new ConcurrentHashMap<>();

        private Api100HookDispatcher() {
        }

        private static boolean register(Method method, ModuleHookCore.AfterHookHandler handler) {
            return AFTER_HANDLERS.putIfAbsent(method, handler) == null;
        }

        @AfterInvocation
        public static void afterHookedMethod(XposedInterface.AfterHookCallback callback) {
            if (!(callback.getMember() instanceof Method)) {
                return;
            }

            Method method = (Method) callback.getMember();
            ModuleHookCore.AfterHookHandler handler = AFTER_HANDLERS.get(method);
            if (handler == null) {
                return;
            }

            handler.onAfter(callback.getThisObject(), Arrays.asList(callback.getArgs()));
        }
    }
}
