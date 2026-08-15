package com.hy.greenbuilding.ui.widget.verticaltablayout.adapter;

import android.view.Gravity;

import com.hy.greenbuilding.ui.widget.verticaltablayout.widget.TabView;

public class DarkHomeTabAdapter extends MyTabAdapter {
    private final String[] titles;
    private final int[] normalIcons;
    private final int[] selectedIcons;

    public DarkHomeTabAdapter(String[] titles, int[] normalIcons, int[] selectedIcons) {
        super(titles, normalIcons, selectedIcons);
        this.titles = titles;
        this.normalIcons = normalIcons;
        this.selectedIcons = selectedIcons;
    }

    @Override
    public TabView.TabTitle getTitle(int position) {
        return new TabView.TabTitle.Builder()
                .setContent(titles[position])
                .setTextColor(0xFFFFFFFF, 0xFF8D8898)
                .build();
    }

    @Override
    public int getBackground(int position) {
        return -1;
    }

    @Override
    public TabView.TabIcon getIcon(int position) {
        return new TabView.TabIcon.Builder()
                .setIcon(selectedIcons[position], normalIcons[position])
                .setIconMargin(8)
                .setIconGravity(Gravity.TOP)
                .setIconSize(24, 24)
                .build();
    }
}
