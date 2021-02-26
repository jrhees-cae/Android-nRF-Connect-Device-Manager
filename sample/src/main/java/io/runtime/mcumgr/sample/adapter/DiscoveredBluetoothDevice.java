/*
 * Copyright (c) 2018, Nordic Semiconductor
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.runtime.mcumgr.sample.adapter;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import io.runtime.mcumgr.sample.viewmodel.DevicesLiveData;

public class DiscoveredBluetoothDevice implements Parcelable {
    private final BluetoothDevice device;
    private ScanResult lastScanResult;
    private String name;
    private String version;
    private String lock_model;
    private String lock_version;
    private int rssi;
    private int previousRssi;
    private int highestRssi = -128;

    public DiscoveredBluetoothDevice(final ScanResult scanResult) {
        device = scanResult.getDevice();
        update(scanResult);
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public String getAddress() {
        return device.getAddress();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getLockVersion() {
        return lock_version;
    }

    public String getLockModel() {
        return lock_model;
    }

    public int getRssi() {
        return rssi;
    }

    public ScanResult getScanResult() {
        return lastScanResult;
    }

    /**
     * Returns the highest recorded RSSI value during the scan.
     *
     * @return Highest RSSI value.
     */
    public int getHighestRssi() {
        return highestRssi;
    }

    /**
     * This method returns true if the RSSI range has changed. The RSSI range depends on drawable
     * levels from {@link io.runtime.mcumgr.sample.R.drawable#ic_rssi_bar}.
     *
     * @return true, if the RSSI range has changed.
     */
    /* package */ boolean hasRssiLevelChanged() {
        final int newLevel =
                rssi <= 10 ?
                        0 :
                        rssi <= 28 ?
                                1 :
                                rssi <= 45 ?
                                        2 :
                                        3;
        final int oldLevel =
                previousRssi <= 10 ?
                        0 :
                        previousRssi <= 28 ?
                                1 :
                                previousRssi <= 45 ?
                                        2 :
                                        3;
        return newLevel != oldLevel;
    }

    /**
     * Updates the device values based on the scan result.
     *
     * @param scanResult the new received scan result.
     */
    public void update(final ScanResult scanResult) {
        lastScanResult = scanResult;
        name = scanResult.getScanRecord() != null ?
                scanResult.getScanRecord().getDeviceName() : null;

        StringBuilder versionStr = new StringBuilder("");
        StringBuilder lockVersionStr = new StringBuilder("");
        StringBuilder lockModelStr = new StringBuilder("");
        if (DevicesLiveData.parseSvcData(scanResult.getScanRecord(), versionStr, lockVersionStr, lockModelStr )) {
            version = versionStr.toString();
            lock_model = lockModelStr.toString();
            lock_version = lockVersionStr.toString();
        }
        else {
            version = null;
            lock_model = null;
            lock_version = null;
        }

//        version = "3.2.1+555";
//        lock_model = "YRD256-CHIP";
//        lock_version = "1.49";

        previousRssi = rssi;
        rssi = scanResult.getRssi();
        if (highestRssi < rssi)
            highestRssi = rssi;
    }

    public boolean matches(final @NotNull ScanResult scanResult) {
        return device.getAddress().equals(scanResult.getDevice().getAddress());
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof DiscoveredBluetoothDevice) {
            final DiscoveredBluetoothDevice that = (DiscoveredBluetoothDevice) o;
            return device.getAddress().equals(that.device.getAddress());
        }
        return super.equals(o);
    }

    // Parcelable implementation

    private DiscoveredBluetoothDevice(final Parcel in) {
        device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        lastScanResult = in.readParcelable(ScanResult.class.getClassLoader());
        name = in.readString();
        version = in.readString();
        lock_model = in.readString();
        lock_version = in.readString();
        rssi = in.readInt();
        previousRssi = in.readInt();
        highestRssi = in.readInt();
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        parcel.writeParcelable(device, flags);
        parcel.writeParcelable(lastScanResult, flags);
        parcel.writeString(name);
        parcel.writeString(version);
        parcel.writeString(lock_model);
        parcel.writeString(lock_version);
        parcel.writeInt(rssi);
        parcel.writeInt(previousRssi);
        parcel.writeInt(highestRssi);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DiscoveredBluetoothDevice> CREATOR = new Creator<DiscoveredBluetoothDevice>() {
        @Override
        public DiscoveredBluetoothDevice createFromParcel(final Parcel source) {
            return new DiscoveredBluetoothDevice(source);
        }

        @Override
        public DiscoveredBluetoothDevice[] newArray(final int size) {
            return new DiscoveredBluetoothDevice[size];
        }
    };
}
