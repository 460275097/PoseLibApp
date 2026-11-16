package com.poselib.app;

/** 一张姿势图：原图路径 lib/xxx、名称、所属分类 id */
public class Img {

    public final String path;
    public final String name;
    public final String nodeId;

    public Img(String path, String name, String nodeId) {
        this.path = path;
        this.name = name;
        this.nodeId = nodeId;
    }

    /** 唯一键（收藏 / 学会 / 隐藏都按它存） */
    public String key() {
        return path;
    }
}
