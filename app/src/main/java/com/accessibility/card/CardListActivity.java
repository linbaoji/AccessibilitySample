package com.accessibility.card;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.accessibility.OpenAccessibilitySettingHelper;
import com.accessibility.R;
import com.accessibility.card.adapter.CardsAdapter;
import com.accessibility.card.carddb.CardDatabase;
import com.accessibility.card.carddb.model.Card;
import com.accessibility.utils.Constants;
import com.alibaba.fastjson.JSONArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CardListActivity extends AppCompatActivity implements CardsAdapter.OnCardItemClick {

    private TextView textViewMsg;
    private RecyclerView recyclerView;
    private CardDatabase cardDatabase;
    private List<Card> cards;
    private CardsAdapter cardsAdapter;
    private int pos;


    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String actionDataKey;//action_data_key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeVies();
        displayList();


        //获取SharedPreferences对象
        sp = getSharedPreferences(Constants.SHARE_PREFERENCES_DATABASE_NAME, Activity.MODE_MULTI_PROCESS);
        // 获取Editor对象
        editor = sp.edit();

        actionDataKey = Constants.SHARE_PREFERENCES_DATA_KEY;

    }

    private void displayList() {
        cardDatabase = CardDatabase.getInstance(CardListActivity.this);
        new RetrieveTask(this).execute();
    }

    private static class RetrieveTask extends AsyncTask<Void, Void, List<Card>> {

        private WeakReference<CardListActivity> activityReference;

        // only retain a weak reference to the activity
        RetrieveTask(CardListActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<Card> doInBackground(Void... voids) {
            if (activityReference.get() != null)
                return activityReference.get().cardDatabase.getCardDao().getCards();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<Card> cards) {
            if (cards != null && cards.size() > 0) {
                activityReference.get().cards.clear();
                activityReference.get().cards.addAll(cards);
                // hides empty text view
                activityReference.get().textViewMsg.setVisibility(View.GONE);
                activityReference.get().cardsAdapter.notifyDataSetChanged();
            }
        }
    }

    private void initializeVies() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textViewMsg = (TextView) findViewById(R.id.tv__empty);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(listener);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(CardListActivity.this));
        cards = new ArrayList<>();
        cardsAdapter = new CardsAdapter(cards, CardListActivity.this);
        recyclerView.setAdapter(cardsAdapter);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivityForResult(new Intent(CardListActivity.this, AddCardActivity.class), 100);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode > 0) {
            if (resultCode == 1) {
                cards.add((Card) data.getSerializableExtra("card"));
            } else if (resultCode == 2) {
                cards.set(pos, (Card) data.getSerializableExtra("card"));
            }
            listVisibility();
        }
    }

    @Override
    public void onCardClick(final int pos) {
        new AlertDialog.Builder(CardListActivity.this)
                .setTitle("请选择操作")
                .setItems(new String[]{"删除", "编辑", "收款"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                removeCard();
                                break;
                            case 1:
                                CardListActivity.this.pos = pos;
                                startActivityForResult(
                                        new Intent(CardListActivity.this,
                                                AddCardActivity.class).putExtra("card", cards.get(pos)),
                                        100);

                                break;
                            case 2:
                                CardListActivity.this.pos = pos;
                                Card card = cards.get(pos);

                                String jsonString = JSONArray.toJSONString(card);

                                editor.remove(actionDataKey);
                                editor.putString(actionDataKey, jsonString);
                                editor.commit();

                                Intent intent = new Intent();
                                //cn.vbill.operations.ad.StartUpAdvertisementActivity
                                //cn.vbill.operations.MainPlusActivity
                                intent.setClassName("com.vbill.shoushua.biz", "cn.vbill.operations.xinaliance.ThirdPayActivity");
                                startActivity(intent);




                                break;
                        }
                    }
                }).show();

    }

    private void listVisibility() {
        int emptyMsgVisibility = View.GONE;
        if (cards.size() == 0) { // no item to display
            if (textViewMsg.getVisibility() == View.GONE)
                emptyMsgVisibility = View.VISIBLE;
        }
        textViewMsg.setVisibility(emptyMsgVisibility);
        cardsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        cardDatabase.cleanUp();
        super.onDestroy();
    }


    private void  removeCard(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CardListActivity.this);
        builder.setTitle("删除");
        builder.setMessage("您确认删除这张银行卡吗");
        builder.setIcon(R.mipmap.ic_launcher_round);
        //点击对话框以外的区域是否让对话框消失
        builder.setCancelable(true);
        //设置正面按钮
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                cardDatabase.getCardDao().deleteCard(cards.get(pos));
                cards.remove(pos);
                listVisibility();
                dialog.dismiss();
            }
        });
        //设置反面按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        //显示对话框
        dialog.show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()){
            case R.id.action_accessibility:
                OpenAccessibilitySettingHelper.jumpToSettingPage(this);
                break;

            default:
                break;
        }
//	       Toast.makeText(MainActivity.this, ""+item.getItemId(), Toast.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }

}
