package cn.yzking.bluetoothclient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.yzking.bluetoothclient.R;
import cn.yzking.bluetoothclient.Student;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    private final List<Student> studentList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView id;
        private final TextView temperature;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.id = itemView.findViewById(R.id.text_id);
            this.temperature = itemView.findViewById(R.id.text_temperature);
        }
    }

    public StudentAdapter() {
        this.studentList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = this.studentList.get(position);
        holder.id.setText(student.getId());
        holder.temperature.setText(String.valueOf(student.getTemperature()));
    }

    @Override
    public int getItemCount() {
        return this.studentList.size();
    }

    public void addStudent(Student student) {
        if (!this.studentList.isEmpty()) {
            for (Student student2 : this.studentList) {
                if (student.getId().equals(student2.getId())) {
                    return;
                }
            }
        }
        this.studentList.add(student);
        this.notifyItemInserted(this.getItemCount());
    }
}
