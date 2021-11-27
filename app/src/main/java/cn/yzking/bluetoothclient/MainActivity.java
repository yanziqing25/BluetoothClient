package cn.yzking.bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Set;
import java.util.UUID;

import cn.yzking.bluetoothclient.adapter.StudentAdapter;
import cn.yzking.bluetoothclient.databinding.ActivityMainBinding;
import cn.yzking.bluetoothclient.utils.Utils;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler;

    private MenuItem bluetoothToolbarItem;

    private StudentAdapter studentAdapter;

    private final static String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private final static String CHARACTERISTIC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private final static String DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private final static int MESSAGE_UPDATE_ICON = 0;
    private final static int MESSAGE_UPDATE_BUTTON = 1;
    private final static int MESSAGE_RECEIVE = 2;
    private BluetoothGattCharacteristic mTransmissionCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        // 启动动态主题
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

        // 注册广播
        BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 蓝牙状态改变
                if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    if (state == BluetoothAdapter.STATE_ON) {
                        bluetoothToolbarItem.setIcon(R.drawable.ic_bluetooth_on);
                    } else if (state == BluetoothAdapter.STATE_OFF) {
                        bluetoothToolbarItem.setIcon(R.drawable.ic_bluetooth_off);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(bluetoothStateReceiver, intentFilter);

        // 初始化Handler
        this.mHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MESSAGE_UPDATE_ICON:
                        int status = (int) msg.obj;
                        if (status == BluetoothProfile.STATE_CONNECTED) {
                            bluetoothToolbarItem.setIcon(R.drawable.ic_bluetooth_link);
                        } else if (status == BluetoothProfile.STATE_DISCONNECTED) {
                            mBluetoothGatt.close();
                            mBluetoothGatt = null;
                            binding.buttonGet.setEnabled(false);// 按钮不可用
                            if (mBluetoothAdapter.isEnabled()) {
                                bluetoothToolbarItem.setIcon(R.drawable.ic_bluetooth_on);
                            } else {
                                bluetoothToolbarItem.setIcon(R.drawable.ic_bluetooth_off);
                            }
                        }
                        break;
                    case MESSAGE_UPDATE_BUTTON:
                        binding.buttonGet.setEnabled(true);
                        break;
                    case MESSAGE_RECEIVE:
                        for (Student student : (Set<Student>) msg.obj) {
                            studentAdapter.addStudent(student);
                        }
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.buttonGet.setEnabled(true);
                        break;
                }
            }
        };

        // 初始化TopAppBar
        Toolbar topAppBar = this.binding.topAppBar;
        this.bluetoothToolbarItem = topAppBar.getMenu().getItem(0);
        this.bluetoothToolbarItem.setIcon(R.drawable.ic_bluetooth_off);
        topAppBar.setOnMenuItemClickListener(item -> {
            if (this.mBluetoothGatt == null) {
                Intent intent = new Intent(this, BluetoothListActivity.class);
                startActivityForResult(intent, 2);  //设置返回宏定义
            } else {
                this.mBluetoothGatt.disconnect();
            }
            return true;
        });

        // 初始化组件
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = bluetoothManager.getAdapter();
        if (this.mBluetoothAdapter == null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("错误")
                    .setIcon(R.drawable.ic_error)
                    .setMessage("本设备不支持蓝牙!")
                    .setNegativeButton("退出", null)
                    .setOnDismissListener(dialog -> finish())
                    .show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        } else {
            this.bluetoothToolbarItem.setIcon(R.drawable.ic_bluetooth_on);
        }

        this.binding.buttonGet.setEnabled(false);
        this.binding.buttonGet.setOnClickListener(v -> {
            this.binding.buttonGet.setEnabled(false);
            this.binding.progressBar.setVisibility(View.VISIBLE);
            // Send
            this.mTransmissionCharacteristic.setValue(Utils.getHexBytes("05"));// new byte[]{5}
            this.mBluetoothGatt.writeCharacteristic(this.mTransmissionCharacteristic);
        });

        RecyclerView recyclerView = this.binding.rvStudent;
        this.studentAdapter = new StudentAdapter();
        recyclerView.setAdapter(this.studentAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                BluetoothDevice bluetoothDevice = data.getParcelableExtra("device");
                this.mBluetoothGatt = bluetoothDevice.connectGatt(this, false, this.gattCallback);
            }
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();// 搜索服务
            }
            mHandler.obtainMessage(MESSAGE_UPDATE_ICON, newState).sendToTarget();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mTransmissionCharacteristic = gatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
                // 可发送
                if (gatt.setCharacteristicNotification(mTransmissionCharacteristic, true)) {
                    mHandler.obtainMessage(MESSAGE_UPDATE_BUTTON).sendToTarget();
                }
                // 可接收
                BluetoothGattDescriptor descriptor = mTransmissionCharacteristic.getDescriptor(UUID.fromString(DESCRIPTOR_UUID));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        //接收数据
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().toString().equals(CHARACTERISTIC_UUID)) {
                mHandler.obtainMessage(MESSAGE_RECEIVE, Utils.parseData(characteristic.getValue())).sendToTarget();
            }
        }
    };
}