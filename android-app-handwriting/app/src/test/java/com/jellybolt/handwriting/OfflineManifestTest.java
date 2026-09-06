package com.jellybolt.handwriting;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
}
