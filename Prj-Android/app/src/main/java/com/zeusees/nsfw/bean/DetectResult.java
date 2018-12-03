// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.zeusees.nsfw.bean;

import java.io.Serializable;

/**
 *
 * @author Yang.kezun
 * @since 2018.2.1
 */
public class DetectResult implements Serializable {


    private String classId;
    private double confidence;

    public DetectResult(String classId, double confidence) {
        this.classId = classId;
        this.confidence = confidence;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("类别:");
        sb.append(classId);
        sb.append("　");
        sb.append("准确率:");
        sb.append(confidence);

        return (sb.toString());
    }
}
