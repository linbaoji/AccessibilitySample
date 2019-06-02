package com.accessibility.card.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.accessibility.R;
import com.accessibility.card.carddb.model.Card;
import com.accessibility.utils.AccessibilityLog;
import com.accessibility.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

/**
 * Created by Pavneet_Singh on 12/20/17.
 */

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.BeanHolder> {

    private List<Card> list;
    private Context context;
    private LayoutInflater layoutInflater;
    private OnCardItemClick onCardItemClick;

    public CardsAdapter(List<Card> list, Context context) {
        layoutInflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
        this.onCardItemClick = (OnCardItemClick) context;
    }


    @Override
    public BeanHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.card_list_item, parent, false);
        return new BeanHolder(view);
    }

    @Override
    public void onBindViewHolder(BeanHolder holder, int position) {

        Card card = list.get(position);

        Log.e("bind", "onBindViewHolder: " + card);
        holder.textViewHolder.setText(card.getHolder());
        holder.textViewNumber.setText(card.getNumber());
        Glide.with(context).load(Constants.ALIPAY_API_BANK_LOGO_URL + card.getBank()).placeholder(R.drawable.ic_launcher_background).into(holder.imageLogo);


    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class BeanHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewHolder;
        TextView textViewNumber;
        ImageView imageLogo;

        public BeanHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            textViewHolder = itemView.findViewById(R.id.tv_holder);
            textViewNumber = itemView.findViewById(R.id.tv_number);
            imageLogo = itemView.findViewById(R.id.imag_logo);
        }

        @Override
        public void onClick(View view) {
            onCardItemClick.onCardClick(getAdapterPosition());
        }
    }

    public interface OnCardItemClick {
        void onCardClick(int pos);
    }
}