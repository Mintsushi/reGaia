package com.example.round.gaia_18.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.round.gaia_18.Data.StoreProduct;
import com.example.round.gaia_18.R;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.round.gaia_18.Data.DataList.storeAdapter;
import static com.example.round.gaia_18.MainActivity.fruit;
import static com.example.round.gaia_18.MainActivity.seed;
import static com.example.round.gaia_18.OverlayService.dataList;

/**
 * Created by 리제 on 2017-09-29.
 */

public class StoreCheckDialog extends Dialog {
    public StoreCheckDialog(Context context){ super((context));}

    ImageView storeCheckImage;
    TextView storeCheckNameText;
    TextView storeCheckExplainText;

    TextView diaPreCostTypeText;
    TextView diaPreCostText;
    TextView diaUseCostTypeText;
    TextView diaUseCostText;

    ImageButton diaBuyYesButton;
    ImageButton diaBuyNoButton;

    StoreProduct storeProduct;

    public int costType;
    public int buySuccess;
    private ConcurrentHashMap<Integer, Integer> useCostHash = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> preCostHash = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> futCostHash = new ConcurrentHashMap<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.menu_store_check_dialog);

        storeCheckImage = (ImageView)findViewById(R.id.storeCheckImage);
        storeCheckNameText = (TextView)findViewById(R.id.storeCheckNameText);
        storeCheckExplainText = (TextView)findViewById(R.id.storeCheckExplainText);
        diaPreCostTypeText = (TextView)findViewById(R.id.diaPreCostTypeText);
        diaPreCostText = (TextView)findViewById(R.id.diaPreCostText);
        diaUseCostTypeText = (TextView)findViewById(R.id.diaUseCostTypeText);
        diaUseCostText = (TextView)findViewById(R.id.diaUseCostText);
        diaBuyYesButton = (ImageButton)findViewById(R.id.buyYesButton);
        diaBuyNoButton = (ImageButton)findViewById(R.id.buyNoButton);

        diaBuyYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 구매관련
                if(costType==1) {
                    if (buyForFruit(useCostHash)) { // 구입완료
                        storeProduct.setItemNumber(1);
                        fruit.setText(dataList.getAllScore(dataList.getFruitHashMap()));

                        if(dataList.getGoalDataByID(11).getGoalRate() < dataList.getGoalDataByID(11).getGoalCondition()) {
                            //업적 증가(아이템 구입)
                            dataList.getGoalDataByID(11).setGoalRate(1);
                        }

                        storeAdapter.notifyDataSetChanged();
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Fruit이 부족합니다!!!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    if (buyForScore(useCostHash)){ //구입완료
                        storeProduct.setItemNumber(1);
                        seed.setText(dataList.getAllScore(dataList.getScoreHashMap()));

                        if(dataList.getGoalDataByID(11).getGoalRate() < dataList.getGoalDataByID(11).getGoalCondition()) {
                            //업적 증가(아이템 구입)
                            dataList.getGoalDataByID(11).setGoalRate(1);
                        }

                        storeAdapter.notifyDataSetChanged();
                        dismiss();

                    } else { //재화부족으로 구매 실패
                        Toast.makeText(getContext(), "Score가 부족합니다!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        diaBuyNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buySuccess = getBuySuccess(0);
                costType = -1;
                dismiss();
            }
        });

    }



    public int getBuySuccess( int a){
        return a;
    }

    // 정보 받아옴
    public void setName(String str) {
        storeCheckNameText.setText(str);
    }

    public void setExplain(int effectType) {
        int resourceId = getContext().getResources().getIdentifier("product" + Integer.toString(effectType), "string", getContext().getPackageName());
        storeCheckExplainText.setText(getContext().getResources().getText(resourceId));
    }

    public void setImage(String str) {
        Log.i("path :: ", ":: " + str + "::" + getContext().getPackageName());
        //storeCheckImage.setImageResource();
    }

    public void setProduct(StoreProduct storeProduct){
        this.storeProduct = storeProduct;
    }

    public void setUseCost(int type, ConcurrentHashMap<Integer, Integer> str) {
        useCostHash = str;
        costType = type;
        if(type==1){
            diaUseCostText.setText(""+dataList.getAllScore(str));
            diaPreCostTypeText.setText("현재 보유중인 열매 : ");
            diaUseCostTypeText.setText("구매에 필요한 열매 : ");
        }else{
            diaUseCostText.setText(""+dataList.getAllScore(str));

            diaPreCostTypeText.setText("현재 보유중인 씨앗 : ");
            diaUseCostTypeText.setText("구매에 필요한 씨앗 : ");
        }
    }

    public void setPreCost(int type, ConcurrentHashMap<Integer, Integer> str) {
        Log.i("str : ",""+str );
        if(type == 1){
            preCostHash =str;
            diaPreCostText.setText(""+dataList.getAllScore(str));
        }
        else{
            preCostHash =str;
            diaPreCostText.setText(""+dataList.getAllScore(str));
        }
    }

    //게임 재화로 구매
    private Boolean buyForScore(ConcurrentHashMap<Integer, Integer> seed) {

        Iterator<Integer> iterator = seed.keySet().iterator();

        while (iterator.hasNext()) {
            int key = iterator.next();
            int value = seed.get(key);

            if (!dataList.minusScore(key, value, dataList.getScoreHashMap())) {
                return false;
            }
        }
        return true;
    }

    //현금성 재화로 구매
    private Boolean buyForFruit(ConcurrentHashMap<Integer, Integer> fruit) {

        Iterator<Integer> iterator = fruit.keySet().iterator();

        while (iterator.hasNext()) {
            int key = iterator.next();
            int value = fruit.get(key);

            if (!dataList.minusScore(key, value, dataList.getFruitHashMap())) {
                return false;
            }
        }

        return true;
    }

}

