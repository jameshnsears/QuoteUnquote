package com.github.jameshnsears.quoteunquote.cloud;

import android.content.Context;

import com.github.jameshnsears.quoteunquote.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.Preferences;
import com.google.gson.Gson;
import com.microsoft.appcenter.Flags;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ConcurrentHashMap;


public class CloudFavouritesHelper {
    private static String localCode;

    public static void setSharedPreferenceLocalCode(final Context context) {
        final Preferences preferences = new Preferences(0, context);
        preferences.setSharedPreferenceLocalCode(CloudFavouritesHelper.getLocalCode());
    }

    public static String getSharedPreferenceLocalCode(final Context context) {
        final Preferences preferences = new Preferences(0, context);
        String localCode = preferences.getSharedPreferenceLocalCode();

        if ("".equals(localCode)) {
            preferences.setSharedPreferenceLocalCode(CloudFavouritesHelper.getLocalCode());
            localCode = preferences.getSharedPreferenceLocalCode();
        }

        return localCode;
    }

    public static void auditFavourites(final String auditEvent, final String code) {
        final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
        properties.put("code", code);
        AuditEventHelper.auditAppCenter(auditEvent, properties, Flags.NORMAL);
    }

    public static synchronized String getLocalCode() {
        if (localCode == null) {
            final String rootCode = RandomStringUtils.randomAlphanumeric(8);
            final String crc = new String(Hex.encodeHex(DigestUtils.md5(rootCode)));
            localCode = rootCode + crc.substring(0, 2);
        }
        return localCode;
    }

    public static boolean isRemoteCodeValid(final String remoteCode) {
        final String rootCode = remoteCode.substring(0, 8);
        final String expectedCRC = new String(Hex.encodeHex(DigestUtils.md5(rootCode)));
        return remoteCode.endsWith(expectedCRC.substring(0, 2));
    }

    public static String receiveRequest(final String remoteCode) {
        final ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.code = remoteCode;
        final Gson gson = new Gson();
        return gson.toJson(receiveRequest);
    }
}
