package com.thelow_quest_helper.thelow_quest_helper.item;

import java.util.Arrays;
import java.util.List;

class Town {
	private static final List<Town> towns = Arrays.asList(
		    new Town("ラジャスタン", -1264, 72, -1284, "馬車"),
		    new Town("ハルシオン", -1240, 95, -791, "飛行船"),
		    new Town("メルトリア王国", -185, 68, -972, "馬車", "飛行船"),
		    new Town("アシュヴィ修道院", -431, 152, -568, "飛行船"),
		    new Town("リグへルムの里", 400, 80, -1160, "馬車"),
		    new Town("イロエル", 1279, 117, -1119, "§7馬車"),
		    new Town("メリアル城塞(スノーリィ村)", 1142, 107, -868, "馬車", "飛行船", "船航路A"),
		    new Town("ラスカル", 869, 90, -423),
		    new Town("アルノース", 1263, 76, -90, "船航路A"),
		    new Town("フェルトン", 1080, 67, 696, "船航路A", "船航路B"),
		    new Town("タリバンズ", 344, 89, 566, "馬車"),
		    new Town("ゴブリン居住区", 165, 116, 86),
		    new Town("エル・ドール", 22, 116, 6, "馬車"),
		    new Town("チェスター地区", -198, 195, 177),
		    new Town("ヴェネミア", -43, 70, 748, "飛行船", "船航路B"),
		    new Town("ビルニス", -138, 184, 1290, "飛行船"),
		    new Town("ハンプニー", -777, 66, 1145, "飛行船", "船航路B"),
		    new Town("シュレリッツ", -575, 72, 601, "馬車"),
		    new Town("ベルフォート", -1009, 69, 701, "船航路B"),
		    new Town("ノイハイム", -1357, 39, -258),
		    new Town("コッコラ", 901, 25, -1275),
		    new Town("ノーブルサール", -115, 36, 507),
		    new Town("ウェリス", -1135, 65, -465),
		    new Town("オブシー", -1226, 67, 241, "馬車")
		);

	
    String name;
    double x, y, z;
    List<String> transports; // 馬車 / 飛行船 / 船航路A など

    public Town(String name, double x, double y, double z, String... transports) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.transports = Arrays.asList(transports);
    }

    public double distanceSq(double px, double py, double pz) {
        double dx = x - px;
        double dy = y - py;
        double dz = z - pz;
        return dx * dx + dy * dy + dz * dz;
    }
    
    public static String getNearestTownInfo(double px, double py, double pz) {
        Town nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        // 最寄りの町を探す
        for (Town t : towns) {
            double distSq = t.distanceSq(px, py, pz);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = t;
            }
        }

        StringBuilder sb = new StringBuilder();
        if (nearest != null) {
            sb.append("§7最寄りの町:§e ").append(nearest.name).append("\n");
        }

        // 交通機関ごとに最寄りの町を探す
        String[] transportTypes = {"馬車", "飛行船", "船航路A", "船航路B"};
        double maxDistanceSq = 400 * 400;

        for (String type : transportTypes) {
            Town best = null;
            double bestDistSq = Double.MAX_VALUE;

            for (Town t : towns) {
                if (!t.transports.contains(type)) continue;
                double distSq = t.distanceSq(px, py, pz);
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    best = t;
                }
            }

            if (best != null && bestDistSq <= maxDistanceSq) {
                sb.append("§7").append(type).append(":§e ").append(best.name).append("\n");
            }
        }

        return sb.toString();
    }

}
