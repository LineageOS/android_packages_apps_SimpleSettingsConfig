/*
 * SPDX-FileCopyrightText: 2020 The Proton AOSP Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settingsconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import lineageos.providers.LineageSettings;

import java.io.File;
import java.io.IOException;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "SimpleSettingsConfig";

    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> {
            Log.i(TAG, "Updating settings storage at boot");
            updateDefaultConfigs(context);
        }).start();
    }

    private void updateDefaultConfigs(Context context) {
        File firstBootDoneFile = new File(
                context.createDeviceProtectedStorageContext().getFilesDir() + "/.first_boot_done");
        if (!firstBootDoneFile.exists()) {
            updateConfig(context, R.array.configs_first_boot_base);
            updateConfig(context, R.array.configs_first_boot_device);

            try {
                firstBootDoneFile.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "Failed to create " + firstBootDoneFile.getPath(), e);
            }
        }

        updateConfig(context, R.array.configs_base);
        updateConfig(context, R.array.configs_device);
    }

    private void updateConfig(Context context, int configArray) {
        // Set current properties
        String[] rawProperties = context.getResources().getStringArray(configArray);
        for (String property : rawProperties) {
            // Format: namespace/key=value
            String[] kv = property.split("=");
            String fullKey = kv[0];
            String[] nsKey = fullKey.split("/");

            String namespace = nsKey[0];
            String key = nsKey[1];
            String value = "";
            if (kv.length > 1) {
                value = kv[1];
            }

            switch (namespace) {
                case "global":
                    Settings.Global.putString(context.getContentResolver(), key, value);
                    break;

                case "secure":
                    Settings.Secure.putString(context.getContentResolver(), key, value);
                    break;

                case "system":
                    Settings.System.putString(context.getContentResolver(), key, value);
                    break;

                case "lineage_global":
                    LineageSettings.Global.putString(context.getContentResolver(), key, value);
                    break;

                case "lineage_secure":
                    LineageSettings.Secure.putString(context.getContentResolver(), key, value);
                    break;

                case "lineage_system":
                    LineageSettings.System.putString(context.getContentResolver(), key, value);
                    break;

                default:
                    Log.e(TAG, "Namespace '" + namespace + "' is invalid.");
            }
        }
    }
}
