# FLYCityPicker
Android 使用Gson解析json不固定key

用笨方法把json解析成Map，然后取所需要的东西。

就拿地区选择为例吧。有三个地区级别json文件，用对应的id作为key。

province.json 文件 省级数据如下

{
  "1": "北京",
  "37": "河北省",
...
}

city.json 文件 市级数据如下

{
  "1": [
    {
      "2": "北京市"
    }
  ],
  "37": [
    {
      "38": "石家庄市"
    },
    {
      "61": "唐山市"
    },
    {
      "76": "秦皇岛市"
    },
    ...
  ]
...
}

district.json 区县级数据

{
  "2": [
    {
      "3": "东城区"
    },
    {
      "4": "西城区"
    },
    ...
  ],
...
}

引入了两个框架，用于解析json数据和滚动选择地区
implementation 'com.contrarywind:Android-PickerView:4.1.6'
implementation 'com.google.code.gson:gson:2.8.0'
# 一、json文件已有，数据结构已知。接下来设定结构类型解析json数据。

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
# 二、Map集合已到手，Android-PickerView 需要联动数据集合，得把地区名称取出来

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

// 根据城市id获取地区名称集合

    public List<String> lookUpTheDistrictNameListByCityId(String cityId) {
        List<String> districtNameList = new ArrayList<>();
        List<Map<String, String>> list = districtMap.get(cityId);
        for (int i=0;i<list.size();i++) {
            Map<String, String> map = list.get(i);
            for (Map.Entry<String, String> me : map.entrySet()) {
                districtNameList.add(me.getValue());
            }
        }
        return districtNameList;
    }

# 三、PickerView选择后的索引取地区名称，根据地区名找相应的id

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



总结：数据结构知道的话是可以取出来的，用的是笨方法遍历查找。但是对于这个场景遍历次数太多了，用这个com.contrarywind:Android-PickerView的话，感觉不太合适。

csdn https://blog.csdn.net/feiyue0823/article/details/88703406
