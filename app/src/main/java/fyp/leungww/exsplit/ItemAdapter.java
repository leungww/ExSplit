package fyp.leungww.exsplit;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Item> data;
    private Context context;
    private ClickListener myClickListener;
    private TravellerDBAdapter travellerDBAdapter;

    public ItemAdapter(Context context) {
        this.inflater=LayoutInflater.from(context);
        this.data=new ArrayList<>();
        this.context=context;
        this.travellerDBAdapter = new TravellerDBAdapter(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.item_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Item item = data.get(position);
        holder.item_name.setText(item.getName());
        holder.item_currency.setText(item.getCurrencyCodeSymbol());
        holder.item_price.setText(item.getPrice()+"");
        holder.item_split_way.setText(item.getWaySplit());
        holder.item_amounts.setText(item.getAmounts_string());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(Item item) {
        data.add(item);
        notifyItemInserted(data.size()-1);
    }

    public void set(int position, Item item){
        data.set(position, item);
        notifyItemChanged(position);
    }

    public void remove(int position){
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void setMyClickListener(ClickListener myClickListener){
        this.myClickListener=myClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView item_name, item_currency, item_price, item_split_way, item_amounts;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_name= (TextView) itemView.findViewById(R.id.item_name);
            item_currency= (TextView) itemView.findViewById(R.id.item_currency);
            item_price= (TextView) itemView.findViewById(R.id.item_price);
            item_split_way= (TextView) itemView.findViewById(R.id.item_split_way);
            item_amounts= (TextView) itemView.findViewById(R.id.item_amounts);
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
