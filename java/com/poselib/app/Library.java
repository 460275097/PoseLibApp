package com.poselib.app;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 图库索引（assets/library.json），已移除的图不参与展示 */
public class Library {

    public static class Node {
        public String id;
        public String title;
        public String cover;
        public String tip;
        public int count;
        public int depth;
        public List<String> children = new ArrayList<>();
        public List<Img> images = new ArrayList<>();
    }

    public static class HomeItem {
        public String nodeId;
        public String title;
        public String cover;
    }

    public Map<String, Node> nodes = new LinkedHashMap<>();
    public List<String> root = new ArrayList<>();
    public List<HomeItem> home = new ArrayList<>();
    public List<HomeItem> catalog = new ArrayList<>();
    public List<String> randomNodes = new ArrayList<>();
    public int total;

    private List<Img> allCache;

    public Node node(String id) {
        return nodes.get(id);
    }

    public static Library load(Context c) {
        Library lib = new Library();
        try {
            InputStream is = c.getAssets().open("library.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            JSONObject o = new JSONObject(sb.toString());
            lib.total = o.optInt("total", 0);

            JSONArray rootArr = o.optJSONArray("root");
            if (rootArr != null) {
                for (int i = 0; i < rootArr.length(); i++) lib.root.add(rootArr.getString(i));
            }

            JSONObject ns = o.optJSONObject("nodes");
            if (ns != null) {
                java.util.Iterator<String> it = ns.keys();
                while (it.hasNext()) {
                    String id = it.next();
                    JSONObject n = ns.getJSONObject(id);
                    Node node = new Node();
                    node.id = id;
                    node.title = n.optString("title", "");
                    node.cover = n.optString("cover", "");
                    node.tip = n.optString("tip", "");
                    node.count = n.optInt("count", 0);
                    node.depth = n.optInt("depth", 1);
                    JSONArray ch = n.optJSONArray("children");
                    if (ch != null) {
                        for (int j = 0; j < ch.length(); j++) node.children.add(ch.getString(j));
                    }
                    JSONArray im = n.optJSONArray("images");
                    if (im != null) {
                        for (int j = 0; j < im.length(); j++) {
                            JSONArray one = im.getJSONArray(j);
                            node.images.add(new Img(one.getString(0), one.optString(1, ""), id));
                        }
                    }
                    lib.nodes.put(id, node);
                }
            }

            JSONArray hm = o.optJSONArray("home");
            if (hm != null) {
                for (int i = 0; i < hm.length(); i++) {
                    JSONObject h = hm.getJSONObject(i);
                    HomeItem item = new HomeItem();
                    item.nodeId = h.optString("node", "");
                    item.title = h.optString("title", "");
                    item.cover = h.optString("cover", "");
                    lib.home.add(item);
                }
            }

            JSONArray cat = o.optJSONArray("catalog");
            if (cat != null) {
                for (int i = 0; i < cat.length(); i++) {
                    JSONObject h = cat.getJSONObject(i);
                    HomeItem item = new HomeItem();
                    item.nodeId = h.optString("node", "");
                    item.title = h.optString("title", "");
                    item.cover = h.optString("cover", "");
                    lib.catalog.add(item);
                }
            }

            JSONArray rnd = o.optJSONArray("random");
            if (rnd != null) {
                for (int i = 0; i < rnd.length(); i++) lib.randomNodes.add(rnd.getString(i));
            }
        } catch (Throwable e) {
            android.util.Log.e("PoseLib", "library load failed", e);
        }
        return lib;
    }

    /** 首页实际展示的分类：按用户自定义顺序，剔除被隐藏的 */
    public List<HomeItem> homeList() {
        return orderedList(home, App.p().homeOrder(), false);
    }

    /** 目录分类实际展示项：与快速分类独立排序、独立隐藏 */
    public List<HomeItem> catalogList() {
        return orderedList(catalog, App.p().catalogOrder(), true);
    }

    private List<HomeItem> orderedList(List<HomeItem> base, String order, boolean catalog) {
        List<HomeItem> out = new ArrayList<>();
        Prefs p = App.p();
        if (order != null && !order.isEmpty()) {
            String[] ids = order.split("\n");
            for (String id : ids) {
                if (id == null || id.isEmpty()) continue;
                if (catalog ? p.isCatalogHidden(id) : p.isHomeHidden(id)) continue;
                for (HomeItem h : base) {
                    if (id.equals(h.nodeId)) {
                        out.add(h);
                        break;
                    }
                }
            }
        }
        for (HomeItem h : base) {
            if (catalog ? p.isCatalogHidden(h.nodeId) : p.isHomeHidden(h.nodeId)) continue;
            boolean has = false;
            for (HomeItem o : out) if (o.nodeId.equals(h.nodeId)) has = true;
            if (!has) out.add(h);
        }
        return out;
    }

    /** 首页全部分类的当前顺序（含被隐藏的），用于调序 */
    public List<String> homeAllIds() {
        return allIds(home, App.p().homeOrder());
    }

    public List<String> catalogAllIds() {
        return allIds(catalog, App.p().catalogOrder());
    }

    private List<String> allIds(List<HomeItem> base, String order) {
        List<String> out = new ArrayList<>();
        if (order != null && !order.isEmpty()) {
            for (String id : order.split("\n")) {
                if (id == null || id.isEmpty()) continue;
                for (HomeItem h : base) if (id.equals(h.nodeId)) out.add(id);
            }
        }
        for (HomeItem h : base) if (!out.contains(h.nodeId)) out.add(h.nodeId);
        return out;
    }

    private boolean hidden(String key) {
        try {
            return App.p().isHidden(key);
        } catch (Throwable e) {
            return false;
        }
    }

    /** 全部图片（按目录顺序，跳过已移除） */
    public List<Img> all() {
        if (allCache == null) {
            List<Img> out = new ArrayList<>();
            for (String id : root) collect(id, out);
            allCache = out;
            total = out.size();
        }
        return allCache;
    }

    /** 删除/恢复图片后让缓存失效 */
    public void invalidate() {
        allCache = null;
    }

    private void collect(String id, List<Img> out) {
        Node n = nodes.get(id);
        if (n == null) return;
        for (Img img : n.images) {
            if (!hidden(img.key())) out.add(img);
        }
        for (String cid : n.children) collect(cid, out);
    }

    /** 某个分类下的图（含子分类） */
    public List<Img> imagesOf(String nodeId) {
        List<Img> out = new ArrayList<>();
        collect(nodeId, out);
        return out;
    }

    /** 随机范围目录里所有图 */
    public List<Img> randomPool() {
        List<Img> out = new ArrayList<>();
        for (String id : randomNodes) collect(id, out);
        return out;
    }

    /** 这张图对应的一句话技巧（找不到就往上找父分类） */
    public String tipOf(String nodeId) {
        String id = nodeId;
        while (id != null && !id.isEmpty()) {
            Node n = nodes.get(id);
            if (n != null && n.tip != null && !n.tip.isEmpty()) return n.tip;
            int slash = id.lastIndexOf('/');
            if (slash < 0) break;
            id = id.substring(0, slash);
        }
        Node n = nodes.get(nodeId);
        if (n != null && n.count > 0) {
            return "先站好再按快门：重心落单脚、肩线放松、下巴微收，拍三张挑一张。";
        }
        return "把动作做满一点，别停在半途，连拍两三张更自然。";
    }

    /** 分类路径标题，例如「单人 / 全身 / 站姿」 */
    public String pathTitle(String nodeId) {
        if (nodeId == null) return "";
        String[] parts = nodeId.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(" · ");
            sb.append(parts[i]);
        }
        return sb.toString();
    }
}
