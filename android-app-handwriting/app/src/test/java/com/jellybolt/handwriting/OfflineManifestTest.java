package com.jellybolt.handwriting;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.XmlResourceParser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParser;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class OfflineManifestTest {
    @Test public void packagedAppHasNoNetworkPermissionOrCloudBackup() throws Exception {
        Context context = RuntimeEnvironment.getApplication();
        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                PackageManager.GET_PERMISSIONS);
        assertTrue(info.requestedPermissions == null || info.requestedPermissions.length == 0);
        assertEquals(0, info.applicationInfo.flags & ApplicationInfo.FLAG_ALLOW_BACKUP);
    }

    @Test public void excludesEveryStorageDomainFromCloudAndDeviceTransfer() throws Exception {
        Context context = RuntimeEnvironment.getApplication();
        Set<String> exclusions = new HashSet<>();
        try (XmlResourceParser parser = context.getResources().getXml(R.xml.data_extraction_rules)) {
            String section = "";
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) continue;
                if (parser.getName().equals("cloud-backup") || parser.getName().equals("device-transfer")) {
                    section = parser.getName();
                }
                if (parser.getName().equals("exclude") && ".".equals(parser.getAttributeValue(null, "path"))) {
                    exclusions.add(section + ":" + parser.getAttributeValue(null, "domain"));
                }
            }
        }
        for (String section : new String[]{"cloud-backup", "device-transfer"}) {
            for (String domain : new String[]{"root", "file", "database", "sharedpref", "external"}) {
                assertTrue(section + ":" + domain, exclusions.contains(section + ":" + domain));
            }
        }
    }

    @Test public void keyboardServiceCanOnlyBeBoundByAndroidAndIsNotDefault() throws Exception {
        Context context = RuntimeEnvironment.getApplication();
        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                PackageManager.GET_SERVICES | PackageManager.GET_META_DATA);
        ServiceInfo keyboard = null;
        for (ServiceInfo service : info.services) {
            if (service.name.equals("com.jellybolt.handwriting.HandwritingImeService")) keyboard = service;
        }
        assertNotNull(keyboard);
        assertTrue(keyboard.exported);
        assertEquals("android.permission.BIND_INPUT_METHOD", keyboard.permission);
        assertEquals(R.xml.input_method, keyboard.metaData.getInt("android.view.im"));
        boolean sawInputMethod = false;
        boolean sawAsciiKeyboard = false;
        try (XmlResourceParser parser = context.getResources().getXml(R.xml.input_method)) {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) continue;
                String android = "http://schemas.android.com/apk/res/android";
                if (parser.getName().equals("input-method")) {
                    sawInputMethod = true;
                    assertNull(parser.getAttributeValue(android, "isDefault"));
                }
                if (parser.getName().equals("subtype")) {
                    sawAsciiKeyboard = parser.getAttributeBooleanValue(android, "isAsciiCapable", false);
                }
            }
        }
        assertTrue(sawInputMethod);
        assertTrue(sawAsciiKeyboard);
    }
}
