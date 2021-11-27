package cn.yzking.bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import cn.yzking.bluetoothclient.adapter.BluetoothDeviceAdapter;
import cn.yzking.bluetoothclient.databinding.ActivityBluetoothListBinding;

public class BluetoothListActivity extends AppCompatActivity {
    private ActivityBluetoothListBinding binding;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;

    private BluetoothDeviceAdapter mBluetoothDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityBluetoothListBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        // 设定默认返回值为取消
        setResult(Activity.RESULT_CANCELED);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = bluetoothManager.getAdapter();
        this.mScanning = false;

        RecyclerView recyclerView = this.binding.rvBluetooth;
        this.mBluetoothDeviceAdapter = new BluetoothDeviceAdapter();
        this.mBluetoothDeviceAdapter.setOnItemClickListener(bluetoothDevice -> {
            this.mBluetoothAdapter.getBluetoothLeScanner().stopScan(this.scanCallback);

            Intent intent = new Intent();
            intent.putExtra("device", bluetoothDevice);
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
        recyclerView.setAdapter(this.mBluetoothDeviceAdapter);

        Button buttonScan = this.binding.buttonScan;
        buttonScan.setOnClickListener(v -> {
            if (!this.mScanning) {
                this.mBluetoothDeviceAdapter.clearBluetoothDevice();

                this.mBluetoothAdapter.getBluetoothLeScanner().startScan(this.scanCallback);
                buttonScan.setText("扫描中......");
            } else {
                this.mBluetoothAdapter.getBluetoothLeScanner().stopScan(this.scanCallback);
                buttonScan.setText("开始扫描");
            }
            this.mScanning = !this.mScanning;
        });
    }

    // Device scan callback.
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice bluetoothDevice = result.getDevice();
            // Only recognize the device which has name.
            if (bluetoothDevice.getName() != null) mBluetoothDeviceAdapter.addBluetoothDevice(bluetoothDevice);
        }
    };
}