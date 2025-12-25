package com.example.cn.helloworld.ui.address;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.example.cn.helloworld.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddressPickerDialog {
    public interface OnPickedListener {
        void onPicked(String province, String city, String district);
    }

    private static final String DATA_ASSET = "pca-code.json";
    private static List<RegionNode> cachedRegions;

    public static void show(Context context, final OnPickedListener listener) {
        final List<RegionNode> regions = loadRegions(context);
        if (regions.isEmpty()) {
            Toast.makeText(context, R.string.address_region_load_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        final NumberPicker provincePicker = createPicker(context);
        final NumberPicker cityPicker = createPicker(context);
        final NumberPicker districtPicker = createPicker(context);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        int padding = (int) (context.getResources().getDisplayMetrics().density * 12);
        container.setPadding(padding, padding, padding, padding);
        container.addView(provincePicker, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        container.addView(cityPicker, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        container.addView(districtPicker, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        updatePicker(provincePicker, namesFor(regions));

        updateCityAndDistrict(regions, provincePicker, cityPicker, districtPicker);

        provincePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateCityAndDistrict(regions, provincePicker, cityPicker, districtPicker);
            }
        });

        cityPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateDistrict(regions, provincePicker, cityPicker, districtPicker);
            }
        });

        new AlertDialog.Builder(context)
                .setTitle(R.string.address_picker_title)
                .setView(container)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RegionNode province = getSelected(regions, provincePicker.getValue());
                        List<RegionNode> cities = province == null ? Collections.<RegionNode>emptyList() : province.children;
                        RegionNode city = getSelected(cities, cityPicker.getValue());
                        List<RegionNode> districts = city == null ? Collections.<RegionNode>emptyList() : city.children;
                        RegionNode district = getSelected(districts, districtPicker.getValue());
                        if (listener != null) {
                            listener.onPicked(
                                    province == null ? "" : province.name,
                                    city == null ? "" : city.name,
                                    district == null ? "" : district.name);
                        }
                    }
                })
                .show();
    }

    private static void updateCityAndDistrict(List<RegionNode> regions, NumberPicker provincePicker,
                                              NumberPicker cityPicker, NumberPicker districtPicker) {
        RegionNode province = getSelected(regions, provincePicker.getValue());
        List<RegionNode> cities = province == null ? Collections.<RegionNode>emptyList() : province.children;
        updatePicker(cityPicker, namesFor(cities));
        cityPicker.setValue(0);
        updateDistrict(regions, provincePicker, cityPicker, districtPicker);
    }

    private static void updateDistrict(List<RegionNode> regions, NumberPicker provincePicker,
                                       NumberPicker cityPicker, NumberPicker districtPicker) {
        RegionNode province = getSelected(regions, provincePicker.getValue());
        List<RegionNode> cities = province == null ? Collections.<RegionNode>emptyList() : province.children;
        RegionNode city = getSelected(cities, cityPicker.getValue());
        List<RegionNode> districts = city == null ? Collections.<RegionNode>emptyList() : city.children;
        updatePicker(districtPicker, namesFor(districts));
        districtPicker.setValue(0);
    }

    private static NumberPicker createPicker(Context context) {
        NumberPicker picker = new NumberPicker(context);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setWrapSelectorWheel(false);
        return picker;
    }

    private static void updatePicker(NumberPicker picker, String[] values) {
        if (values.length == 0) {
            values = new String[]{""};
        }
        picker.setDisplayedValues(null);
        picker.setMinValue(0);
        picker.setMaxValue(values.length - 1);
        picker.setDisplayedValues(values);
        picker.setEnabled(!TextUtils.isEmpty(values[0]));
    }

    private static String[] namesFor(List<RegionNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return new String[0];
        }
        String[] names = new String[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            names[i] = nodes.get(i).name;
        }
        return names;
    }

    private static RegionNode getSelected(List<RegionNode> nodes, int index) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        if (index < 0 || index >= nodes.size()) {
            return nodes.get(0);
        }
        return nodes.get(index);
    }

    private static List<RegionNode> loadRegions(Context context) {
        if (cachedRegions != null) {
            return cachedRegions;
        }
        List<RegionNode> regions = new ArrayList<RegionNode>();
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open(DATA_ASSET);
            String json = readAll(inputStream);
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject node = array.optJSONObject(i);
                if (node != null) {
                    regions.add(parseNode(node));
                }
            }
        } catch (IOException | JSONException e) {
            return Collections.emptyList();
        }
        cachedRegions = regions;
        return regions;
    }

    private static RegionNode parseNode(JSONObject object) {
        String name = object.optString("name");
        JSONArray children = object.optJSONArray("children");
        List<RegionNode> items = new ArrayList<RegionNode>();
        if (children != null) {
            for (int i = 0; i < children.length(); i++) {
                JSONObject child = children.optJSONObject(i);
                if (child != null) {
                    items.add(parseNode(child));
                }
            }
        }
        return new RegionNode(name, items);
    }

    private static String readAll(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        inputStream.close();
        return builder.toString();
    }

    private static class RegionNode {
        final String name;
        final List<RegionNode> children;

        RegionNode(String name, List<RegionNode> children) {
            this.name = name;
            this.children = children == null ? new ArrayList<RegionNode>() : children;
        }
    }
}
