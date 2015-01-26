package fyp.leungww.exsplit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<DrawerInfo> data= Collections.emptyList();
    private Context context;
    private ClickListener myClickListener;

    public DrawerAdapter(Context context, List<DrawerInfo> data) {
        this.inflater=LayoutInflater.from(context);
        this.data=data;
        this.context=context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.drawer_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DrawerInfo myDrawerInfo=data.get(position);
        holder.title.setText(myDrawerInfo.getTitle());
        holder.icon.setImageResource(myDrawerInfo.getIconId());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setMyClickListener(ClickListener myClickListener){
        this.myClickListener=myClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title;
        ImageView icon;

        public MyViewHolder(View itemView) {
            super(itemView);
            title= (TextView) itemView.findViewById(R.id.drawer_title);
            icon= (ImageView) itemView.findViewById(R.id.drawer_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(myClickListener!=null){
                myClickListener.itemClicked(v,getPosition());
            }
        }
    }

    public interface ClickListener{
        public void itemClicked(View view, int position);
    }
}
