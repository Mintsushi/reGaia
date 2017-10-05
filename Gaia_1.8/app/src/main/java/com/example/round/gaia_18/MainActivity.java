package com.example.round.gaia_18;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.round.gaia_18.Data.Flower;
import com.example.round.gaia_18.Data.OverlayPlant;
import com.example.round.gaia_18.Data.Plant;
import com.example.round.gaia_18.Dialog.Setting;
import com.example.round.gaia_18.Dialog.goalListDialog;
import com.example.round.gaia_18.Fragement.MenuDryFlower;
import com.example.round.gaia_18.Fragement.MenuFlower;
import com.example.round.gaia_18.Fragement.MenuOverlay;
import com.example.round.gaia_18.Fragement.MenuSkill;
import com.example.round.gaia_18.Fragement.MenuStore;

import java.util.ArrayList;

import static com.example.round.gaia_18.OverlayService.dataBaseHelper;
import static com.example.round.gaia_18.OverlayService.dataList;
import static com.example.round.gaia_18.OverlayService.user;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = ".MainActivity";
    public static Context context;

    //Get User Number
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public static void weatherOnOff(boolean type){
        if(type) {
            //weather.setVisibility(View.VISIBLE);
        }else {
            // weather.setVisibility(View.INVISIBLE);
        }
    }

    //Layout / View
    public static RelativeLayout relativeLayout;
    private LinearLayout linearLayout;
    private Button goal, menu, setting;
    public static TextView seed, fruit;
    public static ImageView weather;

    //FragementButton
    private ImageButton menuFlowerButton;
    private ImageButton menuASkillButton;
    private ImageButton menuPSkillButton;
    private ImageButton menuOverlayButton;
    private ImageButton menuStoreButton;
    private ImageButton menuDownButton;

    //Data
    public int gameMoney;

    //Fragment Activity
    private MenuFlower menuFlower = new MenuFlower();
    private MenuOverlay menuOverlay = new MenuOverlay();
    private MenuSkill menuSkill = new MenuSkill();
    private MenuDryFlower menuDryFlower = new MenuDryFlower();
    private MenuStore menuStore = new MenuStore();
    //Overlay Service
    public static OverlayService mOverlayService;
    private Intent overlayService;

    //ImageView(Plant) Moving
    private static Boolean moving;
    private static int originalXPos,originalYPos;

    //GPS Setting
    private android.app.AlertDialog alertDialog = null;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG,"ServieConnected");
            mOverlayService = ((OverlayService.LocalBinder)iBinder).getService();

            if(mOverlayService.setLocation()){
                setGPS("GPS Setting이 되지 않았을 수도 있습니다.\n 설정하시겠습니까?\n" +
                        "(설정하지 않으시면 외부기능 이용에 불편함이 있으실 수 있습니다.)");
            }
            //Function / 데이터 초기화
            //1. 사용자 정보 받아오기
            pref = getApplicationContext().getSharedPreferences("Login",getApplicationContext().MODE_PRIVATE);
            editor = pref.edit();
            getUserInfo(Integer.parseInt(pref.getString("id","0")));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG,"Service DisConnected");
        }
    };

    private void setGPS(String message) {
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_gps, null);

        TextView warn = (TextView) dialogView.findViewById(R.id.warn);
        warn.setText(message);
        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        Button submit = (Button) dialogView.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOverlayService.enalbeOverlayService = true;
                alertDialog.cancel();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(mOverlayService != null){
            mOverlayService.invisible();
            dataList.setClickView(relativeLayout);
            relativeLayout.setOnClickListener(this);

            seed.setText(dataList.getAllScore(dataList.getScoreHashMap()));
            fruit.setText(dataList.getAllScore(dataList.getFruitHashMap()));
            mOverlayService.setSeed();
            Log.i("repaint ", "repaint size : "+dataList.getPlants().size());
            for(int i =0;i<dataList.getPlants().size();i++) {

                Log.i("repaint ", "repaint state : "+dataList.getPlants().get(i).getState());
                    if (dataList.getPlants().get(i).getState() == 0) {
                        Log.i("repaint ", "repaint  : "+dataList.getPlants().get(i).getFlower().getFlowerName());
                        dataList.getPlants().get(i).plantRepaint( relativeLayout);
                    }

            }


            for(int i =0;i<dataList.getSkillInfos().size();i++){
                if(dataList.getSkillInfos().get(i).getSkillCoolTimeInApp() != null){
                    dataList.getSkillInfos().get(i).setSkillCoolTime(dataList.getSkillInfos().get(i).getSkillCoolTimeInApp());
                }
            }

            if(dataList.mAdapter != null) {
                dataList.mAdapter.notifyDataSetChanged();
            }
            if(dataList.flowerAdapter != null){
                dataList.flowerAdapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        if(mOverlayService != null){
            mOverlayService.visible();
            dataList.setClickView(null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //5. OverlayService
        overlayService = new Intent(MainActivity.this, OverlayService.class);


        if(!isServiceRunning(OverlayService.class)){
            startService(overlayService);
            bindService(overlayService,mServiceConnection,BIND_AUTO_CREATE);
        }

        //setContext from Static Function
        context = this.getApplicationContext();

        //Layout / View
        relativeLayout = (RelativeLayout)findViewById(R.id.relativeLayout);
        linearLayout = (LinearLayout)findViewById(R.id.menuLayout);
        seed = (TextView)findViewById(R.id.seed);
        fruit = (TextView)findViewById(R.id.fruit);
        menu = (Button)findViewById(R.id.menu);
        goal = (Button)findViewById(R.id.goal);
        weather = (ImageView)findViewById(R.id.weather);
        setting = (Button)findViewById(R.id.setting);

        //Fragement Button
        menuFlowerButton = (ImageButton)findViewById(R.id.menuFlowerButton);
        menuASkillButton = (ImageButton)findViewById(R.id.menuASkillButton);
        menuPSkillButton = (ImageButton)findViewById(R.id.menuPSkillButton);
        menuOverlayButton = (ImageButton)findViewById(R.id.menuOverlayButton);
        menuStoreButton = (ImageButton)findViewById(R.id.menuStoreButton);
        menuDownButton = (ImageButton)findViewById(R.id.menuDownButton);

        //2. Fragement Button setOnClickListener
        setImageButtonClick();
        //3. menu 버튼 활성화
        menu.setOnClickListener(this);
        //4. 화면 클릭을 통한 점수 획득
        relativeLayout.setOnClickListener(this);
        //6. goal(업적) 버튼 활성화
        goal.setOnClickListener(this);
        //7 세팅창
        setting.setOnClickListener(this);

    }

    private void getUserInfo(int id){

//        String url = "http://192.168.0.19:3000/userInfo/"+Integer.toString(id);
//
//        StringRequest request = new StringRequest(Request.Method.GET,url,
//                new Response.Listener<String>(){
//
//                    @Override
//                    public void onResponse(String response){
//
//                        Log.i("getUserInfo","response length : "+response);
//                        try{
//                            JSONObject userInfo = new JSONObject(response);
//
//                            //userInfo
//                            String userName = userInfo.getString("userName");
//                            String userEamil = userInfo.getString("userEmail");
//                            String userImage = userInfo.getString("userImage");
//                            int userDryFlower = userInfo.getInt("userDryFlowerNum");
//
//                            user.setUserName(userName);
//                            user.setUserEmail(userEamil);
//                            user.setUserImage(userImage);
//                            user.setDryFlowerItem(userDryFlower);
//
//                            //seed
//                            JSONArray seeds = userInfo.getJSONArray("seed");
//                            if(seeds.length() > 0) {
//                                for (int i = 0; i < seeds.length(); i++) {
//                                    JSONObject object = seeds.getJSONObject(i);
//                                    int seedType = object.getInt("seedType");
//                                    int seed = object.getInt("seed");
//                                    dataList.setScore(seedType, seed);
//                                }
//                            }else{
//                                dataList.setScore(0, 0);
//                            }
//
//                            //fruit
//                            JSONArray fruits = userInfo.getJSONArray("fruit");
//                            if(fruits.length() > 0) {
//                                for (int i = 0; i < fruits.length(); i++) {
//                                    JSONObject object = fruits.getJSONObject(i);
//                                    int fruitType = object.getInt("fruitType");
//                                    int fruit = object.getInt("fruit");
//                                    dataList.setFruit(fruitType, fruit);
//                                }
//                            }else{
//                                dataList.setFruit(0, 0);
//                            }
//
//                            ArrayList<Plant> plants = new ArrayList<>();
//                            //flower
//                            JSONArray flowers = userInfo.getJSONArray("flower");
//                            if(flowers.length() > 0) {
//                                for (int i = 0; i < flowers.length(); i++) {
//                                    JSONObject object = fruits.getJSONObject(i);
//                                    int flowerNo = object.getInt("flowerNo");
//                                    int level = object.getInt("level");
//                                    int hp = object.getInt("hp");
//                                    addFlower(plants, flowerNo, level, hp);
//                                }
//                            }
//
//                            //flowerArray(모든 꽃 종류에 대한 데이터)에서 꽃의 소유여부, 레벨을 초기화
//                            dataList.setPlants(plants);
//                            dataList.compareFlowers();
//                            dataList.setBuyPossible();
//
//                            //dryflower
//                            JSONArray dryFlowers = userInfo.getJSONArray("dryflower");
//                            if(dryFlowers.length() > 0) {
//                                for (int i = 0; i < dryFlowers.length(); i++) {
//                                    JSONObject object = dryFlowers.getJSONObject(i);
//                                    int flowerNo = object.getInt("dryflowerNo");
//                                    int number = object.getInt("number");
//                                    String flowerName = getFlowerName(flowerNo);
//                                    for (int j = 0; j < number; j++) {
//                                        DryFlower dryFlower = dataBaseHelper.getDryFlowerData(flowerNo);
//                                        dryFlower.setDryFlowerName(flowerName);
//                                        dataList.setDryPlats(dryFlower);
//                                    }
//                                }
//                            }
//
//                            //Store
//                            JSONArray item = userInfo.getJSONArray("item");
//                            if(item.length() > 0) {
//                                int index =0 ;
//                                for (int i = 0; i < dataList.getStoreProducts().size(); i++) {
//                                    int itemNo = i;
//                                    int number = 0;
//
//                                    if(itemNo == item.getJSONObject(index).getInt("itemNo")){
//                                        itemNo = item.getJSONObject(index).getInt("itemNo");
//                                        number = item.getJSONObject(index).getInt("number");
//
//                                        index ++;
//                                    }
//                                    dataList.setNumber(itemNo, number);
//                                }
//                            }else{
//                                for(int i =0;i<dataList.getStoreProducts().size();i++){
//                                    dataList.setNumber(i,0);
//                                }
//                            }
//
//                            //skill
//                            JSONArray skill = userInfo.getJSONArray("skill");
//                            if(skill.length() > 0) {
//                                int index = 0;
//                                for (int i = 0; i < dataList.getSkillInfos().size(); i++) {
//                                    int skillNo = i;
//                                    int skillLevel = 0;
//
//                                    if (skillNo == skill.getJSONObject(index).getInt("skillNo")) {
//                                        skillNo = skill.getJSONObject(index).getInt("skillNo");
//                                        skillLevel = skill.getJSONObject(index).getInt("level");
//
//                                        index++;
//                                    }
//                                    dataBaseHelper.getAllSkillData(skillNo, skillLevel);
//                                }
//                            }else{
//                                for(int i =0;i<dataList.getSkillInfos().size();i++){
//                                    dataBaseHelper.getAllSkillData(i,0);
//                                }
//                            }
//
//                            //goal
//                            JSONArray goal = userInfo.getJSONArray("goal");
//                            if(goal.length() >0) {
//                                int index = 0;
//                                for (int i = 0; i < dataList.getGoalInfos().size(); i++) {
//                                    int goalNo = i;
//                                    int goalLevel = 1;
//                                    int goalRate = 0;
//
//                                    if (goalNo == goal.getJSONObject(index).getInt("goalNo")) {
//                                        goalNo = goal.getJSONObject(index).getInt("goalNo");
//                                        goalLevel = goal.getJSONObject(index).getInt("level");
//                                        goalRate = goal.getJSONObject(index).getInt("rate");
//
//                                        index++;
//                                    }
//
//                                    dataList.setGoalDatas(goalNo, goalLevel, goalRate);
//                                }
//                            }else{
//                                for(int i =0;i<dataList.getGoalInfos().size();i++){
//                                    dataList.setGoalDatas(i,1,0);
//                                }
//                            }
//
//                            seed.setText(dataList.getAllScore(dataList.getScoreHashMap()));
//                            fruit.setText(dataList.getAllScore(dataList.getFruitHashMap()));
//
//                        }catch (JSONException e){
//                            Log.i("getUserInfo","json Exception : "+e.toString());
//                        }
//                    }
//                },new Response.ErrorListener(){
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getApplicationContext(),"Error : "+error.toString(),Toast.LENGTH_SHORT).show();
//                Log.i("getUserInfo",error.toString());
//            }
//        });
//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(request);

        dataList.setScore(3,50);
        dataList.setFruit(0,500);

        dataList.setItemNumber(0,0);
        dataList.setItemNumber(1,0);
        dataList.setItemNumber(2,0);
        dataList.setItemNumber(3,5);

        user.setDryFlowerItem(2);

        ArrayList<Integer> skill = new ArrayList<>();
        skill.add(0,1);
        skill.add(1,1);
        skill.add(2,1);
        skill.add(3,0);
        skill.add(4,0);
        skill.add(5,0);
        skill.add(6,0);

        for(int i =0 ;i<skill.size();i++){
            dataBaseHelper.getAllSkillData(i,skill.get(i));
        }

        dataList.setGoalDatas(0,1,0);
        dataList.setGoalDatas(1,1,0);
        dataList.setGoalDatas(2,1,0);
        dataList.setGoalDatas(3,1,0);
        dataList.setGoalDatas(4,1,0);
        dataList.setGoalDatas(5,1,0);
        dataList.setGoalDatas(6,1,0);
        dataList.setGoalDatas(7,1,0);
        dataList.setGoalDatas(8,1,0);
        dataList.setGoalDatas(9,1,0);
        dataList.setGoalDatas(10,1,0);
        dataList.setGoalDatas(11,1,0);
        dataList.setGoalDatas(12,1,0);
        dataList.setGoalDatas(13,1,0);
        dataList.setGoalDatas(14,1,0);
        dataList.setGoalDatas(15,1,0);
        dataList.setGoalDatas(16,1,0);
        dataList.setGoalDatas(17,1,0);
        dataList.setGoalDatas(18,1,0);
        dataList.setGoalDatas(19,1,0);
        dataList.setGoalDatas(20,1,0);
        dataList.setGoalDatas(21,1,0);
        dataList.setGoalDatas(22,1,0);
        dataList.setGoalDatas(23,1,0);
        dataList.setGoalDatas(24,1,0);

        seed.setText(dataList.getAllScore(dataList.getScoreHashMap()));
        fruit.setText(dataList.getAllScore(dataList.getFruitHashMap()));

        Boolean already = false;
        int plantNo = 0;
        int plantLevel = 399;
        int plantHP = 100;

        ArrayList<Flower> flowers = dataList.getFlowers();
        ArrayList<OverlayPlant> overlayPlants = dataList.getOverlayPlants();

        ArrayList<Plant> plants = new ArrayList<>();

        for (int i = 0; i < overlayPlants.size(); i++) {
            if (overlayPlants.get(i).getPlant().getPlantNo() == plantNo) {
                already = true;
            }
        }

        if(!already) {
            //plantArray(사용자가 소유하고 이름 꽃의 정보)에 데이터 추가
            for (int i = 0; i < flowers.size(); i++) {
                if (flowers.get(i).getFlowerNo() == plantNo) {

                    plants.add(new Plant(plantNo, plantLevel, flowers.get(i),plantHP));
                    break;
                }
            }
        }

        //flowerArray(모든 꽃 종류에 대한 데이터)에서 꽃의 소유여부, 레벨을 초기화
        dataList.setPlants(plants);
        dataList.compareFlowers();
        dataList.setBuyPossible();
    }

    private void addFlower(ArrayList<Plant> plants,int flowerNo, int level, int hp){

        ArrayList<Flower> flowers = dataList.getFlowers();

        //plantArray(사용자가 소유하고 이름 꽃의 정보)에 데이터 추가
        for (int i = 0; i < flowers.size(); i++) {

            if (flowers.get(i).getFlowerNo() == flowerNo) {

                plants.add(new Plant(flowerNo, level, flowers.get(i) ,hp));
                break;
            }
        }
    }

    private String getFlowerName(int flowerNo){
        ArrayList<Flower> flowers = dataList.getFlowers();

        //plantArray(사용자가 소유하고 이름 꽃의 정보)에 데이터 추가
        for (int i = 0; i < flowers.size(); i++) {
            if (flowers.get(i).getFlowerNo() == flowerNo) {
                return flowers.get(i).getFlowerName();
            }
        }

        return null;
    }

    private void setImageButtonClick(){
        menuFlowerButton.setOnClickListener(this);
        menuASkillButton.setOnClickListener(this);
        menuPSkillButton.setOnClickListener(this);
        menuOverlayButton.setOnClickListener(this);
        menuStoreButton.setOnClickListener(this);
        menuDownButton.setOnClickListener(this);
    }

    private boolean isServiceRunning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager)getSystemService(getApplicationContext().ACTIVITY_SERVICE);

        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())){
                if(mOverlayService != null){
                   return true;
                }
                else return false;
            }
        }
        return false;
    }

    @Override
    public void onClick(View view){
        if(view == menuFlowerButton){
            MainActivity.this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.list_layout,menuFlower)
                    .commit();
        }else if(view == menuASkillButton){

            MainActivity.this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.list_layout,menuSkill)
                    .commit();

        }else if(view == menuPSkillButton){

            MainActivity.this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.list_layout,menuDryFlower)
                    .commit();

        }else if(view == menuOverlayButton){

            MainActivity.this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.list_layout, menuOverlay)
                    .commit();

        }else if(view == menuStoreButton){
            MainActivity.this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.list_layout, menuStore)
                    .commit();

        }else if(view == menuDownButton){

            ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
            params.height = 0;
            linearLayout.setLayoutParams(params);

        }else if(view == menu){

            ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
            params.height = 900;
            linearLayout.setLayoutParams(params);

            MainActivity.this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.list_layout,menuFlower)
                    .commit();
        }else if(view == relativeLayout){
            dataList.windowClick();
            seed.setText(dataList.getAllScore(dataList.getScoreHashMap()));

            dataList.getGoalDataByID(9).setGoalRate(1);

        }else if(view == goal){
            goalListDialog dialog = new goalListDialog(view.getContext());
            dialog.show();
        }else if(view == setting){
            Setting dialog = new Setting(view.getContext());
            dialog.show();
        }
    }

    public static void buyPlant( Flower flower ){

        dataList.addPlant(new Plant(flower.getFlowerNo(), 1, flower, 100));
        dataList.getPlants().get(flower.getFlowerNo()).plantRepaint( relativeLayout);
    }


    public static void updatePlantLevel(int plantNo){

        ArrayList<Plant> plants = dataList.getPlants();

        //후에 level에 따른 이미지 변화도 추가
        for(int i =0 ;i < plants.size() ; i++){
            if(plants.get(i).getPlantNo() == plantNo){
                plants.get(i).setLevel(plants.get(i).getLevel()+1);
            }
        }
    }

    public static void updateScore(int score){
        seed.setText(Integer.toString(score));
    }

    public static View.OnTouchListener onTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                Log.i(TAG,"ImageVeiw Touch Down");

                moving = false;

                int [] location = new int[2];
                view.getLocationOnScreen(location);

                originalXPos = location[0];
                originalYPos = location[1];
            }
            else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){

                moving = true;

                Log.i(TAG,"ImageView Touch Move");

                int x = (int)motionEvent.getRawX();
                int y = (int)motionEvent.getRawY();

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)view.getLayoutParams();

                if (Math.abs(x - originalXPos) < 1 && Math.abs(y - originalYPos) < 1 && !moving) {
                    return false;
                }

                params.leftMargin = x-170;
                params.topMargin = y-150;

                relativeLayout.updateViewLayout(view,params);
            }
            else if(motionEvent.getAction() == MotionEvent.ACTION_UP){

                Log.i(TAG,"ImageView Touch Up");
                moving = false;
            }
            return false;
        }
    };

    public static View.OnLongClickListener onLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    };
}
