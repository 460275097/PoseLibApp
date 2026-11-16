package com.poselib.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/** 反馈与评论的本地存储（管理员审核状态存在同一份数据里；同时缓存在线反馈） */
public class Store {

    public static final String KEY = "fb";

    public static JSONArray feedback() {
        try {
            return new JSONArray(App.p().numStr(KEY, "[]"));
        } catch (Throwable e) {
            return new JSONArray();
        }
    }

    public static void saveFeedback(JSONArray arr) {
        App.p().setStr(KEY, arr.toString());
    }

    public static void setStatus(long id, String status) {
        try {
            JSONArray arr = feedback();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (o.optLong("id") == id) {
                    o.put("ok", status);
                }
            }
            saveFeedback(arr);
        } catch (Throwable ignored) {
        }
    }

    public static void remove(long id) {
        try {
            JSONArray arr = feedback();
            JSONArray out = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (o.optLong("id") != id) out.put(o);
            }
            saveFeedback(out);
        } catch (Throwable ignored) {
        }
    }

    public static void approveAll() {
        try {
            JSONArray arr = feedback();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (!"ok".equals(o.optString("ok"))) o.put("ok", "ok");
            }
            saveFeedback(arr);
        } catch (Throwable ignored) {
        }
    }

    public static int pendingCount() {
        int n = 0;
        try {
            JSONArray arr = feedback();
            for (int i = 0; i < arr.length(); i++) {
                if (!"ok".equals(arr.getJSONObject(i).optString("ok"))) n++;
            }
        } catch (Throwable ignored) {
        }
        return n;
    }

    /** 把服务器拉回来的反馈合并进本机（保留 mine 标记；同 id 更新内容与审核状态） */
    public static void mergeRemote(JSONArray remote) {
        try {
            JSONArray arr = feedback();
            Map<Long, JSONObject> mine = new HashMap<Long, JSONObject>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) mine.put(o.optLong("id"), o);
            }
            for (int i = 0; i < remote.length(); i++) {
                JSONObject r = remote.optJSONObject(i);
                if (r == null || r.optLong("id") == 0) continue;
                JSONObject o = mine.get(r.optLong("id"));
                if (o == null) {
                    o = new JSONObject(r.toString());
                    o.put("mine", false);
                    arr.put(o);
                    mine.put(r.optLong("id"), o);
                } else {
                    o.put("t", r.optString("t", o.optString("t", "")));
                    o.put("c", r.optString("c", o.optString("c", "")));
                    o.put("v", r.optString("v", o.optString("v", "")));
                    o.put("ts", r.optLong("ts", o.optLong("ts", 0)));
                    o.put("ok", r.optString("ok", o.optString("ok", "pending")));
                }
            }
            saveFeedback(arr);
        } catch (Throwable ignored) {
        }
    }

    public static JSONObject byId(long id) {
        try {
            JSONArray arr = feedback();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (o.optLong("id") == id) return o;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /** 标记这条反馈是否已经成功上传到服务器 */
    public static void setUploaded(long id, boolean up) {
        try {
            JSONArray arr = feedback();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (o.optLong("id") == id) o.put("up", up);
            }
            saveFeedback(arr);
        } catch (Throwable ignored) {
        }
    }
}
