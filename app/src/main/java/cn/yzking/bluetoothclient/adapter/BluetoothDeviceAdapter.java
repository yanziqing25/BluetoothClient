package cn.yzking.bluetoothclient.adapter;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.yzking.bluetoothclient.R;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {
    private final List<BluetoothDevice> mBluetoothDeviceList;
    private OnItemClickListener mListener;

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView address;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.text_name);
            this.address = itemView.findViewById(R.id.text_address);
        }
    }

    public interface OnItemClickListener {
        void onClick(BluetoothDevice bluetoothDevice);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mListener = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice bluetoothDeviceList = this.mBluetoothDeviceList.get(position);
        holder.name.setText(bluetoothDeviceList.getName());
        holder.address.setText(bluetoothDeviceList.getAddress());
        holder.itemView.setOnClickListener(v -> {
            if (this.mListener != null) {
                this.mListener.onClick(this.mBluetoothDeviceList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBluetoothDeviceList.size();
    }

    public BluetoothDeviceAdapter() {
        this.mBluetoothDeviceList = new ArrayList<>();
    }

    public void addBluetoothDevice(BluetoothDevice bluetoothDevice) {
        if (!this.mBluetoothDeviceList.isEmpty()) {
            for (BluetoothDevice bluetoothDevice2 : this.mBluetoothDeviceList) {
                if (bluetoothDevice.getAddress().equals(bluetoothDevice2.getAddress())) {
                    return;
                }
            }
        }
        this.mBluetoothDeviceList.add(bluetoothDevice);
        this.notifyItemInserted(this.getItemCount());
    }

    public void clearBluetoothDevice() {
        this.mBluetoothDeviceList.clear();
        this.notifyDataSetChanged();
    }
}
