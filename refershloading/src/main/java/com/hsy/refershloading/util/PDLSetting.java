package com.hsy.refershloading.util;

/**
 * 加载时文本内容设置
 * Created by HSY on 2016/12/19.
 */

public class PDLSetting {

    public String release_to_h = "松开刷新";
    public String release_to_f = "松开加载";
    public String continue_to_h = "继续向下拉";
    public String continue_to_f = "继续向上拉";

    /**
     * 自定义加载时文本内容
     *
     * @param release_to_h 松开刷新
     * @param release_to_f 松开加载
     * @param continue_to_h 继续向下拉
     * @param continue_to_f 继续向上拉
     */
    public PDLSetting(String release_to_h, String release_to_f, String continue_to_h, String continue_to_f) {
        this.release_to_h = release_to_h;
        this.release_to_f = release_to_f;
        this.continue_to_h = continue_to_h;
        this.continue_to_f = continue_to_f;
    }
}
