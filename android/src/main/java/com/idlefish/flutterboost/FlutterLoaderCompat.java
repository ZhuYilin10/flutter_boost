package com.idlefish.flutterboost;

import java.lang.reflect.Method;

import io.flutter.embedding.engine.loader.FlutterLoader;

/**
 * Provides access to {@link FlutterLoader#findAppBundlePath()} across different embedding
 * generations. Newer Flutter SDKs expose a singleton on FlutterLoader, while older releases
 * still rely on FlutterInjector.
 */
final class FlutterLoaderCompat {

    private FlutterLoaderCompat() {
    }

    static String findAppBundlePath() {
        String path = findFromFlutterLoaderSingleton();
        if (path != null) {
            return path;
        }
        path = findFromFlutterInjector();
        if (path != null) {
            return path;
        }
        throw new IllegalStateException("Unable to locate Flutter app bundle path.");
    }

    private static String findFromFlutterLoaderSingleton() {
        try {
            Method getInstance = FlutterLoader.class.getDeclaredMethod("getInstance");
            Object loader = getInstance.invoke(null);
            Method findAppBundlePath = FlutterLoader.class.getDeclaredMethod("findAppBundlePath");
            return (String) findAppBundlePath.invoke(loader);
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get FlutterLoader instance.", e);
        }
    }

    private static String findFromFlutterInjector() {
        try {
            Class<?> injectorClass = Class.forName("io.flutter.embedding.engine.FlutterInjector");
            Method instanceMethod = injectorClass.getDeclaredMethod("instance");
            Object injector = instanceMethod.invoke(null);
            Method loaderAccessor = injectorClass.getDeclaredMethod("flutterLoader");
            Object loader = loaderAccessor.invoke(injector);
            Method findAppBundlePath = loader.getClass().getDeclaredMethod("findAppBundlePath");
            return (String) findAppBundlePath.invoke(loader);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return findFromLegacyFlutterInjector();
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve FlutterLoader from FlutterInjector.", e);
        }
    }

    private static String findFromLegacyFlutterInjector() {
        try {
            Class<?> injectorClass = Class.forName("io.flutter.FlutterInjector");
            Method instanceMethod = injectorClass.getDeclaredMethod("instance");
            Object injector = instanceMethod.invoke(null);
            Method loaderAccessor = injectorClass.getDeclaredMethod("flutterLoader");
            Object loader = loaderAccessor.invoke(injector);
            Method findAppBundlePath = loader.getClass().getDeclaredMethod("findAppBundlePath");
            return (String) findAppBundlePath.invoke(loader);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve FlutterLoader from legacy FlutterInjector.", e);
        }
    }
}
