package com.citypicker.flymbp.flycitypicker;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;

import java.util.List;

/**
 * author: flymbp
 * created on: 2019/3/17 9:49 PM
 * description:
 */
public class CityPicker<T> {

    private Activity activity;
    //回调接口
    private OnPickerOptionsClickListener listener;
    private OptionsPickerView pvOptions;
    //一级联动或条件数据源
    private List<T> options1Items;
    //二级联动数据源
    private List<List<T>> options2Items;
    //三级联动数据源
    private List<List<List<T>>> options3Items;
    //默认选中的位置
    private int options1, options2, options3;

    public CityPicker(Activity activity, int options1, int options2, int options3, List<T> options1Items, List<List<T>> options2Items,
                      List<List<List<T>>> options3Items, OnPickerOptionsClickListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.options1 = options1;
        this.options2 = options2;
        this.options3 = options3;
        this.options1Items = options1Items;
        this.options2Items = options2Items;
        this.options3Items = options3Items;
        getInstance();
    }

    private OptionsPickerView getInstance() {
        pvOptions = new OptionsPickerBuilder(activity, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                if (listener != null) {
                    listener.onOptionsSelect(options1, options2, options3, v);
                }
            }
        })
                //分隔线颜色。
                .setDividerColor(Color.parseColor("#BBBBBB"))
                //滚轮背景颜色
                .setBgColor(Color.parseColor("#F5F5F5"))
                //设置两横线之间的间隔倍数
                .setLineSpacingMultiplier(1.8f)
                //设置选中项的颜色
                .setTextColorCenter(Color.parseColor("#333333"))
                //是否只显示中间选中项的label文字，false则每项item全部都带有label
                .isCenterLabel(true)
                ////设置选择的三级单位
                .setLabels("", "", "")
                //默认选中项
                .setSelectOptions(options1, options2, options3)
                .setLayoutRes(R.layout.item_picker_options, new CustomListener() {//自定义布局
                    @Override
                    public void customLayout(View v) {
                        final TextView tvSubmit = v.findViewById(R.id.tv_finish);
                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvOptions.returnData();
                                pvOptions.dismiss();
                            }
                        });
                    }
                })
                .build();
        pvOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
        return pvOptions;
    }

    public void show() {
        if (pvOptions != null && !pvOptions.isShowing()) {
            pvOptions.show();
        }
    }

    public void dismiss() {
        if (pvOptions != null && pvOptions.isShowing()) {
            pvOptions.dismiss();
        }
    }

    public interface OnPickerOptionsClickListener {
        void onOptionsSelect(int options1, int options2, int options3, View view);
    }
}
