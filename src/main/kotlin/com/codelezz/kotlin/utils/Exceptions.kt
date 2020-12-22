@file:JvmName("Errors")
@file:Suppress("unused")
package com.codelezz.kotlin.utils

/**
 * Catch errors.
 * Like try catch only errors that are other than the T type will be reported to google-cloud.
 */
inline fun <reified T : Throwable, reified U : Any> catch(
    err: (T) -> U,
    run: () -> U
): U = try {
    run()
} catch (ex: Throwable) {
    if (ex is T) err(ex) else {
        throw ex
    }
}

/**
 * Catch errors.
 * Like try catch only errors will be reported to google-cloud.
 */
inline fun <reified T : Throwable> catch(
    err: (T) -> Unit = { it.printStackTrace(); },
    run: () -> Unit
): Unit = catch<T, Unit>(err, run)

/**
 * Catch errors.
 * Like try catch only errors that are other than the T type will be reported to google-cloud.
 * Returns the value from the run or the default value if the exception has been thrown.
 */
inline fun <reified T : Throwable, reified U : Any> catch(
    default: U,
    run: () -> U
): U = catch<T, U>({ default }, run)
