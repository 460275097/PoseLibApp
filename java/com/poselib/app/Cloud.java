package com.poselib.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 云端：匿名打开计数（只增计数接口）+ 反馈收发（ntfy 公共通道，真实联网）
 * 反馈通道：客户端 POST 一条 JSON 消息，管理员端按同一 topic 拉取，作者手机里同时留档。
 */
public class Cloud {

    private static final String ENDPOINT =
            "https://abacus.jasoncameron.dev/hit/paizhaozishi/opens";

    /** 反馈通道（topic 相当于一把随机钥匙，只有装了这个 App 的人知道） */
    private static final String NTFY = "https://ntfy.sh/";
    private static final String FB_TOPIC = "poselib_fb_9d4c7a2e6b1f83c5";

    /** 打开一次并把全网累计数取回来；失败返回 0（界面显示离线） */
    public static void reportOpen() {
        Thumbs.pool().execute(new Runnable() {
            @Override
            public void run() {
                int n = fetch();
                if (n > 0) App.p().setCloudCount(n);
            }
        });
    }

    public static int cached() {
        return App.p().cloudCount();
    }

    public static int cachedUsers() {
        return App.p().num("cloud_users", 0);
    }

    public static int cachedRank() {
        return App.p().num("cloud_rank", 0);
    }

    /** 匿名使用者编号：本机持久化一个随机 ID，只用于计算大致排名 */
    private static String anonUid() {
        String uid = App.p().numStr("anon_uid", "");
        if (uid == null || uid.isEmpty()) {
            uid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            App.p().setStr("anon_uid", uid);
        }
        return uid;
    }

    /** 上报匿名使用者，并从通道消息统计使用人数与本人排名；失败保留旧缓存 */
    public static void refreshUsers() {
        Thumbs.pool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String uid = anonUid();
                    JSONObject reg = new JSONObject();
                    reg.put("k", "user");
                    reg.put("id", uid);
                    reg.put("ts", System.currentTimeMillis());
                    post(reg.toString());

                    Map<String, Long> first = new HashMap<String, Long>();
                    JSONArray events = fetchTopic();
                    for (int i = 0; i < events.length(); i++) {
                        JSONObject o = events.optJSONObject(i);
                        if (o == null || !"user".equals(o.optString("k"))) continue;
                        String id = o.optString("id", "");
                        long ts = o.optLong("ts", 0);
                        if (id.isEmpty()) continue;
                        if (!first.containsKey(id) || ts < first.get(id)) first.put(id, ts);
                    }
                    List<String> ids = new ArrayList<String>(first.keySet());
                    java.util.Collections.sort(ids, new java.util.Comparator<String>() {
                        @Override
                        public int compare(String a, String b) {
                            return Long.compare(first.get(a), first.get(b));
                        }
                    });
                    int rank = ids.indexOf(uid) + 1;
                    App.p().setNum("cloud_users", ids.size());
                    App.p().setNum("cloud_rank", rank);
                } catch (Throwable ignored) {
                }
            }
        });
    }

    private static int fetch() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(ENDPOINT);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) return 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            return new JSONObject(sb.toString()).optInt("value", 0);
        } catch (Throwable e) {
            return 0;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // ---------------- 反馈：真实联网 ----------------

    /** 提交一条反馈，成功返回 true */
    public static boolean fbPush(JSONObject item) {
        return post(item.toString());
    }

    /** 管理员审核结果也通过同一通道广播：ok / no / del */
    public static boolean fbStatus(long id, String status) {
        try {
            JSONObject o = new JSONObject();
            o.put("k", "st");
            o.put("id", id);
            o.put("ok", status);
            return post(o.toString());
        } catch (Throwable e) {
            return false;
        }
    }

    private static boolean post(String body) {
        HttpURLConnection conn = null;
        try {
            byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
            URL url = new URL(NTFY + FB_TOPIC);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(9000);
            conn.setReadTimeout(9000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.setFixedLengthStreamingMode(bytes.length);
            OutputStream os = conn.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();
            int code = conn.getResponseCode();
            return code >= 200 && code < 300;
        } catch (Throwable e) {
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * 拉取反馈与审核状态（最近一段时间内的通道消息）。
     * 返回合并后的列表；网络失败抛异常，由界面提示离线。
     */
    /** 拉取通道内所有消息（反馈、状态、使用者注册都在同一通道，按 k 区分） */
    private static JSONArray fetchTopic() throws Exception {
        HttpURLConnection conn = null;
        JSONArray out = new JSONArray();
        try {
            URL url = new URL(NTFY + FB_TOPIC + "/json?poll=1&since=all");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(9000);
            conn.setReadTimeout(12000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/x-ndjson");
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) throw new Exception("http " + code);

            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    JSONObject ev = new JSONObject(line);
                    if (!"message".equals(ev.optString("event"))) continue;
                    String msg = ev.optString("message", "");
                    if (msg.isEmpty() || msg.charAt(0) != '{') continue;
                    out.put(new JSONObject(msg));
                } catch (Throwable ignored) {
                }
            }
            br.close();
            return out;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static JSONArray fbFetch() throws Exception {
        HttpURLConnection conn = null;
        JSONArray out = new JSONArray();
        Map<String, JSONObject> items = new HashMap<String, JSONObject>();
        Map<String, String> status = new HashMap<String, String>();
        List<String> order = new ArrayList<String>();
        JSONArray events = fetchTopic();
        try {
            for (int i = 0; i < events.length(); i++) {
                JSONObject o = events.optJSONObject(i);
                if (o == null) continue;
                String k = o.optString("k", "fb");
                if ("user".equals(k)) continue;
                if ("st".equals(k)) {
                    status.put(String.valueOf(o.optLong("id")), o.optString("ok", "pending"));
                } else {
                    String id = String.valueOf(o.optLong("id"));
                    if (o.optLong("id") == 0) continue;
                    items.put(id, o);
                    if (!order.contains(id)) order.add(id);
                }
            }
            for (String id : order) {
                JSONObject o = items.get(id);
                String st = status.get(id);
                if (st == null) st = o.optString("ok", "pending");
                if ("del".equals(st)) continue;
                o.put("ok", st);
                out.put(o);
            }
            return out;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
