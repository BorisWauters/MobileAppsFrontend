package be.kul.app.adapters;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import be.kul.app.R;
import be.kul.app.room.model.AnswerEntity;
import be.kul.app.room.model.UserEntity;
import be.kul.app.room.viewmodels.UserEntityViewModel;

import java.util.List;
import java.util.Map;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.MyViewHolder> {

    private List<AnswerEntity> answerList;
    private Map<Integer, UserEntity> userMap;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, description;


        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);


        }
    }


    public AnswerAdapter(List<AnswerEntity>answerList, Map<Integer, UserEntity> userMap) {
        this.answerList = answerList;
        this.userMap = userMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.answer_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AnswerEntity answerEntity = answerList.get(position);
        holder.title.setText(answerEntity.getAnswerDescription());
        holder.description.setText("posted by: " + userMap.get(answerEntity.getUserId()).getUsername());
    }

    @Override
    public int getItemCount() {
        return answerList.size();
    }
}
