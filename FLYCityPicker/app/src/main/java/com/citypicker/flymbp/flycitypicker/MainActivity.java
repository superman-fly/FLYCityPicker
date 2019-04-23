package com.citypicker.flymbp.flycitypicker;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Map<String, String> provinceMap;    //省
    private Map<String, List<Map<String, String>>> cityMap; //城市
    private Map<String, List<Map<String, String>>> districtMap; //地区
    private List<String> provinceNameList;
    private List<List<String>> cityNameList;
    private List<List<List<String>>> districtNameList;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button.setOnClickListener(this);

        Gson gson = new Gson();
        Type provinceType = new TypeToken<Map<String, String>>(){}.getType();
        String province_str = readAssets(getBaseContext(), "province.json");
        provinceMap = gson.fromJson(province_str, provinceType);

        Type cityType = new TypeToken<Map<String, List<Map<String, String>>>>(){}.getType();
        String city_str = readAssets(getBaseContext(), "city.json");
        cityMap = gson.fromJson(city_str, cityType);

        Type districtType = new TypeToken<Map<String, List<Map<String, String>>>>(){}.getType();
        String district_str = readAssets(getBaseContext(), "district.json");
        districtMap = gson.fromJson(district_str, districtType);

        getCityPickerNameList();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                new CityPicker(this, 0, 0, 0, provinceNameList, cityNameList, districtNameList, new CityPicker.OnPickerOptionsClickListener() {
                    @Override
                    public void onOptionsSelect(int options1, int options2, int options3, View view) {
                        optionsSelect(options1,options2,options3);
                    }
                }).show();
                break;
        }
    }

    // 确认选择 根据名称找id
    public void optionsSelect(int options1, int options2, int options3) {
        String provinceName = provinceNameList.get(options1);
        String cityName = cityNameList.get(options1).get(options2);
        String districtName = districtNameList.get(options1).get(options2).get(options3);

        String provinceId = getProvinceId(provinceName);     //省id
        List<Map<String, String>> provinceList = cityMap.get(provinceId);
        String cityId = lookUpTheIdByName(provinceList, cityName);    //城市id
        List<Map<String, String>> districtList = districtMap.get(cityId);
        String districtId = lookUpTheIdByName(districtList, districtName);    //地区id

        button.setText(provinceName+"("+provinceId+")"+cityName+"("+cityId+")"+districtName+"("+districtId+")");
    }

    // 获取Picker所需的联动数据
    public void getCityPickerNameList() {
        provinceNameList = new ArrayList<String>(provinceMap.values());
        cityNameList = new ArrayList<>();
        districtNameList = new ArrayList<>();

        for(Map.Entry<String,String> province : provinceMap.entrySet()) {
            List<String> cityList = new ArrayList<>();
            List<List<String>> citydisList = new ArrayList<>();
            List<Map<String,String>> city = cityMap.get(province.getKey());
            if (city == null) {
                continue;
            }
            for (int i=0;i<city.size();i++) {
                Map<String, String> map = city.get(i);
                Set<Map.Entry<String, String>> set = map.entrySet();
                for (Map.Entry<String, String> me : set) {
                    cityList.add(me.getValue());
                    List<String> list = lookUpTheDistrictNameListByCityId(me.getKey());
                    citydisList.add(list);
                }
            }
            districtNameList.add(citydisList);
            if (cityList.size() > 0) {
                cityNameList.add(cityList);
            }
        }
    }

    // 按名字查省id
    public String getProvinceId(String provinceName) {
        String key = "";
        for(Map.Entry<String,String> me : provinceMap.entrySet()) {
            if(provinceName.equals(me.getValue())) {
                key = me.getKey();
                break;
            }
        }
        return key;
    }

    // 按名字查找id
    public String lookUpTheIdByName(List<Map<String, String>> list, String name) {
        String key = "";
        for (int i=0;i<list.size();i++) {
            Map<String, String> map = list.get(i);
            Set<Map.Entry<String, String>> set = map.entrySet();
            for (Map.Entry<String, String> me : set) {
                if (name.equals(me.getValue())) {
                    key = me.getKey();
                    break;
                }
            }
        }
        return key;
    }

    // 根据城市id获取地区名称集合
    public List<String> lookUpTheDistrictNameListByCityId(String cityId) {
        List<String> districtNameList = new ArrayList<>();
        List<Map<String, String>> list = districtMap.get(cityId);
        for (int i=0;i<list.size();i++) {
            Map<String, String> map = list.get(i);
            Set<Map.Entry<String, String>> set = map.entrySet();
            for (Map.Entry<String, String> me : set) {
                districtNameList.add(me.getValue());
            }
        }
        return districtNameList;
    }

    // 获取json文件字符串
    public static String readAssets(Context context, String fileName) {
        InputStream is = null;
        String content = null;
        try {
            is = context.getAssets().open(fileName);
            if (is != null) {

                byte[] buffer = new byte[1024];
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                while (true) {
                    int readLength = is.read(buffer);
                    if (readLength == -1) break;
                    arrayOutputStream.write(buffer, 0, readLength);
                    byte[] bytes = arrayOutputStream.toByteArray();
                }
                is.close();
                arrayOutputStream.close();
                content = new String(arrayOutputStream.toByteArray());

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            content = null;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return content;
    }
}
