package fyp.leungww.exsplit;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TravellerAdapter extends RecyclerView.Adapter<TravellerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<GraphUser> data;
    private Context context;

    public TravellerAdapter(Context context) {
        this.inflater=LayoutInflater.from(context);
        this.data=new ArrayList<>();
        this.context=context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.traveller_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        GraphUser user=data.get(position);
        holder.fb_profile_picture.setProfileId(user.getId());
        holder.fb_profile_username.setText(user.getName());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(GraphUser item) {
        data.add(item);
        notifyItemInserted(data.size()-1);
    }

    public void remove(GraphUser item) {
        int position = data.indexOf(item);
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void removeAll(){
        int sizeData = data.size();
        for (int position = 0; position < sizeData; position++) {
            data.remove(0);
            notifyItemRemoved(0);
        }
        notifyItemRangeChanged(0, data.size());
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        ProfilePictureView fb_profile_picture;
        TextView fb_profile_username;

        public MyViewHolder(View itemView) {
            super(itemView);
            fb_profile_picture= (ProfilePictureView) itemView.findViewById(R.id.fb_profile_picture);
            fb_profile_username= (TextView) itemView.findViewById(R.id.fb_profile_username);
        }
    }

}
