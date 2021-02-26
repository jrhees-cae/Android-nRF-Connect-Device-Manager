/*
 * Copyright (c) 2018, Nordic Semiconductor
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.sample.viewmodel;

import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.lifecycle.LiveData;
import io.runtime.mcumgr.ble.McuMgrBleTransport;
import io.runtime.mcumgr.sample.adapter.DiscoveredBluetoothDevice;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * This class keeps the current list of discovered Bluetooth LE devices matching filter.
 * Each time @{link {@link #applyFilter()} is called, the observers are notified with a new
 * list instance.
 */
@SuppressWarnings("unused")
public class DevicesLiveData extends LiveData<List<DiscoveredBluetoothDevice>> {
//    private static final ParcelUuid FILTER_UUID = new ParcelUuid(McuMgrBleTransport.SMP_SERVICE_UUID);
    // August 16-bit service UUID == 0xFE24
    public final static UUID AUGUST_SERVICE_UUID =
        UUID.fromString("0000fe24-0000-1000-8000-00805f9b34fb");

    public static final ParcelUuid FILTER_UUID = new ParcelUuid(AUGUST_SERVICE_UUID);
    public static final int AUGUST_SVC_DATA_MAGIC = 0x5A;

    private static final int FILTER_RSSI = -50; // [dBm]

    private final List<DiscoveredBluetoothDevice> mDevices = new ArrayList<>();
    private List<DiscoveredBluetoothDevice> mFilteredDevices = null;
    private boolean mFilterUuidRequired;
    private boolean mFilterNearbyOnly;

    /* package */ DevicesLiveData(final boolean filterUuidRequired, final boolean filterNearbyOnly) {
        mFilterUuidRequired = filterUuidRequired;
        mFilterNearbyOnly = filterNearbyOnly;
    }

    /* package */
    synchronized void bluetoothDisabled() {
        mDevices.clear();
        mFilteredDevices = null;
        postValue(null);
    }

    /* package */  boolean filterByUuid(final boolean uuidRequired) {
        mFilterUuidRequired = uuidRequired;
        return applyFilter();
    }

    /* package */  boolean filterByDistance(final boolean nearbyOnly) {
        mFilterNearbyOnly = nearbyOnly;
        return applyFilter();
    }

    /* package */
    synchronized boolean deviceDiscovered(final ScanResult result) {
        DiscoveredBluetoothDevice device;

        // Check if it's a new device.
        final int index = indexOf(result);
        if (index == -1) {
            device = new DiscoveredBluetoothDevice(result);
            mDevices.add(device);
        } else {
            device = mDevices.get(index);
        }

        // Update RSSI and name.
        device.update(result);

        // Return true if the device was on the filtered list or is to be added.
        return (mFilteredDevices != null && mFilteredDevices.contains(device))
                || (matchesUuidFilter(result) && matchesNearbyFilter(device.getHighestRssi()));
    }

    /**
     * Clears the list of devices.
     */
    public synchronized void clear() {
        mDevices.clear();
        mFilteredDevices = null;
        postValue(null);
    }

    /**
     * Refreshes the filtered device list based on the filter flags.
     */
    /* package */
    synchronized boolean applyFilter() {
        final List<DiscoveredBluetoothDevice> devices = new ArrayList<>();
        for (final DiscoveredBluetoothDevice device : mDevices) {
            final ScanResult result = device.getScanResult();
            if (matchesUuidFilter(result) && matchesNearbyFilter(device.getHighestRssi())) {
                devices.add(device);
            }
        }
        mFilteredDevices = devices;
        postValue(mFilteredDevices);
        return !mFilteredDevices.isEmpty();
    }

    /**
     * Finds the index of existing devices on the device list.
     *
     * @param result scan result.
     * @return Index of -1 if not found.
     */
    private int indexOf(final ScanResult result) {
        int i = 0;
        for (final DiscoveredBluetoothDevice device : mDevices) {
            if (device.matches(result))
                return i;
            i++;
        }
        return -1;
    }

    static private boolean isValidSvcData(final ScanRecord record) {
        if (record == null)
            return false;

        final byte[] svcData = record.getServiceData(FILTER_UUID);
        if (svcData == null)
            return false;

        if (svcData[0] != AUGUST_SVC_DATA_MAGIC)
            return false;

        if (svcData.length != 10)
            return false;

        return true;
    }

    static public boolean parseSvcData(final ScanRecord record, StringBuilder fw_version, StringBuilder lock_version, StringBuilder lock_model ) {
        if (!isValidSvcData(record))
            return false;

        final byte[] svcData = record.getServiceData(FILTER_UUID);

        fw_version.delete(0, fw_version.length());
        lock_version.delete(0, lock_version.length());
        lock_model.delete(0, lock_model.length());
        fw_version.append(String.format("%d.%d.%d", svcData[1] & 0xff, svcData[2] & 0xff, (svcData[3] & 0xff) | ((svcData[4] & 0xff) << 8)));
        if ((svcData[6] != 0) || (svcData[5] != 0)) {
            lock_version.append(String.format("%d.%d.%d", svcData[6] / 10, svcData[6] % 10, svcData[5]));
        }
        final int model = (svcData[7] & 0xff) | ((svcData[8] & 0xff) << 8);
        String modelStr;

        switch (model) {
            case 0x8004:
                modelStr = "YRD216";
                break;
            case 0x8002:
                modelStr = "YRD226/246/446";
                break;
            case 0x8005:
                modelStr = "PUSH-ZB/ZW";
                break;
            case 0x8006:
                modelStr = "CAP-ZB/ZW";
                break;
            case 0x8007:
                modelStr = "NTM-PB";
                break;
            case 0x8008:
                modelStr = "NTM-TS";
                break;
            case 0x8001:
                modelStr = "NTB-PB";
                break;
            case 0x8003:
                modelStr = "NTB-TS";
                break;
            case 0x8009:
                modelStr = "NTE-PB";
                break;
            case 0x800A:
                modelStr = "NTE-TS";
                break;
            case 0x800B:
                modelStr = "YRL-PB";
                break;
            case 0x800C:
                modelStr = "YRL-TS";
                break;
            case 0x800D:
                modelStr = "YRD610/630";
                break;
            case 0x800E:
                modelStr = "YRD620/430";
                break;
            case 0x8010:
                modelStr = "NTA610/630";
                break;
            case 0x8011:
                modelStr = "NTA615/635";
                break;
            case 0x8012:
                modelStr = "NTA620/640";
                break;
            case 0x8013:
                modelStr = "NTA625/645";
                break;
            case 0x8014:
                modelStr = "YRM276-TS";
                break;
            case 0x8015:
                modelStr = "YRM286-PB";
                break;
            case 0x8016:
                modelStr = "YRM476-TS";
                break;
            case 0x8017:
                modelStr = "YRV116-PB";
                break;
            case 0x8018:
                modelStr = "EMPPAD-TS";
                break;
            case 0x80FF:
                modelStr = "YRD540";
                break;
            default:
                modelStr = null;
                break;
        }

        if (modelStr != null)
            lock_model.append(modelStr);

        return true;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean matchesUuidFilter(final ScanResult result) {
        if (!mFilterUuidRequired)
            return true;

        final ScanRecord record = result.getScanRecord();

        return isValidSvcData (record);

//        final List<ParcelUuid> uuids = record.getServiceUuids();
//        if (uuids == null)
//            return false;
//
//        return uuids.contains(FILTER_UUID);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean matchesNearbyFilter(final int rssi) {
        if (!mFilterNearbyOnly)
            return true;

        return rssi >= FILTER_RSSI;
    }
}
