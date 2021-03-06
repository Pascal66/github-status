package com.deange.githubstatus.model;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;

import com.deange.githubstatus.R;
import com.deange.githubstatus.http.GithubApi;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Status {

    public static final String STATUS = "status";
    public static final String BODY = "body";
    public static final String CREATED_ON = "created_on";

    private static final Map<String, Integer> STATUS_MAP;
    static {
        final Map<String, Integer> map = new HashMap<>();
        map.put(GithubApi.STATUS_UNAVAILABLE, R.string.error_server_unavailable_status);
        map.put(GithubApi.STATUS_GOOD, R.string.status_good);
        map.put(GithubApi.STATUS_MINOR, R.string.status_minor);
        map.put(GithubApi.STATUS_MAJOR, R.string.status_major);
        STATUS_MAP = Collections.unmodifiableMap(map);
    }

    public enum SpecialType {
        ERROR, LOADING
    }

    @SerializedName(STATUS)
    private String mStatus;

    @SerializedName(BODY)
    private String mBody;

    @SerializedName(CREATED_ON)
    private String mCreatedOn;

    private boolean mSpecial = false;

    public static Status getSpecialStatus(final Context context, final SpecialType type) {

        final Time now = new Time();
        now.setToNow();

        final Status specialStatus = new Status();
        specialStatus.mCreatedOn = now.format3339(false);
        specialStatus.mSpecial = true;

        switch (type) {

            case ERROR:
                specialStatus.mStatus = context.getString(R.string.error_server_unavailable_status);
                specialStatus.mBody = context.getString(R.string.error_server_unavailable_message);
                break;

            case LOADING:
                specialStatus.mStatus = context.getString(R.string.loading_status);
                specialStatus.mBody = context.getString(R.string.loading_message);
                break;

        }

        return specialStatus;
    }

    public Status() {
    }

    public boolean isSpecialStatus() {
        return mSpecial;
    }

    public String getStatus() {
        return mStatus == null ? "" : mStatus;
    }

    public String getBody() {
        return mBody == null ? "" : mBody;
    }

    public Level getLevel() {
        return Level.from(getStatus());
    }

    public static String getTranslatedStatus(final Context context, final Status status) {

        final String translatedStatus;

        if (status != null && status.getStatus() != null) {
            final String key = status.getStatus().toLowerCase();
            if (!STATUS_MAP.containsKey(key)) {
                // Fallback to default string
                translatedStatus = key;

            } else {
                final Integer statusResId = STATUS_MAP.get(key);
                translatedStatus = context.getString(statusResId);
            }

        } else {

            if (context != null) {
                translatedStatus = context.getString(R.string.error_server_unavailable_status);

            } else {
                translatedStatus = "Unavailable";
            }

        }

        return translatedStatus;
    }

    public Time getCreatedOn() {
        if (TextUtils.isEmpty(mCreatedOn)) {
            return null;

        } else {
            final Time time = new Time(Time.TIMEZONE_UTC);
            time.parse3339(mCreatedOn);
            time.switchTimezone(Time.getCurrentTimezone());
            return time;
        }
    }

    public static boolean shouldAlert(final Status oldStatus, final Status newStatus) {

        if (oldStatus == null) {
            // First request ever, no alert necessary
            return false;

        } else if (newStatus == null) {
            // Prevent a NullPointerException, but this *really* shouldn't happen
            return false;

        } else if (newStatus.getLevel().isHigherThan(Level.GOOD)) {
            // Any time time the status changes and it is above GOOD,
            // we should be alerting the change.
            // Clients may block it if the user decides to.
            return !Objects.equals(oldStatus, newStatus);

        } else {
            // Alert on a status level change
            return oldStatus.getLevel() != newStatus.getLevel();
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (super.equals(obj)) {
            return true;
        }

        if (!(obj instanceof Status)) {
            return false;
        }

        final Status status = (Status) obj;

        if (!Objects.equals(getBody(), status.getBody())) {
            return false;

        } else if (!Objects.equals(getStatus(), status.getStatus())) {
            return false;

        } else if (!Objects.equals(getLevel(), status.getLevel())) {
            return false;

        } else if (!Objects.equals(getCreatedOn(), status.getCreatedOn())) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Status{" +
                "mStatus='" + mStatus + '\'' +
                ", mBody='" + mBody + '\'' +
                ", mCreatedOn='" + mCreatedOn + '\'' +
                '}';
    }
}
