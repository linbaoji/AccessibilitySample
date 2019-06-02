package com.accessibility.card;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.accessibility.R;
import com.accessibility.card.carddb.CardDatabase;
import com.accessibility.card.carddb.model.Card;
import com.accessibility.card.model.BankCard;
import com.accessibility.utils.BankUtil;
import com.accessibility.utils.ValidateUtil;
import com.accessibility.watcher.BankCardTextWatcher;

import java.lang.ref.WeakReference;
import java.util.Date;

public class AddCardActivity extends AppCompatActivity {

    private TextInputEditText et_holder, et_number, et_password, et_payment, et_poundage;
    private TextInputLayout ti_number, ti_holder, ti_password, ti_payment, ti_poundage, ti_signature;
    private String strSignature;
    private Button bt_signature;


    private CardDatabase cardDatabase;
    private Card card;

    private BankCard bankCard;

    private boolean update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        et_holder = findViewById(R.id.et_holder);
        et_number = findViewById(R.id.et_number);
        et_password = findViewById(R.id.et_password);
        et_payment = findViewById(R.id.et_payment);
        et_poundage = findViewById(R.id.et_poundage);


        ti_number = findViewById(R.id.ti_number);
        ti_holder = findViewById(R.id.ti_holder);
        ti_password = findViewById(R.id.ti_password);
        ti_payment = findViewById(R.id.ti_payment);
        ti_poundage = findViewById(R.id.ti_poundage);
        ti_signature = findViewById(R.id.ti_signature);

        bt_signature = findViewById(R.id.bt_signature);

        cardDatabase = CardDatabase.getInstance(AddCardActivity.this);
        Button button = findViewById(R.id.but_save);


        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || "".equals(s.toString())) {
                    ti_payment.setError("请输入金额(整数)！");
                    ti_payment.setErrorEnabled(true);//当设置成false的时候 错误信息不显示 反之显示

                } else if (!ValidateUtil.isLicheng(s.toString())) {
                    ti_payment.setError("请输入正确的金额！");
                    ti_payment.setErrorEnabled(true);//当设置成false的时候 错误信息不显示 反之显示

                } else {
                    ti_payment.setErrorEnabled(false);
                }
            }
        };
        //et_payment.addTextChangedListener(watcher);

        BankCardTextWatcher.bind(et_number);
        et_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s != null && s.length() > 0) {

                    BankUtil.getBanckNameByCard(AddCardActivity.this, s.toString(), new BankUtil.CallBack() {
                        @Override
                        public void getBankOk(BankCard bankCard) {

                            AddCardActivity.this.bankCard = bankCard;

                            if (card != null) {
                                card.setBank(bankCard.getBank());
                                card.setCardType(bankCard.getCardType());
                            }

                            if (bankCard != null) {
                                ti_number.setHint(bankCard.getBankName() + "(" + BankUtil.CardTypeEnum.valueOf(bankCard.getCardType()).backName + ")");
                            } else {
                                ti_number.setHint("银行卡号");
                            }

                        }
                    });

                }


            }
        });
//        textinput.setHint("请输入用户名");


        if ((card = (Card) getIntent().getSerializableExtra("card")) != null) {
            getSupportActionBar().setTitle("修改卡信息");
            update = true;
            button.setText("保存");

            et_holder.setText(card.getHolder());
            et_number.setText(card.getNumber());
            et_password.setText(card.getPassword());
            et_payment.setText(String.format("%d", card.getPayment()));
            et_poundage.setText(String.format("%.1f", card.getPoundage()));

            strSignature = card.getSignature();

        }


        bt_signature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(AddCardActivity.this, SignatureViewActivity.class), 100);
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = et_number.getText().toString();
                String holder = et_holder.getText().toString();
                String password = et_password.getText().toString();
                String payment = et_payment.getText().toString();
                String poundage = et_poundage.getText().toString();

                boolean checkOk = true;

                if (!ValidateUtil.validateluhm(number.trim().replace(" ", ""))) {

                    String msg = number == null || "".equals(number) ? "请输入正确的银行卡号" : "输入正确的银行卡号";
                    ti_number.setError(msg);
                    ti_number.setErrorEnabled(true);

                    checkOk = false;
                } else {
                    ti_number.setErrorEnabled(false);
                }
                if (holder == null || "".equals(holder) && holder.length() > 20) {
                    String msg = holder == null && "".equals(holder) ? "请输入持卡人姓名" : "请不要超出20个字符";
                    ti_holder.setError("请输入持卡人姓名！");
                    ti_holder.setErrorEnabled(true);//当设置成false的时候 错误信息不显示 反之显示
                    checkOk = false;
                } else {
                    ti_holder.setErrorEnabled(false);
                }
                if (!ValidateUtil.isBackCardPassword(password)) {
                    String msg = password == null || "".equals(password) ? "请输入行卡号密码" : "输入输入6位数字密码";
                    checkOk = false;
                    ti_password.setError(msg);
                    ti_password.setErrorEnabled(true);
                } else {
                    ti_password.setErrorEnabled(false);
                }
                if (!ValidateUtil.isMoney(payment)) {
                    String msg = payment == null || "".equals(payment) ? "请输入还款金额" : "输入还款正确的金额";
                    checkOk = false;
                    ti_payment.setError(msg);
                    ti_payment.setErrorEnabled(true);
                } else {
                    ti_payment.setErrorEnabled(false);
                }
                if (!ValidateUtil.isMoney(poundage)) {
                    String msg = poundage == null || "".equals(poundage) ? "请输入手续费" : "输入还款两位小数";
                    checkOk = false;
                    ti_poundage.setError(msg);
                    ti_poundage.setErrorEnabled(true);
                } else {
                    ti_poundage.setErrorEnabled(false);
                }

                if (strSignature == null || "".equals(strSignature)) {
                    ti_signature.setError("请录入卡主签名");
                    ti_signature.setErrorEnabled(true);
                    checkOk = false;
                } else {
                    ti_signature.setErrorEnabled(false);
                }

                if (!checkOk) {
                    return;
                } else if (update) {
                    card.setBank(bankCard != null ? bankCard.getBank() : null);
                    card.setBank(bankCard != null ? bankCard.getCardType() : null);
                    card.setNumber(number);
                    card.setHolder(holder);
                    card.setPassword(password);
                    card.setPayment(Integer.parseInt(payment));
                    card.setPoundage(Float.parseFloat(poundage));
                    card.setSignature(strSignature);

                    cardDatabase.getCardDao().updateCard(card);

                    setResult(card, 2);
                } else {
                    card = new Card(number
                            , bankCard != null ? bankCard.getBank() : null
                            , bankCard != null ? bankCard.getCardType() : null
                            , holder
                            , password
                            , Integer.parseInt(payment)
                            , Float.parseFloat(poundage)
                            , strSignature
                            , new Date());


                    new InsertTask(AddCardActivity.this, card).execute();
                }
            }
        });
    }

    private void setResult(Card card, int flag) {
        setResult(flag, new Intent().putExtra("card", card));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode > 0) {
            if (resultCode == 1) {
                strSignature = data.getStringExtra("pointList");
            }

        }
    }


    private static class InsertTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<AddCardActivity> activityReference;
        private Card card;

        // only retain a weak reference to the activity
        InsertTask(AddCardActivity context, Card card) {
            activityReference = new WeakReference<>(context);
            this.card = card;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            // retrieve auto incremented card id
            long j = activityReference.get().cardDatabase.getCardDao().insertCard(card);
            card.setCard_id(j);
            Log.e("ID ", "doInBackground: " + j);
            return true;
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool) {
                activityReference.get().setResult(card, 1);
                activityReference.get().finish();
            }
        }
    }


}
