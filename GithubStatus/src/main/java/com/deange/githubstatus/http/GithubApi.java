package com.deange.githubstatus.http;

import android.content.Context;
import android.os.AsyncTask;

import com.deange.githubstatus.model.Status;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class GithubApi {

    // URL ENDPOINTS
    public static final String BASE_URL = "https://status.github.com";
    public static final String BASE_API_URL = BASE_URL + "/api";
    public static final String JSON = ".json";

    public static final String STATUS = "/status" + JSON;
    public static final String LAST_MESSAGE = "/last-message" + JSON;
    public static final String LAST_MESSAGES = "/messages" + JSON;


    // STATUS CODES (must be lowercase!)
    public static final String STATUS_UNAVAILABLE = "unavailable";
    public static final String STATUS_GOOD = "good";
    public static final String STATUS_MINOR = "minor";
    public static final String STATUS_MAJOR = "major";

    // HTTP METHODS
    public static void getStatus(final Context context, final HttpTask.Listener<Status> listener) {
        doApiGet(new GithubStatusApi(context), Status.class, GithubApi.LAST_MESSAGE, listener);
    }

    public static Status getStatus(final Context context) throws IOException {
        return doApiGet(new GithubStatusApi(context), Status.class, GithubApi.LAST_MESSAGE);
    }

    public static void getMessages(final Context context, final HttpTask.Listener<List<Status>> listener) {
        doApiGet(new GithubStatusMessagesApi(context), new TypeToken<List<Status>>(){}.getType(), GithubApi.LAST_MESSAGES, listener);
    }

    public static List<Status> getMessages(final Context context) throws IOException {
        return doApiGet(new GithubStatusMessagesApi(context), new TypeToken<List<Status>>(){}.getType(), GithubApi.LAST_MESSAGES);
    }

    private static <T, S> void doApiGet(final BaseApi<T, S> api, final Type clazz, final String url, final HttpTask.Listener<T> listener) {

        final AsyncTask<Void, Void, T> getTask = new AsyncTask<Void, Void, T>() {

            Exception ex = null;

            @Override
            public T doInBackground(final Void... params) {
                try {
                    return api.get(clazz, url);
                } catch (IOException e) {

                    ex = e;
                    return null;
                }
            }

            @Override
            public void onPostExecute(final T entity) {
                if (listener != null) {
                    listener.onGet(entity, ex);
                }
            }
        };

        getTask.execute();
    }

    private static <T, S> T doApiGet(final BaseApi<T, S> api, final Type clazz, final String url) throws IOException {
        return api.get(clazz, url);
    }

    private GithubApi() {
        // Uninstantiable
    }

}
