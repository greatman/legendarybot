
package com.greatmancode.legendarybot.api.utils;

/**
 * A Stacktrace handler that does nothing with the given stacktrace
 */
public class NullStacktraceHandler  implements StacktraceHandler{
    @Override
    public void sendStacktrace(Throwable e, String... tags) {

    }
}
