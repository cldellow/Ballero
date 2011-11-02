package com.cldellow.ballero.service;

import android.os.AsyncTask;

/**
 * Temporary workaround to solve a Scala compiler issue which shows up
 * at runtime with the error message
 * "java.lang.AbstractMethodError: abstract method not implemented"
 * for the missing method LookupTask.doInBackground(String... args).
 *
 * Our solution: the Java method doInBackground(String... args) forwards
 * the call to the Scala method doInBackground1(String[] args).
 */
public abstract class JsonParseTaskBase extends AsyncTask<JsonParseRequest<?>, Void, JsonParseResult<?>> {
    protected abstract JsonParseResult<?> doInBackground1(JsonParseRequest<?>[] args);

    @Override
    protected JsonParseResult<?> doInBackground(JsonParseRequest<?>... args) {
        JsonParseRequest[] args1 = new JsonParseRequest[args.length];
        for (int i = 0; i < args.length; i++) {
            args1[i] = args[i];
        }
        return doInBackground1(args1);
    }

}

